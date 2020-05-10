import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/gestures.dart';
import 'o.dart';

enum NativeADEvent {
  onLayoutChange,
  onNoAD,
  onADLoaded,
  onRenderFail,
  onRenderSuccess,
  onADExposure,
  onADClicked,
  onADClosed,
  onADLeftApplication,
  onADOpenOverlay,
  onADCloseOverlay,
}

typedef NativeADCallback = Function(NativeADEvent event, dynamic arguments);

class NativeExpressAd extends StatefulWidget {
  NativeExpressAd(
      {Key key,
      this.posId,
      this.requestCount: 5,
      this.callback,
      this.refreshOnCreate,
      this.controller,
      this.backgroundColor = "#ffffffff"})
      : super(key: key);

  final String posId;
  final int requestCount; // 广告计数请求，默认值是5
  final NativeADCallback callback;
  final bool refreshOnCreate;
  final NativeAdController controller;
  final String backgroundColor;

  @override
  NativeExpressAdState createState() => NativeExpressAdState();
}

class NativeExpressAdState extends State<NativeExpressAd> {
  MethodChannel _methodChannel;
  double _height = 0;
  String oldBgColor = "";
  bool inited = false;
  @override
  void initState() {
    super.initState();
    if (this.widget.controller != null) {
      this.widget.controller.widgetState = this;
    }
  }

  @override
  Widget build(BuildContext context) {
    //start-setbg
    //这里判断是否需要重新设置广告的背景色
    Future.microtask(() async {
      await Future.doWhile(() async {
        await Future.delayed(Duration(milliseconds:10));
        return inited == false;
      });
      if (this.widget.backgroundColor != oldBgColor) {
        this.oldBgColor = this.widget.backgroundColor;
        this.setBgColor(this.widget.backgroundColor);
      }
    });
    //end-setbg

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: '$NATIVE_AD_ID',
        onPlatformViewCreated: _onPlatformViewCreated,
        creationParams: {'posId': widget.posId, 'count': widget.requestCount},
        creationParamsCodec: StandardMessageCodec(),
      );
    }
    return Container(
        height: _height,
        child: AndroidView(
          viewType: '$NATIVE_AD_ID',
          onPlatformViewCreated: _onPlatformViewCreated,
          creationParams: {'posId': widget.posId, 'count': widget.requestCount},
          creationParamsCodec: const StandardMessageCodec(),
        ));
  }

  void _onPlatformViewCreated(int id) {
    this.inited = true;
    _methodChannel = MethodChannel('$NATIVE_AD_ID\_$id');
    _methodChannel.setMethodCallHandler(_handleMethodCall);
    if (widget.refreshOnCreate == true) {
      refreshAd();
    }
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    if (widget.callback != null) {
      NativeADEvent event;
      switch (call.method) {
        case 'onLayoutChange':
          if (event == NativeADEvent.onLayoutChange && mounted) {
            setState(() {
              _height = MediaQuery.of(context).size.width *
                  call.arguments['height'] /
                  call.arguments['width'];
            });
            return;
          }
          event = NativeADEvent.onLayoutChange;
          break;
        case 'onNoAD':
          event = NativeADEvent.onNoAD;
          break;
        case 'onADLoaded':
          event = NativeADEvent.onADLoaded;
          break;
        case 'onRenderFail':
          event = NativeADEvent.onRenderFail;
          break;
        case 'onRenderSuccess':
          event = NativeADEvent.onRenderSuccess;
          break;
        case 'onADExposure':
          event = NativeADEvent.onADExposure;
          break;
        case 'onADClicked':
          event = NativeADEvent.onADClicked;
          break;
        case 'onADClosed':
          event = NativeADEvent.onADClosed;
          break;
        case 'onADLeftApplication':
          event = NativeADEvent.onADLeftApplication;
          break;
        case 'onADOpenOverlay':
          event = NativeADEvent.onADOpenOverlay;
          break;
        case 'onADCloseOverlay':
          event = NativeADEvent.onADCloseOverlay;
          break;
      }
      widget.callback(event, call.arguments);
    }
  }

  Future<void> show() async => await _methodChannel.invokeMethod('show');

  Future<void> closeAd() async => await _methodChannel.invokeMethod('close');

  Future<void> refreshAd() async => await _methodChannel.invokeMethod('refresh');

  Future<void> setBgColor(String clr) async => await _methodChannel.invokeMethod('setBgColor', clr);
}

// class NativeExpressAdWidget extends StatefulWidget {
//   final String posId;
//   final int requestCount;
//   final GlobalKey<NativeExpressAdState> adKey;
//   final NativeADCallback adEventCallback;
//   final double loadingHeight;

//   NativeExpressAdWidget(
//     this.posId, {
//     GlobalKey<NativeExpressAdState> adKey,
//     this.requestCount,
//     this.adEventCallback,
//     this.loadingHeight: 1.0,
//   }) : adKey = adKey ?? GlobalKey();

//   @override
//   NativeExpressAdWidgetState createState() =>
//       NativeExpressAdWidgetState(height: loadingHeight);
// }

// class NativeExpressAdWidgetState extends State<NativeExpressAdWidget> {
//   double _height;
//   NativeExpressAd _nativeAD;

//   NativeExpressAdWidgetState({double height}) : _height = height;

//   @override
//   void initState() {
//     super.initState();
//     _nativeAD = NativeExpressAd(
//       key: widget.adKey,
//       posId: widget.posId,
//       requestCount: widget.requestCount,
//       callback: _adEventCallback,
//       refreshOnCreate: false,
//     );
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Container(
//       height: _height,
//       child: _nativeAD,
//     );
//   }

// }

class NativeAdController {
  NativeExpressAdState widgetState = null;

  void showAd() {
    widgetState.show();
  }

  void refreshAd() {
    widgetState.refreshAd();
  }

  void closeAd() {
    widgetState.closeAd();
  }

  void setBgColor(String color){
    widgetState.setBgColor(color);
  }
}
