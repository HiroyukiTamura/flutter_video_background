package com.example.flutter_video_background

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media.MediaBrowserServiceCompat
import com.example.flutter_video_background.extensions.id

/**
 * Class that manages a connection to a [MediaBrowserServiceCompat] instance, typically a
 * [MusicService] or one of its subclasses.
 *
 * Typically it's best to construct/inject dependencies either using DI or, as UAMP does,
 * using [InjectorUtils] in the app module. There are a few difficulties for that here:
 * - [MediaBrowserCompat] is a final class, so mocking it directly is difficult.
 * - A [MediaBrowserConnectionCallback] is a parameter into the construction of
 *   a [MediaBrowserCompat], and provides callbacks to this class.
 * - [MediaBrowserCompat.ConnectionCallback.onConnected] is the best place to construct
 *   a [MediaControllerCompat] that will be used to control the [MediaSessionCompat].
 *
 *  Because of these reasons, rather than constructing additional classes, this is treated as
 *  a black box (which is why there's very little logic here).
 *
 *  This is also why the parameters to construct a [MusicServiceConnection] are simple
 *  parameters, rather than private properties. They're only required to build the
 *  [MediaBrowserConnectionCallback] and [MediaBrowserCompat] objects.
 */
class MusicServiceConnection(context: Context, serviceComponent: ComponentName) {
    val isConnected = MutableLiveData(false)
    val networkFailure = MutableLiveData(false)//todo handle

    val rootMediaId: String get() = mediaBrowser.root

    val playbackState = MutableLiveData(EMPTY_PLAYBACK_STATE)
    val nowPlaying = MutableLiveData(NOTHING_PLAYING)

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    /**
     * must check [isConnected]
     */
    val currentPosition: Long
        get() {
            val position = mediaController.playbackState.position
            return if (position < 0)
                0
            else
                position
        }

    /**
     * must check [isConnected]
     */
    val isStatePlaying: Boolean
        get() = mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
            context,
            serviceComponent,
            mediaBrowserConnectionCallback, null
    ).apply { connect() }
    private lateinit var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun sendCommand(command: String, parameters: Bundle?) =
            sendCommand(command, parameters) { _, _ -> }

    fun sendCommand(
            command: String,
            parameters: Bundle?,
            resultCallback: ((Int, Bundle?) -> Unit)
    ) = if (mediaBrowser.isConnected) {
        mediaController.sendCommand(command, parameters, object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                resultCallback(resultCode, resultData)
            }
        })
        true
    } else {
        false
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
            MediaBrowserCompat.ConnectionCallback() {
        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully
         * completed.
         */
        override fun onConnected() {
            // Get a MediaController for the MediaSession.
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.postValue(true)
        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        override fun onConnectionSuspended() = isConnected.postValue(false)

        /**
         * Invoked when the connection to the media browser failed.
         */
        override fun onConnectionFailed() = isConnected.postValue(false)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) =
                playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)

        /**
         * When ExoPlayer stops we will receive a callback with "empty" metadata. This is a
         * metadata object which has been instantiated with default values. The default value
         * for media ID is null so we assume that if this value is null we are not playing
         * anything.
         */
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            val value = if (metadata?.id == null)
                NOTHING_PLAYING
            else
                metadata
            nowPlaying.postValue(value)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) = Unit

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_FAILURE -> networkFailure.postValue(true)
            }
        }

        /**
         * Normally if a [MediaBrowserServiceCompat] drops its connection the callback comes via
         * [MediaControllerCompat.Callback] (here). But since other connection status events
         * are sent to [MediaBrowserCompat.ConnectionCallback], we catch the disconnect here and
         * send it on to the other callback.
         */
        override fun onSessionDestroyed() = mediaBrowserConnectionCallback.onConnectionSuspended()
    }

    companion object {
        /**
         * (Media) Session events
         */
        const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"

        // For Singleton instantiation.
        @Volatile
        private var instance: MusicServiceConnection? = null

        fun getInstance(app: Application, serviceComponent: ComponentName) =
                instance ?: synchronized(this) {
                    instance ?: MusicServiceConnection(app.applicationContext, serviceComponent)
                            .also { instance = it }
                }
    }
}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
        .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
        .build()
