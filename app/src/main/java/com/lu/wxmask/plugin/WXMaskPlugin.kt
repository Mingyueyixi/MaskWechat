package com.lu.wxmask.plugin

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.JsonObject
import com.lu.magic.frame.baseutils.kxt.optBoolean
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.bean.MaskItemBean.AlertTipData
import com.lu.wxmask.util.ConfigUtil.Companion.getMaskList
import com.lu.wxmask.util.ConfigUtil.Companion.registerConfigSetObserver
import com.lu.wxmask.util.ConfigUtil.ConfigSetObserver
import com.lu.wxmask.util.TextCheckUtil
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class WXMaskPlugin : IPlugin, ConfigSetObserver {
    private var keywordList = getKeywordList()
    private fun getKeywordList(): Array<String?> {
        val maskList = getMaskList()
        val ret = arrayOfNulls<String>(maskList.size)
        for (i in maskList.indices) {
            ret[i] = maskList[i].keyWord
        }
        return ret
    }

    override fun onCreate() {
        registerConfigSetObserver(this)
    }

    override fun onConfigChange() {
        //实时更新keywordList的值
        keywordList = getKeywordList()
    }

    override fun handleHook(context: Context, lpparam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            View::class.java,
            "performClick",
            object : XC_MethodReplacement() {
                @Throws(Throwable::class)
                override fun replaceHookedMethod(param: MethodHookParam): Any {
                    val view = param.thisObject as View
                    if ("com.tencent.mm.ui.conversation.ConversationFolderItemView" != view.javaClass.name) {
                        return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)
                    }
                    val index = TextCheckUtil.haveMatchText(view, *keywordList)
                    if (index > -1) {
                        LogUtil.w("WXMaskPlugin replace performCLick")
                        val maskList = getMaskList()
                        val item = maskList[index]
                        handleMaskItem(param, view, item)
                        LogUtil.w(GsonUtil.toJson(item))
                        return false
                    }
                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)
                }
            }
        )
    }

    private fun handleMaskItem(param: MethodHookParam, view: View, item: MaskItemBean) {
        try {
            val tipMode = item.tipMode
            if (Constrant.WX_MASK_TIP_MODE_SILENT == tipMode) {
                //静默模式，啥都没干
            } else if (Constrant.WX_MASK_TIP_MODE_ALERT == tipMode) {
                handleAlertMode(view.getContext(), item);
            }
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }


    private fun handleAlertMode(uiContext: Context, item: MaskItemBean) {
        //提示模式
        val tipData = GsonUtil.fromJson(item.tipData, AlertTipData::class.java)
        AlertDialog.Builder(uiContext)
            .setMessage(tipData.mess)
            .setNegativeButton("知道了", null)
            .show()
    }
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

}