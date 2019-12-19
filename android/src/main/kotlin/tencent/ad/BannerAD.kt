package tencent.ad

import com.qq.e.ads.banner2.UnifiedBannerADListener
import com.qq.e.ads.banner2.UnifiedBannerView
import com.qq.e.comm.util.AdError
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.platform.PlatformView

/**
 * @param id 调用次数
 * @param messenger 二进制异步消息通信器
 * */
class BannerAD(messenger: BinaryMessenger,
               id: Int,
               params: Map<String, Any>
) : PlatformView, MethodCallHandler, UnifiedBannerADListener {
    private val posId = "${params["posId"]}"
    private val bannerView = UnifiedBannerView(TencentAD.activity, O.APP_ID, posId, this)
    private val methodChannel = MethodChannel(messenger, "${O.BANNER_AD_ID}_$id")

    init {
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView() = bannerView

    override fun dispose() {
        methodChannel.setMethodCallHandler(null)
        bannerView.destroy()
    }

    override fun onMethodCall(methodCall: MethodCall, result: Result) =
            when (methodCall.method) {
                "loadAD" -> {
                    bannerView.loadAD()
                    result.success(true)
                }
                "destroy" -> {
                    bannerView.destroy()
                    result.success(true)
                }
                else -> result.notImplemented()
            }

    override fun onNoAD(e: AdError) = methodChannel.invokeMethod("onNoAD", null)
    override fun onADReceive() = methodChannel.invokeMethod("onADReceive", null)
    override fun onADExposure() = methodChannel.invokeMethod("onADExposure", null)
    override fun onADClosed() = methodChannel.invokeMethod("onADClosed", null)
    override fun onADClicked() = methodChannel.invokeMethod("onADClicked", null)
    override fun onADLeftApplication() = methodChannel.invokeMethod("onADLeftApplication", null)
    override fun onADOpenOverlay() = methodChannel.invokeMethod("onADOpenOverlay", null)
    override fun onADCloseOverlay() = methodChannel.invokeMethod("onADCloseOverlay", null)
}