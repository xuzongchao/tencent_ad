package tencent.ad

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import com.qq.e.ads.splash.SplashAD
import com.qq.e.ads.splash.SplashADListener
import com.qq.e.comm.util.AdError
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import tencent.ad.O.TAG
import tencent.ad.TencentAD.Companion.activity

class SplashAD(
        context: Context,
        messenger: BinaryMessenger?,
        private val posId: String,
        bgPic: String?,
        private var instance: SplashAD?
) : SplashADListener {
    private val methodChannel = MethodChannel(messenger, O.SPLASH_AD_ID)
    private val container = FrameLayout(context)

    fun showAD() = fetchSplashAD(activity, null, O.APP_ID, posId, this, 0)

    fun closeAD() {
        methodChannel.setMethodCallHandler(null)
        val parent = container.parent as ViewGroup
        parent.removeView(container)
        instance = null
    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考文档。
     * @param activity        展示广告的activity
     * @param skipContainer   自定义的跳过按钮：只需绘制样式
     * @param appID           应用ID
     * @param posId           广告位ID
     * @param adListener      广告状态监听器
     * @param fetchDelay      拉取广告的超时时长：[3000, 5000]，0为默认
     */
    @Suppress("SameParameterValue")
    private fun fetchSplashAD(
            activity: Activity,
            skipContainer: View?,
            appID: String,
            posId: String,
            adListener: SplashADListener,
            fetchDelay: Int
    ) {
        if (instance != null) return
        instance = skipContainer?.let {
            SplashAD(activity, it, appID, posId, adListener, fetchDelay)
        } ?: SplashAD(activity, appID, posId, adListener, fetchDelay)
        instance!!.fetchAndShowIn(container)
    }

    override fun onADDismissed() {
        closeAD()
        methodChannel.invokeMethod("onAdDismiss", null)
    }

    override fun onNoAD(e: AdError) {
        methodChannel.invokeMethod("onNoAD", null)
        Log.i(TAG, "SplashAD onNoAD:无广告 错误码:${e.errorCode} ${e.errorMsg}")
        closeAD()
    }

    override fun onADPresent() {
        methodChannel.invokeMethod("onAdPresent", null)
    }

    override fun onADClicked() {
        methodChannel.invokeMethod("onADClicked", null)
    }

    override fun onADTick(ms: Long) {
        Log.i(TAG, "onADTick: $ms")
    }

    override fun onADExposure() = methodChannel.invokeMethod("onADExposure", null)


    init {
        container.setBackgroundColor(Color.WHITE)
        if (bgPic != null) {
            try {
                val manager = activity.packageManager
                val resources = manager.getResourcesForApplication(bgPic
                        .substring(0, bgPic.indexOf(":")))
                val resId = resources.getIdentifier(bgPic, null, null)
                container.background = ContextCompat.getDrawable(context, resId)
            } catch (e: Exception) {
                Log.i(TAG, "广告背景未获取, 资源:$bgPic", e)
            }
        }
        container.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        activity.addContentView(container, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }
}