import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:flutter_video_background/model/replay_data.dart';

class FlutterVideoBackground {
  static const MethodChannel _CHANNEL =
      MethodChannel('flutter_video_background/main');

  static Future<void> startPlayBackGround({
    @required String url,
    @required bool isLiveStream,
    String position,
    String iconUrl,
    String cookie,
    String title,
    String subtitle,
  }) async {
    final positionSafe =
        int.tryParse(position)?.isNegative != false ? 0 : position;
    final args = {
      'id': url,
      'mediaUrl': url,
      'title': title,
      'subtitle': subtitle,
      'isLiveStream': isLiveStream,
      'position': positionSafe.toString(),
      'iconUrl': iconUrl,
      'cookie': cookie,
    };
    await _CHANNEL.invokeMethod(_MethodName.START_BACKGROUND, args);
  }

  static Future<ReplyData> stopBackGround(bool hideNotification) async {
    final args = {'hideNotification': hideNotification};
    final result =
        await _CHANNEL.invokeMethod(_MethodName.STOP_BACKGROUND, args);

    return ReplyData.fromReply(result);
  }
}

class _MethodName {
  const _MethodName._();

  static const START_BACKGROUND = 'start_background';
  static const STOP_BACKGROUND = 'stop_background';
}
