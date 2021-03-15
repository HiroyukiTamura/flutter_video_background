import 'package:better_player/better_player.dart';
import 'package:flutter/material.dart';
import 'package:flutter_video_background/flutter_video_background.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  static const _MEDIA_URL =
      'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance?.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance?.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.inactive:
        try {
          FlutterVideoBackground.startPlayBackGround(
            url: _MEDIA_URL,
            isLiveStream: false,
            title: 'title',
            subtitle: 'subtitle',
            iconUrl: 'https://d27ea4kkb8flj9.cloudfront.net/122873_1_L.jpg',
          );
        } catch (e) {
          print(e);
        }
        break;
      case AppLifecycleState.paused:
      case AppLifecycleState.detached:
        break;
      case AppLifecycleState.resumed:
        try {
          FlutterVideoBackground.stopBackGround(true).then((position) {
            debugPrint(position.toString());
          });
        } catch (e) {
          print(e);
        }
        break;
    }
  }

  @override
  Widget build(BuildContext context) => MaterialApp(
    home: Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: BetterPlayer.network(
        _MEDIA_URL,
        betterPlayerConfiguration: const BetterPlayerConfiguration(
          aspectRatio: 16 / 9,
        ),
      ),
    ),
  );
}
