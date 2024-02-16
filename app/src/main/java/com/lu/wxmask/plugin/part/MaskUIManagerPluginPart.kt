package com.lu.wxmask.plugin.part

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.wxmask.ClazzN
import com.lu.wxmask.plugin.ui.MaskManagerCenterUI
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 糊脸ui管理页面
 */
class MaskUIManagerPluginPart : IPlugin {
    override fun handleHook(context: Context?, lpparam: XC_LoadPackage.LoadPackageParam?) {
        XposedHelpers2.findAndHookMethod(
            ClazzN.from("com.tencent.mm.plugin.setting.ui.setting.SettingsCareModeIntro"),
            "initView",
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act: Activity = param.thisObject as Activity
                    val contentView = act.findViewById<ViewGroup>(android.R.id.content)
                    ChildDeepCheck().filter(contentView) {
                        return@filter it is Button && it.id > 0
                    }.forEach {
                        it.setOnLongClickListener { v ->
                            MaskManagerCenterUI(act).show()
                            return@setOnLongClickListener true
                        }
                    }
                }
            }
        )
    }


}