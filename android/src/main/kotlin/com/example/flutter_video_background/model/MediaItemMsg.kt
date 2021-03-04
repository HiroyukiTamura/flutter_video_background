package com.example.flutter_video_background.model

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.example.flutter_video_background.extensions.*
import com.google.android.exoplayer2.C

class MediaItemMsg private constructor(val cookie: String?, val playbackStartPositionMs: Long, val media: MediaMetadataCompat) {

    constructor(extras: Bundle) : this(
            cookie = extras.getString(MEDIA_DESCRIPTION_EXTRAS_MEDIA_COOKIE, null),
            playbackStartPositionMs =
            extras.getLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, C.TIME_UNSET),
            media = bundle2MediaMetaData(extras),
    )

    companion object {

        const val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"
        const val MEDIA_DESCRIPTION_EXTRAS_MEDIA_COOKIE = "playback_media_cookie"

        private fun bundle2MediaMetaData(bundle: Bundle): MediaMetadataCompat =
                MediaMetadataCompat.Builder()
                        .apply {
                            id = bundle.getString(KEY_MEDIA_ID)!!
                            title = bundle.getString(KEY_TITLE)
                            displayTitle = bundle.getString(KEY_TITLE)
                            displaySubtitle = bundle.getString(KEY_SUBTITLE)
                            displayIconUri = bundle.getString(KEY_ICON_URI)
                            mediaUri = bundle.getString(KEY_MEDIA_URL)
                        }
                        .build()

        fun createBundle(
                id: String,
                displayTitle: String?,
                displaySubtitle: String?,
                displayIconUri: String?,
                mediaUri: String,
                cookie: String?,
                playbackStartPositionMs: Long?,
        ): Bundle =
                bundleOf(
                        KEY_MEDIA_ID to id,
                        KEY_TITLE to displayTitle,
                        KEY_SUBTITLE to displaySubtitle,
                        KEY_ICON_URI to displayIconUri,
                        KEY_MEDIA_URL to mediaUri,
                        MEDIA_DESCRIPTION_EXTRAS_MEDIA_COOKIE to cookie,
                        MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS to playbackStartPositionMs)

        fun createMediaItem(
                position: Long,
                cookie: String,
                mediaId: String,
                title: String,
                subtitle: String,
                iconUrl: String,
                mediaUrl: String
        ): MediaBrowserCompat.MediaItem {
            val extras = createBundle(mediaId, title, subtitle, iconUrl, mediaUrl, cookie, position)

            val description = MediaDescriptionCompat.Builder()
                    .setMediaUri(mediaUrl.toUri())
                    .setMediaId(mediaId)
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setIconUri(iconUrl.toUri())
                    .setExtras(extras)
                    .build()

            return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
        }
    }
}

private const val KEY_MEDIA_URL = "media_url"
private const val KEY_MEDIA_ID = "media_id"
private const val KEY_TITLE = "title"
private const val KEY_SUBTITLE = "subtitle"
private const val KEY_ICON_URI = "icon_uri"