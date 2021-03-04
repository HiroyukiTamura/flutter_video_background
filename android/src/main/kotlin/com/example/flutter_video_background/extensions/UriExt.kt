package com.example.flutter_video_background.extensions

import android.net.Uri

inline val Uri.isHlsUrl: Boolean
    get() = toString().endsWith("m3u8", true) || toString().endsWith("m3u")