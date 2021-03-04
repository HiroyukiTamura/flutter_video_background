package com.example.flutter_video_background

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.flutter_video_background.extensions.isHlsUrl
import com.example.flutter_video_background.model.MediaItemMsg
import com.example.flutter_video_background.extensions.mediaUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.MediaItem as ExoMediaItem
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MusicServiceDelegate(
        private val service: MediaBrowserServiceCompat,
        private val applicationName: String,
        private val serviceClz: Class<out MediaBrowserServiceCompat>,
        private val notificationConfig: UampNotificationManager.Config,
        private val errNotificationManagerConfig: ErrNotificationManager.ErrNotificationManagerConfig,
) {

    private lateinit var notificationManager: UampNotificationManager
    private lateinit var errNotificationManager: ErrNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var lastPlayed: MediaMetadataCompat? = null
    private lateinit var storage: PersistentStorage

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var isForegroundService = false

    private val uAmpAudioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()

    private val playerListener = PlayerEventListener()

    /**
     * Configure ExoPlayer to handle audio focus for us.
     * See [Player.AudioComponent.setAudioAttributes] for details.
     */
    private val exoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(service).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    /**
     * must call from [MediaBrowserServiceCompat.onCreate]
     */
    fun onCreate() {

        storage = PersistentStorage.getInstance(service, notificationConfig.ntfImgHeight, notificationConfig.ntfImgWidth)

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
                service.packageManager?.getLaunchIntentForPackage(service.packageName)?.let { sessionIntent ->
                    PendingIntent.getActivity(service, 0, sessionIntent, 0)
                }

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(service, MEDIA_SESSION_TAG)
                .apply {
                    setSessionActivity(sessionActivityPendingIntent)
                    isActive = true
                }

        /**
         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat].
         *
         * It is possible to wait to set the session token, if required for a specific use-case.
         * However, the token *must* be set by the time [MediaBrowserServiceCompat.onGetRoot]
         * returns, or the connection will fail silently. (The system will not even call
         * [MediaBrowserCompat.ConnectionCallback.onConnectionFailed].)
         */
        service.sessionToken = mediaSession.sessionToken

        /**
         * The notification manager will use our player and media session to decide when to post
         * notifications. When notifications are posted or removed our listener will be called, this
         * allows us to promote the service to foreground (required so that we're not killed if
         * the main UI is not visible).
         */
        notificationManager = UampNotificationManager(
                service,
                notificationConfig,
                mediaSession.sessionToken,
                PlayerNotificationListener()
        )

        errNotificationManager = ErrNotificationManager(service, errNotificationManagerConfig)

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setQueueNavigator(UampQueueNavigator(mediaSession))
            setPlaybackPreparer(UampPlaybackPreparer())
            setPlayer(exoPlayer)
        }

        notificationManager.showNotificationForPlayer(exoPlayer)
    }

    /**
     * must call from [MediaBrowserServiceCompat.onTaskRemoved]
     *
     * This is the code that causes UAMP to stop playing when swiping the activity away from
     * recents. The choice to do this is app specific. Some apps stop playback, while others allow
     * playback to continue and allow users to stop it with the notification.
     */
    fun onTaskRemoved(rootIntent: Intent) {
        saveRecentSongToStorage()

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE] triggering
         * [Player.EventListener.onPlayerStateChanged] to be called. This will cause the
         * notification to be hidden and trigger
         * [PlayerNotificationManager.NotificationListener.onNotificationCancelled] to be called.
         * The service will then remove itself as a foreground service, and will call
         * [MediaBrowserServiceCompat.stopSelf].
         */
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    /**
     * must call from [MediaBrowserServiceCompat.onDestroy]
     */
    fun onDestroy() {
        Logger.instance.d(TAG, "onDestroy")
        mediaSession.run {
            isActive = false
            release()
        }

        // Cancel coroutines when the service is going away.
        serviceJob.cancel()

        // Free ExoPlayer resources.
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    /**
     * must call from [MediaBrowserServiceCompat.onGetRoot]
     *
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     */
    fun onGetRoot(): MediaBrowserServiceCompat.BrowserRoot = MediaBrowserServiceCompat.BrowserRoot(UAMP_RECENT_ROOT, null)

    /**
     * must call from [MediaBrowserServiceCompat.onLoadChildren]
     */
    fun onLoadChildren(result: MediaBrowserServiceCompat.Result<List<MediaItem>>) = result.sendResult(storage.loadRecentSong()?.let { song -> listOf(song) })

    /**
     * Load the supplied list of songs and the song to play into the current player.
     */
    private fun prepareMedia(
            itemToPlay: MediaItemMsg,
            playWhenReady: Boolean
    ) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.playWhenReady = playWhenReady

        lastPlayed = itemToPlay.media

        val mediaSource = createMediaSource(itemToPlay.cookie, itemToPlay.media.mediaUri)
        Logger.instance.d(TAG, itemToPlay.playbackStartPositionMs)
        if (itemToPlay.playbackStartPositionMs != C.TIME_UNSET)
            exoPlayer.setMediaSource(mediaSource, itemToPlay.playbackStartPositionMs)
        else
            exoPlayer.setMediaSource(mediaSource, true)

        exoPlayer.prepare()
    }

    private fun createMediaSource(cookie: String?, uri: Uri): MediaSource {
        val userAgent = Util.getUserAgent(service, applicationName)
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent)
        cookie?.let {
            dataSourceFactory.setDefaultRequestProperties(mapOf("Cookie" to it))
        }

        val mediaItem = ExoMediaItem.fromUri(uri)

        return if (uri.isHlsUrl)
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        else
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
    }

    private fun saveRecentSongToStorage() {
        // Obtain the current song details *before* saving them on a separate thread, otherwise
        // the current player may have been unloaded by the time the save routine runs.
        val description = lastPlayed?.description ?: return
        val position = exoPlayer.currentPosition

        serviceScope.launch {
            storage.saveRecentSong(description, position)
        }
    }

    /**
     * Listen for notification events.
     */
    private inner class PlayerNotificationListener :
            PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                        service.applicationContext,
                        Intent(service.applicationContext, serviceClz)
                )

                service.startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            service.stopForeground(true)
            isForegroundService = false
            service.stopSelf()
        }
    }


    /**
     * Listen for events from ExoPlayer.
     */
    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Logger.instance.d(TAG, "$playWhenReady , $playbackState")

            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(exoPlayer)
                    if (playbackState == Player.STATE_READY) {
                        if (!playWhenReady) {

                            saveRecentSongToStorage()

                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed. An alternative would be to provide a
                            // "close" button in the notification which stops playback and clears
                            // the notification.
                            service.stopForeground(false)
                        }
                    }
                }
                Player.STATE_IDLE ->
                    if (!playWhenReady)
                        notificationManager.hideNotification()
                else ->
                    notificationManager.hideNotification()
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            when (error.type) {
                ExoPlaybackException.TYPE_SOURCE ->
                    Logger.instance.e(TAG, "TYPE_SOURCE: " + error.sourceException.message)
                ExoPlaybackException.TYPE_RENDERER ->
                    Logger.instance.e(TAG, "TYPE_RENDERER: " + error.rendererException.message)
                ExoPlaybackException.TYPE_UNEXPECTED ->
                    Logger.instance.e(TAG, "TYPE_UNEXPECTED: " + error.unexpectedException.message)
                ExoPlaybackException.TYPE_REMOTE ->
                    Logger.instance.e(TAG, "TYPE_REMOTE: " + error.message)
            }

            errNotificationManager.showErrNotification(error.type, lastPlayed)
        }
    }

    private inner class UampQueueNavigator(
            mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
                lastPlayed?.description ?: MediaDescriptionCompat.Builder().build()
    }

    private inner class UampPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        /**
         * UAMP supports preparing (and playing) from search, as well as media ID, so those
         * capabilities are declared here.
         *
         * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
         */
        override fun getSupportedPrepareActions(): Long =
                PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            val recentSong = storage.loadRecentSong() ?: return
            onPrepareFromMediaId(
                    recentSong.mediaId!!,
                    playWhenReady,
                    recentSong.description.extras
            )
        }

        override fun onPrepareFromMediaId(
                mediaId: String,
                playWhenReady: Boolean,
                extras: Bundle?
        ) {
            extras ?: return
            prepareMedia(
                    MediaItemMsg(extras),
                    playWhenReady,
            )
        }

        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onCommand(
                player: Player,
                controlDispatcher: ControlDispatcher,
                command: String,
                extras: Bundle?,
                cb: ResultReceiver?
        ) = false
    }
}

private const val MEDIA_SESSION_TAG = "MusicService"
private const val UAMP_RECENT_ROOT = "__RECENT__"
private const val TAG = "MusicServiceDelegate"