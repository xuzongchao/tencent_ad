package tencent.ad

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Layout
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.AbsoluteLayout
import android.widget.FrameLayout
import androidx.core.view.children
import com.qq.e.ads.banner2.UnifiedBannerADListener
import com.qq.e.ads.banner2.UnifiedBannerView
import com.qq.e.ads.cfg.DownAPPConfirmPolicy
import com.qq.e.comm.managers.GDTADManager
import com.qq.e.comm.util.AdError
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.platform.PlatformView
import java.lang.Exception
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1

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
    private var bgColor:String = "#ffffff"

    init {
        methodChannel.setMethodCallHandler(this)
        bannerView.setDownConfirmPolicy(DownAPPConfirmPolicy.NOConfirm)

//        val bv = UnifiedBannerView(TencentAD.activity, O.APP_ID, posId, this)
//        val fl = FrameLayout(TencentAD.activity)
//        fl.addView(bv)
//        TencentAD.activity.addContentView(fl, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
//        bv.loadAD()
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
                "setBgColor" -> {
                    try{
                        var argument = methodCall.arguments as String
                        this.bgColor = argument
                        setBgColor(argument)
                    }catch (e:Exception){
                        Log.e("colorError>>>>", e.toString())
                    }
                    result.success(true)
                }
                else -> result.notImplemented()
            }

    override fun onNoAD(e: AdError) = methodChannel.invokeMethod("onNoAD", null)
    override fun onADReceive() = methodChannel.invokeMethod("onADReceive", null)
    override fun onADExposure() {
        setBgColor(bgColor)
        methodChannel.invokeMethod("onADExposure", null)
//        var view = (((bannerView.children.first() as ViewGroup).children.first() as ViewGroup).children.first() as ViewGroup).children.first()
//        var vvv = view.javaClass.superclass?.superclass?.superclass?.superclass?.getDeclaredField("mContext")
//        vvv?.isAccessible = true
////        vvv?.set(view, TencentAD.activity)
//        var oo = TencentAD.activity.applicationContext.javaClass?.getDeclaredField("mCurrentActivity")
//        oo?.isAccessible = true
//        oo.set(TencentAD.activity.applicationContext, TencentAD.activity)


//        var a = view.javaClass.getDeclaredField("a");
//        a?.isAccessible = true
//        var c = a.get(view)
//        var b = c.javaClass.superclass?.superclass?.superclass?.superclass?.getDeclaredField("mContext")
//        b?.isAccessible = true
//        b?.set(c, TencentAD.activity)
//        TencentAD.activity.applicationContext
//        Log.e("eeee10", a.toString())
//        Log.e("eeee11", b.toString())
//        Log.e("eeee9", vvv?.get(view).toString())
//        Log.e("eeee8", vvv.toString())
//        android.webkit.WebView
    }

    fun setBgColor(colorStr: String){
        try {
            var jsStr = """
            javascript:(function() { 
                document.body.firstElementChild.style.backgroundColor = "${colorStr}"
            })()
            """

            var webview = findWebView(bannerView)
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
            return null
        }
    }

    override fun onADClosed() = methodChannel.invokeMethod("onADClosed", null)
    override fun onADClicked() = methodChannel.invokeMethod("onADClicked", null)
    override fun onADLeftApplication() = methodChannel.invokeMethod("onADLeftApplication", null)
    override fun onADOpenOverlay() = methodChannel.invokeMethod("onADOpenOverlay", null)
    override fun onADCloseOverlay() = methodChannel.invokeMethod("onADCloseOverlay", null)
}