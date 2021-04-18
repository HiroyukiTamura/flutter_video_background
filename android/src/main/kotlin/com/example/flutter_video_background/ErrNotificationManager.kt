package com.example.flutter_video_background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.example.flutter_video_background.extensions.id
import com.google.android.exoplayer2.ExoPlaybackException

class ErrNotificationManager(
        private val context: Context,
        private val config: ErrNotificationManagerConfig,
) {

    private val ntfManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(config.channelTitleResId)
            val mChannel = NotificationChannel(config.channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(config.channelDescResId)
                setShowBadge(false)
            }

            ntfManager.createNotificationChannel(mChannel)
        }
    }

    fun showErrNotification(@ExoPlaybackException.Type type: Int, metadata: MediaMetadataCompat?) {
        val title = config.getContentTitle(type, metadata)
        val content = config.getContentText(type, metadata)
        val ntf = NotificationCompat.Builder(context, config.channelId).apply {
            setSmallIcon(config.ntfIcon)
            setContentTitle(title)
            content?.let {
                setContentText(it)
            }
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()

        ntfManager.notify(metadata?.id?.hashCode() ?: config.defaultNtfId, ntf)
    }

    abstract class ErrNotificationManagerConfig(
            val channelId: String,
            val defaultNtfId: Int,
            @StringRes val channelTitleResId: Int,
            @StringRes val channelDescResId: Int,
            @DrawableRes val ntfIcon: Int,
    ) {
        abstract fun getContentTitle(@ExoPlaybackException.Type exceptionType: Int, lastPlayed: MediaMetadataCompat?): CharSequence
        abstract fun getContentText(@ExoPlaybackException.Type exceptionType: Int, lastPlayed: MediaMetadataCompat?): CharSequence?
    }
}
