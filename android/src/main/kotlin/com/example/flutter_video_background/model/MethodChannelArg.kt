package com.example.flutter_video_background.model

import android.os.Bundle
import io.flutter.plugin.common.MethodCall
import kotlin.collections.HashMap


abstract class MethodChannelArg(methodCall: MethodCall) {
    protected val defaultMap = methodCall.arguments<HashMap<String, Any?>>()
            .withDefault { null }

    /**
     * [position] : handle position as String because dart has int type only.
     * @see [StartBackground.position]
     */
    class StartBackground(
            methodCall: MethodCall
    ) : MethodChannelArg(methodCall) {

        val id: String by defaultMap
        private val mediaUrl: String by defaultMap
        private val title: String? by defaultMap
        private val subtitle: String? by defaultMap
        private val position: String? by defaultMap
        private val iconUrl: String? by defaultMap
        private val cookie: String? by defaultMap

        fun toBundle(): Bundle =
                MediaItemMsg.createBundle(id, title, subtitle, iconUrl, mediaUrl, cookie, position?.toLongOrNull())
    }

    class StopForeground(methodCall: MethodCall) : MethodChannelArg(methodCall) {
        val hideNotification : Boolean by defaultMap
    }
}

class MethodReplyArg private constructor(val map: Map<String, String>) {

    /**
     * [position] : handle position as String because dart has int type only.
     * @see [MethodChannelArg.position]
     */
    constructor(position: Long, wasPlaying: Boolean) : this(mapOf(
            "position" to position.toString(),
            "wasPlaying" to wasPlaying.toString())
    ) {
        require(0 <= position)
    }
}