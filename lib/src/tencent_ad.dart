import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/material.dart';

import 'o.dart';

class TencentAD {
  static const MethodChannel channel = const MethodChannel(PLUGIN_ID);

  /// [appID] 来自 `https://adnet.qq.com`
  /// [phoneSTAT] 手机状态权限码: 0:未授权, 1:已授权, 2:已禁用
  /// [fineLOC] 获取定位权限码: 0:未授权, 1:已授权, 2:已禁用
  static Future<bool> config({
    @required String appID,
    int phoneSTAT: 0,
    int fineLOC: 0,
  }) async =>
      await channel.invokeMethod('config', {
        'appID': appID,
        'phoneSTAT': phoneSTAT,
        'fineLOC': fineLOC,
      });

  /// 创建一个插屏广告，并通过渠道ID`PLUGIN_ID`/inters`posId` 与其进行通信
  static Future<bool> createIntersAD({@required String posID}) async =>
      await channel.invokeMethod('createIntersAD', {'posID': posID});
}
