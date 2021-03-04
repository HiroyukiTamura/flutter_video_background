package com.example.flutter_video_background

import android.util.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel

class EventChannelClient : EventChannel.StreamHandler {

    private var eventSink: EventChannel.EventSink? = null

    fun initEventChannel(messenger: BinaryMessenger) {
        val channel = EventChannel(messenger, CHANNEL_NAME)
        channel.setStreamHandler(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        eventSink = events
        Logger.instance.d(TAG, "EventChannel onListen called")
    }

    override fun onCancel(arguments: Any?) {
        Logger.instance.e(TAG, "EventChannel onCancel called")
        eventSink = null
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var mInstance: EventChannelClient? = null

        val instance: EventChannelClient
            get() =
                mInstance ?: synchronized(this) {
                    mInstance ?: EventChannelClient()
                            .also { mInstance = it }
                }
    }
}

private const val TAG = "EventChannelClient"
private const val CHANNEL_NAME = "flutter_video_background"