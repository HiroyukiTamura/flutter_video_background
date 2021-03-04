package com.example.flutter_video_background

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*

/**
 * A wrapper class for ExoPlayer's PlayerNotificationManager. It sets up the notification shown to
 * the user during audio playback and provides track metadata, such as track title and icon image.
 */
class UampNotificationManager(
        private val context: Context,
        private val config: Config,
        sessionToken: MediaSessionCompat.Token,
        notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val imgWidth = context.resources.getDimensionPixelSize(config.ntfImgWidth)
    private val imgHeight = context.resources.getDimensionPixelOffset(config.ntfImgHeight)
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        val dispatcher = DefaultControlDispatcher(REWIND_MS, REWIND_MS)

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                config.channelId,
                config.channelTitleResId,
                config.channelDescResId,
                config.notificationId,
                DescriptionAdapter(mediaController),
                notificationListener,
        ).apply {
            setMediaSessionToken(sessionToken)
            setSmallIcon(config.ntfIcon)
            setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            setControlDispatcher(dispatcher)
        }
    }

    fun hideNotification() = notificationManager.setPlayer(null)

    fun showNotificationForPlayer(player: Player) = notificationManager.setPlayer(player)

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
            PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null
        private val tag = "DescriptionAdapter"

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
                controller.sessionActivity

        override fun getCurrentContentText(player: Player) =
                controller.metadata.description.subtitle.toString()

        override fun getCurrentContentTitle(player: Player) =
                controller.metadata.description.title.toString()

        override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = controller.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {

                // Cache the bitmap for the current song so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let {
                        resolveUriAsBitmap(it)
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
            // Block on downloading artwork.
            try {
                Glide.with(context).applyDefaultRequestOptions(glideOptions)
                        .asBitmap()
                        .load(uri)
                        .submit(imgWidth, imgHeight)
                        .get()
            } catch (e: Exception) {
                Logger.instance.e(tag, e)
                null
            }
        }
    }

    data class Config(
            val channelId: String,
            val notificationId: Int,
            @StringRes val channelTitleResId: Int,
            @StringRes val channelDescResId: Int,
            @DrawableRes val ntfIcon: Int,
            @DimenRes val ntfImgWidth: Int,
            @DimenRes val ntfImgHeight: Int,
    )
}

private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.DATA)

private const val REWIND_MS = 30 * 1000L