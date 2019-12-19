import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'o.dart';

enum BannerEvent {
  onNoAD,
  onAdReceived,
  onAdExposure,
  onAdClosed,
  onAdClicked,
  onAdLeftApplication,
  onAdOpenOverlay,
  onAdCloseOverlay,
}

typedef BannerCallback = Function(BannerEvent event, dynamic args);

class UnifiedBannerAd extends StatefulWidget {
  UnifiedBannerAd(
    this.posId, {
    Key key,
    this.adEventCallback,
    this.refreshOnCreate,
  }) : super(key: key);
  static final double ratio = 64; // 宽高比
  final String posId;
  final BannerCallback adEventCallback;
  final bool refreshOnCreate;

  @override
  UnifiedBannerAdState createState() => UnifiedBannerAdState();
}

class UnifiedBannerAdState extends State<UnifiedBannerAd> {
  MethodChannel _methodChannel;

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: '$BANNER_AD_ID',
        onPlatformViewCreated: _onPlatformViewCreated,
        creationParams: {'posId': widget.posId},
        creationParamsCodec: StandardMessageCodec(),
      );
    }
    return AndroidView(
      viewType: '$BANNER_AD_ID',
      onPlatformViewCreated: _onPlatformViewCreated,
      creationParams: {'posId': widget.posId},
      creationParamsCodec: StandardMessageCodec(),
    );
  }

  void _onPlatformViewCreated(int id) {
    this._methodChannel = MethodChannel('$BANNER_AD_ID\_$id');
    this._methodChannel.setMethodCallHandler(_handleMethodCall);
    if (this.widget.refreshOnCreate == true) {
      this.loadAD();
    }
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    if (widget.adEventCallback != null) {
      BannerEvent event;
      switch (call.method) {
        case 'onNoAD':
          event = BannerEvent.onNoAD;
          break;
        case 'onAdReceived':
          event = BannerEvent.onAdReceived;
          break;
        case 'onAdExposure':
          event = BannerEvent.onAdExposure;
          break;
        case 'onAdClosed':
          event = BannerEvent.onAdClosed;
          break;
        case 'onAdClicked':
          event = BannerEvent.onAdClicked;
          break;
        case 'onAdLeftApplication':
          event = BannerEvent.onAdLeftApplication;
          break;
        case 'onAdOpenOverlay':
          event = BannerEvent.onAdOpenOverlay;
          break;
        case 'onAdCloseOverlay':
          event = BannerEvent.onAdCloseOverlay;
          break;
      }
      widget.adEventCallback(event, call.arguments);
    }
  }

  Future<void> closeAD() async => await _methodChannel.invokeMethod('destroy');
  Future<void> loadAD() async => await _methodChannel.invokeMethod('loadAD');
}
