package com.example.flutter_video_background.extensions

import com.example.flutter_video_background.model.MethodErrorCode
import com.example.flutter_video_background.model.MethodReplyArg
import io.flutter.plugin.common.MethodChannel

fun MethodChannel.Result.successNormal() = success(null)

/**
 * [position] must not be negative.
 */
fun MethodChannel.Result.successReplyVideoPosition(position: Long, wasPlaying: Boolean) =
        success(MethodReplyArg(position, wasPlaying).map)

fun MethodChannel.Result.error(code: MethodErrorCode) = error(code.code, null, null)