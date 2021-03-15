// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides

part of 'replay_data.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more informations: https://github.com/rrousselGit/freezed#custom-getters-and-methods');

/// @nodoc
class _$ReplyDataTearOff {
  const _$ReplyDataTearOff();

  _ReplyData call({required bool wasPlaying, Duration? position}) {
    return _ReplyData(
      wasPlaying: wasPlaying,
      position: position,
    );
  }
}

/// @nodoc
const $ReplyData = _$ReplyDataTearOff();

/// @nodoc
mixin _$ReplyData {
  bool get wasPlaying => throw _privateConstructorUsedError;
  Duration? get position => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ReplyDataCopyWith<ReplyData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ReplyDataCopyWith<$Res> {
  factory $ReplyDataCopyWith(ReplyData value, $Res Function(ReplyData) then) =
      _$ReplyDataCopyWithImpl<$Res>;
  $Res call({bool wasPlaying, Duration? position});
}

/// @nodoc
class _$ReplyDataCopyWithImpl<$Res> implements $ReplyDataCopyWith<$Res> {
  _$ReplyDataCopyWithImpl(this._value, this._then);

  final ReplyData _value;
  // ignore: unused_field
  final $Res Function(ReplyData) _then;

  @override
  $Res call({
    Object? wasPlaying = freezed,
    Object? position = freezed,
  }) {
    return _then(_value.copyWith(
      wasPlaying: wasPlaying == freezed
          ? _value.wasPlaying
          : wasPlaying // ignore: cast_nullable_to_non_nullable
              as bool,
      position: position == freezed
          ? _value.position
          : position // ignore: cast_nullable_to_non_nullable
              as Duration?,
    ));
  }
}

/// @nodoc
abstract class _$ReplyDataCopyWith<$Res> implements $ReplyDataCopyWith<$Res> {
  factory _$ReplyDataCopyWith(
          _ReplyData value, $Res Function(_ReplyData) then) =
      __$ReplyDataCopyWithImpl<$Res>;
  @override
  $Res call({bool wasPlaying, Duration? position});
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
    Object? wasPlaying = freezed,
    Object? position = freezed,
  }) {
    return _then(_ReplyData(
      wasPlaying: wasPlaying == freezed
          ? _value.wasPlaying
          : wasPlaying // ignore: cast_nullable_to_non_nullable
              as bool,
      position: position == freezed
          ? _value.position
          : position // ignore: cast_nullable_to_non_nullable
              as Duration?,
    ));
  }
}

/// @nodoc
class _$_ReplyData implements _ReplyData {
  const _$_ReplyData({required this.wasPlaying, this.position});

  @override
  final bool wasPlaying;
  @override
  final Duration? position;

  @override
  String toString() {
    return 'ReplyData(wasPlaying: $wasPlaying, position: $position)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other is _ReplyData &&
            (identical(other.wasPlaying, wasPlaying) ||
                const DeepCollectionEquality()
                    .equals(other.wasPlaying, wasPlaying)) &&
            (identical(other.position, position) ||
                const DeepCollectionEquality()
                    .equals(other.position, position)));
  }

  @override
  int get hashCode =>
      runtimeType.hashCode ^
      const DeepCollectionEquality().hash(wasPlaying) ^
      const DeepCollectionEquality().hash(position);

  @JsonKey(ignore: true)
  @override
  _$ReplyDataCopyWith<_ReplyData> get copyWith =>
      __$ReplyDataCopyWithImpl<_ReplyData>(this, _$identity);
}

abstract class _ReplyData implements ReplyData {
  const factory _ReplyData({required bool wasPlaying, Duration? position}) =
      _$_ReplyData;

  @override
  bool get wasPlaying => throw _privateConstructorUsedError;
  @override
  Duration? get position => throw _privateConstructorUsedError;
  @override
  @JsonKey(ignore: true)
  _$ReplyDataCopyWith<_ReplyData> get copyWith =>
      throw _privateConstructorUsedError;
}
