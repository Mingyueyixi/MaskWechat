package com.lu.wxmask.plugin

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.magic.util.view.SelfDeepCheck
import com.lu.magic.util.view.ViewUtil
import com.lu.wxmask.BuildConfig
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CommonPlugin : IPlugin {
    override fun handleHook(context: Context?, lpparam: XC_LoadPackage.LoadPackageParam?) {
//        if (!BuildConfig.DEBUG) {
//            return
//        }
//        XposedHelpers2.findAndHookMethod(
//            View::class.java,
//            "onTouchEvent",
//            MotionEvent::class.java,
//            object : XC_MethodHook2() {
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    val view = param.thisObject
//                    LogUtil.w("touch view is ", view)
//                    ChildDeepCheck().each(view as View?) { child ->
//                        LogUtil.w("---> child is ", child)
//                    }
//                }
//            }
//        )
    }
}