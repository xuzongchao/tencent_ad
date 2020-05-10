package tencent.ad

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec.*
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.children
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
import java.lang.Exception
import java.lang.annotation.Native

class NativeAD(
        context: Context,
        messenger: BinaryMessenger,
        id: Int,
        params: Map<String, Any>
) : PlatformView, MethodCallHandler, NativeExpressADListener, OnLayoutChangeListener {
    private var nativeExpressADView: NativeExpressADView?=null
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
    private var adList: List<NativeExpressADView> = ArrayList()
    private var bgColor: String = "#ffffffff"

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
            "show" -> {
                show()
                result.success(true)
            }
            "setBgColor" -> {
                var argument = methodCall.arguments as String
                this.bgColor = argument
                setBgColor(argument)
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

    fun setBgColor(colorStr: String){
        try {
            var jsStr = """
            javascript:(function() {
                document.body.firstElementChild.style.backgroundColor = "${colorStr}"
            })()
            """

            var webview = findWebView(nativeExpressADView!!)
            if (webview != null) {
                if(webview is com.tencent.smtt.sdk.WebView) {
                    var v = webview as com.tencent.smtt.sdk.WebView
                    v.loadUrl(jsStr)
                }else if(webview is WebView){
                    var v = webview as WebView
                    v.loadUrl(jsStr)
                }
            }
        }catch (e:Exception){
            Log.e("adError>>>", "改颜色报错")
        }
//        try{
//            nativeExpressADView!!.setBackgroundColor(Color.BLACK)
//            (nativeExpressADView as ViewGroup).children.first().setBackgroundColor(Color.BLACK)
//        }catch(e:Exception){
//            Log.e("ff", e.stackTrace.toString())
//        }
    }

    fun findWebView(view: View): View? {
        try {
            if (view is com.tencent.smtt.sdk.WebView || view is WebView) {
                return view
            } else {
                var child = (view as ViewGroup).children.first()
                return findWebView(child)
            }
        }catch (e:Exception){
            return view
        }
    }


///展示广告，比较缓存列表中所有广告，选择ecpm价格最高的展示
    private fun show(){
//      start-checkcount：检查缓存列表里是否有广告
        if(adList.count() ==0){
            return
        }
//      end-checkcount

//      start-getbest:选择价格最高的广告
        var current = adList[0]
        try {
            for (index in 0..adList.count() - 1) {
                val now = adList[index]
                if (now.boundData.ecpm > current.boundData.ecpm) {
                    current = now
                }
            }
            adList -= current
        }catch(e:Exception){
            Log.e("广告错误", "选择高价广告错误")
        }
//      end-getbest

//      start-renderad:普光选中的广告
        when {
            nativeExpressADView != null -> nativeExpressADView!!.destroy()
            // 广告可见才会产生曝光，否则将无法产生收益。
            container.visibility != View.VISIBLE -> container.visibility = View.VISIBLE
            container.childCount > 0 -> container.removeAllViews()
        }
        nativeExpressADView = current
        nativeExpressADView!!.addOnLayoutChangeListener(this)
        container.addView(nativeExpressADView)
        nativeExpressADView!!.render()  // 广告可见才会产生曝光，否则将无法产生收益。
//      end-renderad

        setBgColor(this.bgColor)
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
        this.adList = adList
        show()
        methodChannel.invokeMethod("onAdLoaded", null)
    }

    override fun onRenderFail(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onRenderFail", null)

    override fun onRenderSuccess(nativeExpressADView: NativeExpressADView) {
        methodChannel.invokeMethod("onRenderSuccess", null)
    }

    override fun onADExposure(nativeExpressADView: NativeExpressADView) {
        methodChannel.invokeMethod("onAdExposure", null)
        setBgColor(this.bgColor)
    }

    override fun onADClicked(nativeExpressADView: NativeExpressADView) =
            methodChannel.invokeMethod("onAdClicked", null)

    override fun onADClosed(nativeExpressADView: NativeExpressADView) {
        // 广告模板关闭后原生渲染广告也会被释放, 不可再复用
        if (container.childCount > 0) {
            container.removeAllViews()
            nativeExpressADView.destroy()
            container.visibility = View.GONE
        }
        methodChannel.invokeMethod("onAdClosed", null)
    }

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