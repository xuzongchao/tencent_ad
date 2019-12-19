import 'package:flutter/services.dart';
import 'tencent_ad.dart';
import 'o.dart';

enum SplashAdEvent {
  onNoAd,
  onAdDismiss,
  onAdClosed,
  onAdPresent,
  onAdExposure,
}

typedef SplashAdEventCallback = Function(
    SplashAdEvent event, dynamic arguments);

class SplashAd {
  final String posId;
  final String bgPic;

  final SplashAdEventCallback callBack;

  MethodChannel _methodChannel;

  SplashAd(this.posId, {this.bgPic, this.callBack}) {
    this._methodChannel = MethodChannel(SPLASH_AD_ID);
    this._methodChannel.setMethodCallHandler(_handleMethodCall);
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    if (callBack != null) {
      SplashAdEvent event;
      switch (call.method) {
        case 'onNoAd':
          event = SplashAdEvent.onNoAd;
          break;
        case 'onAdDismiss':
          event = SplashAdEvent.onAdDismiss;
          break;
        case 'onAdClosed':
          event = SplashAdEvent.onAdClosed;
          break;
        case 'onAdPresent':
          event = SplashAdEvent.onAdPresent;
          break;
        case 'onAdExposure':
          event = SplashAdEvent.onAdExposure;
          break;
      }
      callBack(event, call.arguments);
    }
  }

  Future<void> showAd() async {
    await TencentAD.channel.invokeMethod('showSplash', {
      'posId': posId,
      'bgPic': bgPic,
    });
  }

  Future<void> closeAd() async {
    await TencentAD.channel.invokeMethod('closeSplash');
  }
}
