package tencent.ad

import android.util.Log
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener
import com.qq.e.comm.util.AdError
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import tencent.ad.O.APP_ID
import tencent.ad.O.TAG
import tencent.ad.TencentAD.Companion.activity

class IntersAD(private val posId: String, messenger: BinaryMessenger) :
        MethodCallHandler, UnifiedInterstitialADListener {
    private var intersAD: UnifiedInterstitialAD? = null
    private val methodChannel = MethodChannel(messenger, O.INTERS_AD_ID + "_" + posId)

    init {
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(methodCall: MethodCall, result: Result) {
        when (methodCall.method) {
            "loadAD" -> {
                Log.i(TAG, "IntersAD onMethodCall: ")
                intersAD = getIAD()
                setVideoOption()
                intersAD?.loadAD()
                intersAD?.show()
                result.success(true)
            }
            "closeAD" -> {
                closeAd()
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    private fun getIAD(): UnifiedInterstitialAD? {
        if (intersAD != null) {
            intersAD!!.close()
            intersAD!!.destroy()
            intersAD = null
        }
        intersAD = UnifiedInterstitialAD(activity, APP_ID, posId, this)
        return intersAD
    }

    private fun setVideoOption() {
        val builder = VideoOption.Builder()
        val option = builder.build()
        builder.setAutoPlayMuted(true)
                .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI)
                .setDetailPageMuted(true)
                .build()
        intersAD?.setVideoOption(option)
        intersAD?.setVideoPlayPolicy(option.autoPlayPolicy)
    }

    fun closeAd() {
        if (intersAD != null) {
            intersAD!!.destroy()
            intersAD = null
        }
        methodChannel.setMethodCallHandler(null)
        TencentAD.removeInterstitial(posId)
    }

    override fun onNoAD(error: AdError) {
        Log.i(TAG, "IntersAD onNoAD:无广告 错误码:${error.errorCode} ${error.errorMsg}")
        intersAD = null
        methodChannel.invokeMethod("onNoAD", error)
    }

    override fun onADExposure() = methodChannel.invokeMethod("onADExposure", null)
    override fun onVideoCached() = methodChannel.invokeMethod("onVideoCached", null)
    override fun onADOpened() = methodChannel.invokeMethod("onADOpened", null)
    override fun onADClosed() = methodChannel.invokeMethod("onADClosed", null)
    override fun onADLeftApplication() = methodChannel.invokeMethod("onADLeftApplication", null)
    override fun onADReceive() = methodChannel.invokeMethod("onADReceive", null)
    override fun onADClicked() = methodChannel.invokeMethod("onADClicked", null)
}