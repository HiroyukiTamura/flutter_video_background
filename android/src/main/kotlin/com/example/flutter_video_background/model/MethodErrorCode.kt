package com.example.flutter_video_background.model

enum class MethodErrorCode(val code: String) {
    SERVICE_IS_NOT_CONNECTED("service_is_not_connected"), SERVICE_IS_NOT_INITIALIZED("service_is_not_initialized"),
}

enum class MethodSuccessCode(val code: String) {
    NO_CHANGE("no_change"), SUCCESS("success")
}