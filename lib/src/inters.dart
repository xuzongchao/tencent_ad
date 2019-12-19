import 'package:flutter/services.dart';

import '../export.dart';
import 'o.dart';

typedef IntersADCallback = Function(IntersEvent event, dynamic arguments);
enum IntersEvent {
  onNoAD,
  onADReceive,
  onADExposure,
  onADClosed,
  onADClicked,
  onADLeftApplication,
  onADOpened,
}

class IntersAD {
  final String posID;

  final IntersADCallback callback;

  MethodChannel _methodChannel;

  IntersAD(this.posID, {this.callback}) {
    TencentAD.createIntersAD(posID: posID);
    _methodChannel = MethodChannel('$INTERS_AD_ID\_$posID');
    _methodChannel.setMethodCallHandler(_handleMethodCall);
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    if(callback != null) {
      IntersEvent event;
      switch (call.method) {
        case 'onNoAD':
          event = IntersEvent.onNoAD;
          break;
        case 'onADReceive':
          event = IntersEvent.onADReceive;
          break;
        case 'onADExposure':
          event = IntersEvent.onADExposure;
          break;
        case 'onADClosed':
          event = IntersEvent.onADClosed;
          break;
        case 'onADClicked':
          event = IntersEvent.onADClicked;
          break;
        case 'onADLeftApplication':
          event = IntersEvent.onADLeftApplication;
          break;
        case 'onADOpened':
          event = IntersEvent.onADOpened;
          break;
      }
      callback(event, call.arguments);
    }
  }

  Future<void> loadAD() async {
    await _methodChannel.invokeMethod('loadAD');
  }

  Future<void> closeAD() async {
    await _methodChannel.invokeMethod('closeAD');
  }
}