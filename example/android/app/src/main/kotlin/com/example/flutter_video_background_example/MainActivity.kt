package com.example.flutter_video_background_example

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import com.example.flutter_video_background.*
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity(), LoggerListener {

    private val channelClient = ChannelClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        Logger.instance.initialize(this)
        channelClient.mConnection = MusicServiceConnection.getInstance(this.application, ComponentName(this, MusicService::class.java))
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        channelClient.initEventChannel(flutterEngine.dartExecutor.binaryMessenger)
        EventChannelClient.instance.initEventChannel(flutterEngine.dartExecutor.binaryMessenger)
    }

    override fun d(tag: String, msg: Any?) {
        Log.d(tag, msg.toString())
    }

    override fun e(tag: String, msg: String?) {
        Log.e(tag, msg.toString())
    }

    override fun e(tag: String, thr: Throwable) {
        Log.e(tag, "", thr)
    }
}
