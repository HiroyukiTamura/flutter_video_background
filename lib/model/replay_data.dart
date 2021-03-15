import 'package:freezed_annotation/freezed_annotation.dart';

part 'replay_data.freezed.dart';

@freezed
class ReplyData with _$ReplyData {
  const factory ReplyData({
    required bool wasPlaying,
    Duration? position,
  }) = _ReplyData;

  factory ReplyData.fromReply(dynamic map) {
    final positionStr = map['position'] as String?;
    final wasPlayingStr = map['wasPlaying'] as String?;

    return ReplyData(
      position: Duration(
        milliseconds: positionStr?.toPositiveInt() ?? 0,
      ),
      wasPlaying: wasPlayingStr?.toBool() ?? false,
    );
  }
}

extension on String {
  bool toBool() {
    if (this == true.toString())
      return true;
    else if (this == false.toString())
      return false;
    else
      throw ArgumentError.value(this);
  }

  int toPositiveInt() {
    final position = int.parse(this);
    return position.isNegative ? 0 : position;
  }
}
