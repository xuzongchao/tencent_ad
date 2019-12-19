import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:tencent_ad/export.dart';
import 'package:flutter/services.dart';

final config = defaultTargetPlatform == TargetPlatform.iOS
    ? {
        'appID': '1105344611',
        'bannerID': '1080958885885321',
        'nativeID': '1020922903364636',
        'intersID': '1050652855580392',
        'splashID': '9040714184494018',
        'bgPic': 'LaunchImage'
      }
    : {
        'appID': '1101152570',
        'bannerID': '4080052898050840',
        'nativeID': '2000629911207832',
        'intersID': '3040652898151811',
        'splashID': '8863364436303842593',
        'bgPic': 'tencent.ad_example:mipmap/splash_img'
      };

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    TencentAD.config(appID: config['appID'], phoneSTAT: 0, fineLOC: 0).then(
        (_) => SplashAd(config['splashID'], bgPic: config['bgPic']).showAd());
  } on PlatformException {}
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _adClosed = false;
  GlobalKey<UnifiedBannerAdState> _adKey = GlobalKey();
  IntersAD _intersAD;

  void _adEventCallback(BannerEvent event, dynamic arguments) {
    if (event == BannerEvent.onAdClosed) {
      if (this.mounted) {
        this.setState(() {
          _adClosed = true;
        });
      }
    }
  }

  Future<void> _intersADCallback(IntersEvent event, dynamic args) async {
    if (event == IntersEvent.onADReceive) {
      _intersAD.loadAD();
    }
  }

  @override
  void initState() {
    _intersAD = IntersAD(config['intersID'], callback: _intersADCallback);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(),
        body: ListView(
          children: <Widget>[
            Column(
              children: <Widget>[
                Container(
                  height: _adClosed ? 0 : UnifiedBannerAd.ratio,
                  child: _adClosed
                      ? Container()
                      : UnifiedBannerAd(config['bannerID'],
                          key: _adKey,
                          adEventCallback: _adEventCallback,
                          refreshOnCreate: true),
                ),
                Row(
                  children: <Widget>[
                    RaisedButton(
                      onPressed: () {
                        this.setState(() => this._adClosed = false);
                        _adKey.currentState?.loadAD();
                      },
                      child: Text('刷新横幅'),
                    ),
                    RaisedButton(
                      onPressed: () async {
                        await _adKey.currentState?.closeAD();
                        if (this.mounted) {
                          this.setState(() => _adClosed = true);
                        }
                      },
                      child: Text('关闭横幅'),
                    ),
                  ],
                ),
              ],
            ),
            SizedBox(
              height: 500.0,
              child: NativeExpress(),
            ),
            Row(
              children: <Widget>[
                RaisedButton(
                  child: Text('加载插屏'),
                  onPressed: () {
                    _intersAD.loadAD();
                  },
                ),
                RaisedButton(
                  child: Text('关闭插屏'),
                  onPressed: () {
                    _intersAD.closeAD();
                  },
                ),
              ],
            )
          ],
        ),
      ),
    );
  }
}

class NativeExpress extends StatefulWidget {
  @override
  _NativeExpressState createState() => _NativeExpressState();
}

class _NativeExpressState extends State<NativeExpress> {
  double adHeight;

  bool adRemoved = false;

  GlobalKey<NativeExpressAdState> _adKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ListView(
        children: <Widget>[
          Row(
            children: <Widget>[
              RaisedButton(
                onPressed: () {
                  setState(() {
                    adHeight = null;
                    adRemoved = false;
                  });
                  _adKey.currentState?.refreshAd();
                },
                child: Text('刷新消息流'),
              ),
              RaisedButton(
                onPressed: () async {
                  await _adKey.currentState?.closeAd();
                  if (this.mounted) {
                    this.setState(() {
                      adRemoved = true;
                      adHeight = null;
                    });
                  }
                },
                child: Text('关闭消息流'),
              ),
            ],
          ),
          adRemoved
              ? Container()
              : Container(
                  height: adHeight == null ? 1 : adHeight,
                  child: NativeExpressAd(
                    key: _adKey,
                    posId: config['nativeID'],
                    callback: _adEventCallback,
                    refreshOnCreate: true,
                  ),
                ),
          Container(
            height: 200.0,
            color: Colors.accents[0],
          ),
          NativeExpressAdWidget(config['nativeID']),
        ],
      ),
    );
  }

  void _adEventCallback(NativeADEvent event, dynamic arguments) async {
    if (event == NativeADEvent.onLayoutChange && this.mounted) {
      this.setState(() {
        // 根据选择的广告位模板尺寸计算，这里是1280x720
        adHeight = MediaQuery.of(context).size.width *
            arguments['height'] /
            arguments['width'];
      });
      return;
    }
    if (event == NativeADEvent.onADClosed) {
      this.setState(() {
        adRemoved = true;
      });
    }
  }
}
