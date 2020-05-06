package tencent.ad

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec.*
import android.view.View.OnLayoutChangeListener
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import com.qq.e.ads.cfg.DownAPPConfirmPolicy
import com.qq.e.ads.cfg.VideoOption.AutoPlayPolicy.WIFI
import com.qq.e.ads.cfg.VideoOption.Builder
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressAD.NativeExpressADListener
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.comm.util.AdError
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.platform.PlatformView
import tencent.ad.O.TAG

class NativeAD(
        context: Context,
        messenger: BinaryMessenger,
        id: Int,
        params: Map<String, Any>
) : PlatformView, MethodCallHandler, NativeExpressADListener, OnLayoutChangeListener {
    private var nativeExpressADView: NativeExpressADView? = null
    private val methodChannel: MethodChannel
    private val container: FrameLayout
    private val posId = "${params["posId"]}"
    private var count = 5
    private var nativeExpressAD = NativeExpressAD(
            TencentAD.activity,
            ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT),
            O.APP_ID,
            posId,
            this
    )

    init {
        checkNotNull(O.APP_ID) { "在创建广告视图之前，必须先配置应用ID" }
        methodChannel = MethodChannel(messenger, O.NATIVE_AD_ID + "_" + id)
        methodChannel.setMethodCallHandler(this)
        container = FrameLayout(context)
        container.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        if (params.containsKey("count") && params["count"] != null) {
            count = params["count"] as Int
        }
        nativeExpressAD.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.NOConfirm)
    }

    override fun onMethodCall(methodCall: MethodCall, result: Result) {
        when (methodCall.method) {
            "refresh" -> {
                refreshAD()
                result.success(true)
            }
            "close" -> {
                nativeExpressADView!!.destroy()
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    override fun getView() = container

    override fun dispose() {
        methodChannel.setMethodCallHandler(null)
        nativeExpressADView!!.destroy()
    }

    private fun refreshAD() {
        nativeExpressAD.setVideoOption(Builder()
                .setAutoPlayPolicy(WIFI) // WIFI网络自动播放
                .setAutoPlayMuted(true) // 是否静音
                .build())
        nativeExpressAD.loadAD(count)
    }

    override fun onLayoutChange(view: View?, left: Int, top: Int, right: Int, bottom: Int,
                                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        val displayMetrics = Resources.getSystem().displayMetrics
        val widthSpec = makeMeasureSpec(displayMetrics.widthPixels, EXACTLY)
        val heightSpec = makeMeasureSpec(0, UNSPECIFIED)
        container.measure(widthSpec, heightSpec)
        val params = HashMap<String, Any>()
        params["width"] = container.measuredWidth / displayMetrics.density
        params["height"] = container.measuredHeight / displayMetrics.density
        methodChannel.invokeMethod("onLayoutChange", params)

    }

    override fun onNoAD(e: AdError) {
        Log.i(TAG, "NativeAD onNoAD: 错误码:${e.errorCode} ${e.errorMsg}")
        methodChannel.invokeMethod("onNoAD", null)
    }

    override fun onADLoaded(adList: List<NativeExpressADView>) {

        when {
            nativeExpressADView != null -> nativeExpressADView!!.destroy()
            // 广告可见才会产生曝光，否则将无法产生收益。
            container.visibility != View.VISIBLE -> container.visibility = View.VISIBLE
            container.childCount > 0 -> container.removeAllViews()
        }
        nativeExpressADView = adList[0]
        nativeExpressADView!!.addOnLayoutChangeListener(this)
        container.addView(nativeExpressADView)
        nativeExpressADView!!.render()  // 广告可见才会产生曝光，否则将无法产生收益。
        methodChannel.invokeMethod("onAdLoaded", null)
    }


    override fun onADClosed(nativeExpressADView: NativeExpressADView) {
        // 广告模板关闭后原生渲染广告也会被释放, 不可再复用
        if (container.childCount > 0) {
            container.removeAllViews()
            nativeExpressADView.destroy()
            container.visibility = View.GONE
        }
        methodChannel.invokeMethod("onAdClosed", null)
    }

    override fun onRenderFail(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onRenderFail", null)

    override fun onRenderSuccess(nativeExpressADView: NativeExpressADView) {
        methodChannel.invokeMethod("onRenderSuccess", null)
    }

    override fun onADExposure(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onAdExposure", null)

    override fun onADClicked(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onAdClicked", null)

    override fun onADLeftApplication(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onAdLeftApplication", null)

    override fun onADOpenOverlay(nativeExpressADView: NativeExpressADView){
        Handler(Looper.getMainLooper()).post(Runnable {
            fun run(){
                methodChannel.invokeMethod("onAdOpenOverlay", null)
            }
        })
    }

    override fun onADCloseOverlay(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onAdCloseOverlay", null)
}