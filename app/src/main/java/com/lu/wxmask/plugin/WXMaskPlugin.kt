package com.lu.wxmask.plugin

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.AppUtil
import com.lu.magic.util.ReflectUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.plugin.part.EnterChattingUIPluginPart
import com.lu.wxmask.plugin.part.HideMainUIListPluginPart
import com.lu.wxmask.plugin.part.HideSearchListUIPluginPart
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.ConfigUtil.ConfigSetObserver
import com.lu.wxmask.util.QuickCountClickListenerUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

class WXMaskPlugin : IPlugin, ConfigSetObserver {
    lateinit var maskIdList: Array<String?>
    private val hideSearchListPluginPart = HideSearchListUIPluginPart()
    private val enterChattingUIPluginPart = EnterChattingUIPluginPart()
    private val hideMainUIListPluginPart = HideMainUIListPluginPart()

    companion object {
        fun containChatUser(chatUser: String): Boolean {
            val self = PluginProviders.from(WXMaskPlugin::class.java)
            return self.maskIdList.contains(chatUser)
        }
    }

    private fun loadMaskIdList(): Array<String?> {
        val maskList = ConfigUtil.getMaskList()
        val ret = arrayOfNulls<String>(maskList.size)
        for (i in maskList.indices) {
            ret[i] = maskList[i].maskId
        }
        return ret
    }

    override fun onCreate() {
        ConfigUtil.registerConfigSetObserver(this)
    }

    override fun onConfigChange() {
        //实时更新id的值
        maskIdList = loadMaskIdList()
    }

    override fun handleHook(context: Context, lpparam: LoadPackageParam) {
//        handleViewClick(context, lpparam)
//        LogUtil.w(" context state:::::::::::", context is Application, context.applicationContext)
        maskIdList = loadMaskIdList()
        hideMainUIListPluginPart.handleHook(context, lpparam)
        enterChattingUIPluginPart.handleHook(context, lpparam)
        hideSearchListPluginPart.handleHook(context, lpparam)
    }


}


//    private fun handleViewClick(context: Context, lpparam: LoadPackageParam) {
//        XposedHelpers2.findAndHookMethod(
//            View::class.java,
//            "performClick",
//            object : XC_MethodReplacement() {
//                @Throws(Throwable::class)
//                override fun replaceHookedMethod(param: MethodHookParam): Any {
//                    val view = param.thisObject as View
//                    LogUtil.w("click view for ", view)
//                    if ("com.tencent.mm.ui.conversation.ConversationFolderItemView" != view.javaClass.name) {
//                        return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)
//                    }
//                    val index = TextCheckUtil.haveMatchText(view, *maskIdList)
//                    if (index > -1) {
//                        LogUtil.w("WXMaskPlugin replace performCLick")
//                        val maskList = ConfigUtil.getMaskList()
//                        val item = maskList[index]
//                        handleMaskItem(param, view, item)
//                        LogUtil.w(GsonUtil.toJson(item))
//                        return false
//                    }
//                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)
//                }
//            }
//        )
//    }
//
//    private fun handleMaskItem(param: MethodHookParam, view: View, item: MaskItemBean) {
//        try {
//            val tipMode = item.tipMode
//            if (Constrant.WX_MASK_TIP_MODE_SILENT == tipMode) {
//                //静默模式，啥都没干
//            } else if (Constrant.WX_MASK_TIP_MODE_ALERT == tipMode) {
//                handleAlertMode(view.getContext(), item);
//            }
//        } catch (e: Exception) {
//            LogUtil.e(e)
//        }
//    }
//

//    待开发的功能 //TODO
//    private fun handleCipherMode(param: MethodHookParam, view: View, item: MaskItemBean) {
//        LogUtil.w("handleCipherMode", item)
//        val context = view.context
//        //提示模式
//        val tipData = GsonUtil.fromJson(item.tipData, AlertTipData::class.java)
//        val layout = LinearLayout(context).apply {
//            orientation = LinearLayout.VERTICAL
//            layoutParams =
//                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        }
//        val textView = TextView(context)
//        textView.text = "该用户已私密，请输入接头暗号"
//        val editText = EditText(context)
//
//        layout.addView(textView)
//        layout.addView(editText, LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//
//        AlertDialog.Builder(view.context)
//            .setView(layout)
//            .setNeutralButton("取消", null)
//            .setNegativeButton("确定") { dialog: DialogInterface?, which: Int ->
//                val text = editText.text.toString()
//                if ("123456" == text) {
//                    // view.performClick() //不使用，由于hook替换了方法，直接调用会再次触发hook
//                    // 调用原始方法
//                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)
//                } else {
//                    ToastUtil.show("暗号错误")
//                }
//            }
//            .show()
//    }
//
