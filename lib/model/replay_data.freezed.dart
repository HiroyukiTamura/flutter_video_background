// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies

part of 'replay_data.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

/// @nodoc
class _$ReplyDataTearOff {
  const _$ReplyDataTearOff();

// ignore: unused_element
  _ReplyData call({@required Duration position, @required bool wasPlaying}) {
    return _ReplyData(
      position: position,
      wasPlaying: wasPlaying,
    );
  }
}

/// @nodoc
// ignore: unused_element
const $ReplyData = _$ReplyDataTearOff();

/// @nodoc
mixin _$ReplyData {
  Duration get position;
  bool get wasPlaying;

  @JsonKey(ignore: true)
  $ReplyDataCopyWith<ReplyData> get copyWith;
}

/// @nodoc
abstract class $ReplyDataCopyWith<$Res> {
  factory $ReplyDataCopyWith(ReplyData value, $Res Function(ReplyData) then) =
      _$ReplyDataCopyWithImpl<$Res>;
  $Res call({Duration position, bool wasPlaying});
}

/// @nodoc
class _$ReplyDataCopyWithImpl<$Res> implements $ReplyDataCopyWith<$Res> {
  _$ReplyDataCopyWithImpl(this._value, this._then);

  final ReplyData _value;
  // ignore: unused_field
  final $Res Function(ReplyData) _then;

  @override
  $Res call({
    Object position = freezed,
    Object wasPlaying = freezed,
  }) {
    return _then(_value.copyWith(
      position: position == freezed ? _value.position : position as Duration,
      wasPlaying:
          wasPlaying == freezed ? _value.wasPlaying : wasPlaying as bool,
    ));
  }
}

/// @nodoc
abstract class _$ReplyDataCopyWith<$Res> implements $ReplyDataCopyWith<$Res> {
  factory _$ReplyDataCopyWith(
          _ReplyData value, $Res Function(_ReplyData) then) =
      __$ReplyDataCopyWithImpl<$Res>;
  @override
  $Res call({Duration position, bool wasPlaying});
}

/// @nodoc
class __$ReplyDataCopyWithImpl<$Res> extends _$ReplyDataCopyWithImpl<$Res>
    implements _$ReplyDataCopyWith<$Res> {
  __$ReplyDataCopyWithImpl(_ReplyData _value, $Res Function(_ReplyData) _then)
      : super(_value, (v) => _then(v as _ReplyData));

  @override
  _ReplyData get _value => super._value as _ReplyData;

  @override
  $Res call({
    Object position = freezed,
    Object wasPlaying = freezed,
  }) {
    return _then(_ReplyData(
      position: position == freezed ? _value.position : position as Duration,
      wasPlaying:
          wasPlaying == freezed ? _value.wasPlaying : wasPlaying as bool,
    ));
  }
}

/// @nodoc
class _$_ReplyData implements _ReplyData {
  const _$_ReplyData({@required this.position, @required this.wasPlaying})
      : assert(position != null),
        assert(wasPlaying != null);

  @override
  final Duration position;
  @override
  final bool wasPlaying;

  @override
  String toString() {
    return 'ReplyData(position: $position, wasPlaying: $wasPlaying)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other is _ReplyData &&
            (identical(other.position, position) ||
                const DeepCollectionEquality()
                    .equals(other.position, position)) &&
            (identical(other.wasPlaying, wasPlaying) ||
                const DeepCollectionEquality()
                    .equals(other.wasPlaying, wasPlaying)));
  }

  @override
  int get hashCode =>
      runtimeType.hashCode ^
      const DeepCollectionEquality().hash(position) ^
      const DeepCollectionEquality().hash(wasPlaying);

  @JsonKey(ignore: true)
  @override
  _$ReplyDataCopyWith<_ReplyData> get copyWith =>
      __$ReplyDataCopyWithImpl<_ReplyData>(this, _$identity);
}

abstract class _ReplyData implements ReplyData {
  const factory _ReplyData(
      {@required Duration position, @required bool wasPlaying}) = _$_ReplyData;

  @override
  Duration get position;
  @override
  bool get wasPlaying;
  @override
  @JsonKey(ignore: true)
  _$ReplyDataCopyWith<_ReplyData> get copyWith;
}
