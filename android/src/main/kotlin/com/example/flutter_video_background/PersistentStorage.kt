package com.example.flutter_video_background

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.DimenRes
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.example.flutter_video_background.extensions.asAlbumArtContentUri
import com.example.flutter_video_background.model.MediaItemMsg
import com.example.flutter_video_background.model.MediaItemMsg.Companion.MEDIA_DESCRIPTION_EXTRAS_MEDIA_COOKIE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersistentStorage private constructor(private val context: Context, @DimenRes private val ntfImgH: Int, @DimenRes private val ntfImgW: Int) {

    private val imgWidth = context.resources.getDimensionPixelSize(ntfImgW)
    private val imgHeight = context.resources.getDimensionPixelOffset(ntfImgH)

    /**
     * Store any data which must persist between restarts, such as the most recently played song.
     */
    private var preferences: SharedPreferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
    )

    companion object {

        @Volatile
        private var instance: PersistentStorage? = null

        fun getInstance(context: Context, @DimenRes ntfImgH: Int, @DimenRes ntfImgW: Int) =
                instance ?: synchronized(this) {
                    instance ?: PersistentStorage(context, ntfImgH, ntfImgW).also { instance = it }
                }
    }

    suspend fun saveRecentSong(description: MediaDescriptionCompat, position: Long) {

        withContext(Dispatchers.IO) {

            /**
             * After booting, Android will attempt to build static media controls for the most
             * recently played song. Artwork for these media controls should not be loaded
             * from the network as it may be too slow or unavailable immediately after boot. Instead
             * we convert the iconUri to point to the Glide on-disk cache.
             */
            val localIconUri: Uri? =
                    try {
                        Glide.with(context).asFile()
                                .load(description.iconUri)
                                .submit(imgWidth, imgHeight)
                                .get()
                                .asAlbumArtContentUri()
                    } catch (e: Exception) {
                        Logger.instance.e(TAG, e)
                        null
                    }

            val cookie = description.extras?.getString(MEDIA_DESCRIPTION_EXTRAS_MEDIA_COOKIE)

            preferences.edit {
                putString(RECENT_SONG_MEDIA_ID_KEY, description.mediaId)
                putString(RECENT_SONG_TITLE_KEY, description.title.toString())
                putString(RECENT_SONG_SUBTITLE_KEY, description.subtitle.toString())
                putString(RECENT_SONG_ICON_URI_KEY, localIconUri.toString())
                putString(RECENT_SONG_MEDIA_URL, description.mediaUri.toString())
                putString(RECENT_SONG_COOKIE, cookie)
                putLong(RECENT_SONG_POSITION_KEY, position)
            }
        }
    }

    fun loadRecentSong(): MediaBrowserCompat.MediaItem? {
        val mediaId = preferences.getString(RECENT_SONG_MEDIA_ID_KEY, null) ?: return null

        val position = preferences.getLong(RECENT_SONG_POSITION_KEY, 0L)
        val title = preferences.getString(RECENT_SONG_TITLE_KEY, null) ?: ""
        val subtitle = preferences.getString(RECENT_SONG_SUBTITLE_KEY, null) ?: ""
        val iconUrl = preferences.getString(RECENT_SONG_ICON_URI_KEY, null) ?: ""
        val cookie = preferences.getString(RECENT_SONG_COOKIE, null) ?: ""
        val mediaUrl = preferences.getString(RECENT_SONG_MEDIA_URL, null) ?: ""

        return MediaItemMsg.createMediaItem(position, cookie, mediaId, title, subtitle, iconUrl, mediaUrl)
    }
}

private const val PREFERENCES_NAME = "flutter_video_background"
private const val RECENT_SONG_COOKIE = "recent_song_cookie"
private const val RECENT_SONG_MEDIA_URL = "recent_song_media_url"
private const val RECENT_SONG_MEDIA_ID_KEY = "recent_song_media_id"
private const val RECENT_SONG_TITLE_KEY = "recent_song_title"
private const val RECENT_SONG_SUBTITLE_KEY = "recent_song_subtitle"
private const val RECENT_SONG_ICON_URI_KEY = "recent_song_icon_uri"
private const val RECENT_SONG_POSITION_KEY = "recent_song_position"
private const val TAG = "PersistentStorage"