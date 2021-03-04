package com.example.flutter_video_background_example

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.flutter_video_background.ErrNotificationManager
import com.example.flutter_video_background.MusicServiceDelegate
import com.example.flutter_video_background.UampNotificationManager
import com.example.flutter_video_background.extensions.title

/**
 * This class is the entry point for browsing and playback commands from the APP's UI
 * and other apps that wish to play music via UAMP (for example, Android Auto or
 * the Google Assistant).
 *
 * Browsing begins with the method [MusicService.onGetRoot], and continues in
 * the callback [MusicService.onLoadChildren].
 *
 * For more information on implementing a MediaBrowserService,
 * visit [https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html).
 *
 * This class also handles playback for Cast sessions.
 * When a Cast session is active, playback commands are passed to a
 * [CastPlayer](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/ext/cast/CastPlayer.html),
 * otherwise they are passed to an ExoPlayer for local playback.
 */
class MusicService : MediaBrowserServiceCompat() {

    private val errNtfConfig = object : ErrNotificationManager.ErrNotificationManagerConfig(
            CHANNEL_ID_ERR,
            DEFAULT_ERR_NTF_ID,
            R.string.err_channel_name,
            R.string.err_channel_desc,
            R.drawable.baseline_error_white_24dp,
    ) {
        override fun getContentTitle(exceptionType: Int, lastPlayed: MediaMetadataCompat?): CharSequence =
                getString(R.string.err_ntf_title_unknown)

        override fun getContentText(exceptionType: Int, lastPlayed: MediaMetadataCompat?): CharSequence? = lastPlayed?.title
    }

    private val audioNtfConfig = UampNotificationManager.Config(
            CHANNEL_ID,
            NTF_ID,
            R.string.channel_name,
            R.string.channel_desc,
            R.drawable.ntf_icon,
            R.dimen.program_img_w,
            R.dimen.program_img_h,
    )

    private val serviceDelegate = MusicServiceDelegate(this, BuildConfig.APPLICATION_ID, this::class.java, audioNtfConfig, errNtfConfig)

    override fun onCreate() {
        super.onCreate()
        serviceDelegate.onCreate()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        serviceDelegate.onTaskRemoved(rootIntent)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() = serviceDelegate.onDestroy()

    override fun onGetRoot(
            clientPackageName: String,
            clientUid: Int,
            rootHints: Bundle?
    ) = serviceDelegate.onGetRoot()

    override fun onLoadChildren(
            parentMediaId: String,
            result: Result<List<MediaItem>>
    ) = serviceDelegate.onLoadChildren(result)
}

private const val CHANNEL_ID = "com.example.flutter_video_background_example/main"
private const val CHANNEL_ID_ERR = "com.example.flutter_video_background_example/err"
private const val NTF_ID = 123454321
private const val DEFAULT_ERR_NTF_ID = 1234567
