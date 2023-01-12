package com.lu.wxmask.plugin

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.lu.lposed.api2.XposedHelpers2
import com.lu.magic.util.ReflectUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.ConfigUtil.Companion.registerConfigSetObserver
import com.lu.wxmask.util.ConfigUtil.ConfigSetObserver
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class WXMaskPlugin : IPlugin, ConfigSetObserver {
    private var maskIdList = getMaskIdList()
    private fun getMaskIdList(): Array<String?> {
        val maskList = ConfigUtil.getMaskList()
        val ret = arrayOfNulls<String>(maskList.size)
        for (i in maskList.indices) {
            ret[i] = maskList[i].maskId
        }
        return ret
    }

    override fun onCreate() {
        registerConfigSetObserver(this)
    }

    override fun onConfigChange() {
        //实时更新id的值
        maskIdList = getMaskIdList()
    }

    override fun handleHook(context: Context, lpparam: LoadPackageParam) {
//        handleViewClick(context, lpparam)
        handleChattingUIFragment(context, lpparam)
    }

    private fun handleChattingUIFragment(context: Context, lpparam: LoadPackageParam) {

        XposedHelpers.findAndHookMethod(
            "com.tencent.mm.ui.chatting.BaseChattingUIFragment",
            context.classLoader,
            "onEnterBegin",
            object : XC_MethodHook() {
                val tagConst = "ChattingUIFragment-mask-view"

                override fun afterHookedMethod(param: MethodHookParam) {
                    hook(param)
                }

                private fun hook(param: MethodHookParam) {
                    val fragmentObj = param.thisObject
                    LogUtil.w("enter chattingUI")
                    //估计是class冲突，无法强转Fragment，改为反射获取
                    val arguments = ReflectUtil.invokeMethod(fragmentObj, "getArguments") as Bundle?
                    val activity = ReflectUtil.invokeMethod(fragmentObj, "getActivity") as Activity

                    if (arguments != null) {
                        LogUtil.w("hook onEnterBegin ", arguments)
                        val chatUser = arguments.getString("Chat_User")
                        //命中配置的微信号
                        if (chatUser != null && maskIdList.contains(chatUser)) {
                            val listView = findChatListView(fragmentObj)
                            if (listView != null) {
                                listView.visibility = View.INVISIBLE
                            } else {
                                addMaskToChatUI(param, fragmentObj, activity, chatUser)
                            }
                        } else {
                            val listView = findChatListView(fragmentObj)
                            if (listView != null) {
                                listView.visibility = View.VISIBLE
                            } else {
                                resetChatUI(fragmentObj)
                            }
                        }
                    }
                    else{
                        LogUtil.w("chattingUI's arguments if null")
                    }
                }

                private fun findChatListView(fragmentObj: Any): View? {
                    return XposedHelpers2.callMethod(fragmentObj, "getListView")
                }

                //恢复聊天页原先的ui
                private fun resetChatUI(fragmentObj: Any) {
                    //糊界面一脸
                    val view = ReflectUtil.invokeMethod(fragmentObj, "getView") as? ViewGroup?
                    if (view != null) {
                        val maskView: View? = view.findViewWithTag(tagConst)
                        maskView?.parent?.let {
                            if (it is ViewGroup) {
                                it.removeView(maskView)
                            }
                        }
                    }

                }

                //对聊天页面添加水印，进行糊脸
                private fun addMaskToChatUI(
                    param: MethodHookParam,
                    fragmentObj: Any,
                    activity: Activity,
                    chatUser: String
                ) {
                    val item = try {
                        ConfigUtil.getMaskList().first {
                            it.maskId == chatUser
                        }
                    } catch (e: Exception) {
                        LogUtil.w(e)
                        return
                    }
                    //糊界面一脸
                    val contentView = ReflectUtil.invokeMethod(fragmentObj, "getView") as? ViewGroup?
                    contentView?.let {
                        val pvId = it.resources.getIdentifier("b49", "id", lpparam.packageName)
                        val parent = contentView.findViewById<ViewGroup>(pvId)
                        var maskView = it.findViewWithTag<View>(tagConst)
                        if (maskView == null) {
                            maskView = View(it.context).also { child ->
                                child.tag = tagConst
                                child.background = ColorDrawable(0xFFEDEDED.toInt())
                                child.translationZ = 9999f
                            }
                            parent.addView(
                                maskView,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    }

                    if (Constrant.WX_MASK_TIP_MODE_SILENT == item.tipMode) {
                        // 静默模式，不弹提示框
                    } else if (Constrant.WX_MASK_TIP_MODE_ALERT == item.tipMode) {
                        handleAlertMode(activity, item)
                    }

                    LogUtil.w("hook dialog show ")
                }

            }
        )
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

    private fun handleAlertMode(uiContext: Context, item: MaskItemBean) {
        //提示模式
        AlertDialog.Builder(uiContext)
            .setMessage(MaskItemBean.AlertTipData.from(item).mess)
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