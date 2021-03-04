package com.example.flutter_video_background

import android.support.v4.media.session.PlaybackStateCompat
import com.example.flutter_video_background.extensions.error
import com.example.flutter_video_background.extensions.id
import com.example.flutter_video_background.extensions.successNormal
import com.example.flutter_video_background.extensions.successReplyVideoPosition
import com.example.flutter_video_background.model.MethodChannelArg
import com.example.flutter_video_background.model.MethodErrorCode
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class ChannelClient : MethodChannel.MethodCallHandler {

    var mConnection: MusicServiceConnection? = null

    fun initEventChannel(messenger: BinaryMessenger) {
        MethodChannel(messenger, CHANNEL_NAME)
                .setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        mConnection?.also { connection ->
            when (call.method) {
                START_BACKGROUND -> {
                    if (!validateConnection(connection, result))
                        return

                    val arg = MethodChannelArg.StartBackground(call)

                    if (connection.playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING
                            && connection.nowPlaying.value?.id == arg.id) {
                        result.successNormal()
                        return
                    }

                    val bundle = arg.toBundle()
                    connection.transportControls.playFromMediaId(arg.id, bundle)
                    result.successNormal()
                }
                STOP_BACKGROUND -> {
                    if (!validateConnection(connection, result))
                        return

                    val wasStatePlaying = connection.isStatePlaying // get value before stop player
                    val hideNtf = MethodChannelArg.StopForeground(call).hideNotification
                    connection.transportControls.run {
                        if (hideNtf)
                            stop()
                        else
                            pause()
                    }

                    result.successReplyVideoPosition(connection.currentPosition, wasStatePlaying)
                }
                else ->
                    result.notImplemented()
            }
        } ?: run {
            result.error(MethodErrorCode.SERVICE_IS_NOT_INITIALIZED)
        }
    }

    private fun validateConnection(connection: MusicServiceConnection, result: MethodChannel.Result): Boolean {
        if (connection.isConnected.value != true) {
            result.error(MethodErrorCode.SERVICE_IS_NOT_CONNECTED)
            return false
        }
        return true
    }
}

private const val CHANNEL_NAME = "flutter_video_background/main"
private const val START_BACKGROUND = "start_background"
private const val STOP_BACKGROUND = "stop_background"