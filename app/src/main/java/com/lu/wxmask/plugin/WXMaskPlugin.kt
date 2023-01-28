package com.lu.wxmask.plugin

import android.app.Activity
import android.app.AlertDialog
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
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.ConfigUtil.ConfigSetObserver
import com.lu.wxmask.util.QuickCountClickListenerUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

class WXMaskPlugin : IPlugin, ConfigSetObserver {
    private val hookMethodNameRecord = linkedSetOf<String>()
    var maskIdList = loadMaskIdList()

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
        handleMainUIChattingListView(context, lpparam)
        handleChattingUIFragment(context, lpparam)
    }


    private fun handleChattingUIFragment(context: Context, lpparam: LoadPackageParam) {
        val onEnterBeginMethod = XposedHelpers2.findMethodExactIfExists(
            ClazzN.BaseChattingUIFragment,
            context.classLoader,
            "onEnterBegin"
        )
        if (onEnterBeginMethod == null) {
            LogUtil.w("onEnterBegin function == null")
        } else {
            LogUtil.d("hook onEnterBegin")
            XposedHelpers2.hookMethod(onEnterBeginMethod,
                object : XC_MethodHook() {
                    val tagConst = "chatting-onEnterBegin"
                    val enterAction = EnterChattingHookAction(context, lpparam, tagConst)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        enterAction.handle(param)
                    }
                })
            return
        }

        //版本8.0.32-arm64反编译代码, I函数
        val dispatchMethod = XposedHelpers2.findMethodExactIfExists(
            ClazzN.BaseChattingUIFragment,
            context.classLoader,
            "I",
            //==int.class
            java.lang.Integer.TYPE,
            Runnable::class.java,
        )
        if (dispatchMethod == null) {
            LogUtil.w("I function is null")
        } else {
            LogUtil.d("hook I")
            XposedHelpers2.hookMethod(dispatchMethod, object : XC_MethodHook() {
                val tagConst = "chatting-I"
                val enterAction = EnterChattingHookAction(context, lpparam, tagConst)
                val doResumeAction = DoResumeAction(context, lpparam, tagConst)

                override fun afterHookedMethod(param: MethodHookParam) {
                    // onEnterBegin后，调用的函数的参数常量，啥意思不知道
//                    LogUtil.d("after I method call, first param：", param.args[0])
                    when (param.args[0]) {
                        //onEnterBegin
                        128 -> enterAction.handle(param)
                        //doResume
                        8 -> doResumeAction.handle(param)
                    }
                }
            })
            return
        }
        //找不到，尝试根据参数类型查找
        val dispatchMethodArray = XposedHelpers2.findMethodsByExactParameters(
            XposedHelpers2.findClass(ClazzN.BaseChattingUIFragment, context.classLoader),
            Void.TYPE,
            java.lang.Integer.TYPE,
            Runnable::class.java
        )
        if (dispatchMethodArray.isNullOrEmpty()) {
            LogUtil.w(
                "经过遍历查找，仍然不支持的版本：",
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            )
            return
        }
        LogUtil.d("hook I list function")
        //可能会出问题
        dispatchMethodArray.forEachIndexed { index, method ->
            val tagConst = "chatting-I-list-$index"
            val enterAction = EnterChattingHookAction(context, lpparam, tagConst)
            val doResumeAction = DoResumeAction(context, lpparam, tagConst)

            XposedHelpers2.hookMethod(dispatchMethod, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    when (param.args[0]) {
                        //onEnterBegin，发起聊天
                        128 -> enterAction.handle(param)
                        //doResume 去配置页
                        8 -> doResumeAction.handle(param)
                    }

                }
            })
        }

    }

    //隐藏指定用户的主页的消息
    private fun handleMainUIChattingListView(context: Context, lpparam: LoadPackageParam) {
        val adapterName = when (AppVersionUtil.getVersionCode()) {
            Constrant.WX_CODE_8_0_32 -> "com.tencent.mm.ui.conversation.p"
            Constrant.WX_CODE_8_0_22 -> "com.tencent.mm.ui.conversation.k"
            else -> null
        }
        var adapterClazz: Class<*>? = null
        if (adapterName != null) {
            adapterClazz = XposedHelpers2.findClassIfExists(
                adapterName,
                AppUtil.getContext().classLoader
            )
        }
        if (adapterClazz != null) {
            hookListViewAdapter(adapterClazz)
        } else {
            LogUtil.w("WeChat MainUI not found Adapter for ListView, guess start.")
            val setAdapterMethod = XposedHelpers2.findMethodExactIfExists(
                ListView::class.java.name,
                context.classLoader,
                "setAdapter",
                ListAdapter::class.java
            )
            XposedHelpers2.hookMethod(
                setAdapterMethod,
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val adapter = param.args[0] ?: return
                        hookListViewAdapter(adapter.javaClass)
                    }
                }
            )

        }

    }

    private fun hookListViewAdapter(adapterClazz: Class<*>) {
        val getViewMethod: Method = XposedHelpers2.findMethodExactIfExists(
            adapterClazz,
            "getView",
            java.lang.Integer.TYPE,
            View::class.java,
            ViewGroup::class.java
        ) ?: return
        if (hookMethodNameRecord.contains(getViewMethod.toString())) {
            return
        }
        val baseConversationClazz =
            XposedHelpers2.findClassIfExists(ClazzN.BaseConversation, AppUtil.getContext().classLoader)
        XposedHelpers2.hookMethod(
            getViewMethod,
            object : XC_MethodHook2() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val adapter: ListAdapter = param.thisObject as ListAdapter
                    val position: Int = (param.args[0] as? Int?) ?: return
                    val itemData: Any = adapter.getItem(position) ?: return

//                    LogUtil.d("after getView", adapter.javaClass, GsonUtil.toJson(itemData))
                    if (baseConversationClazz.isAssignableFrom(itemData.javaClass)) {
                        val chatUser: String = XposedHelpers2.getObjectField(itemData, "field_username") ?: return
                        val itemView: View = param.args[1] as? View ?: return
//                        LogUtil.d(chatUser, GsonUtil.toJson(itemData))
                        if (containChatUser(chatUser)) {
                            hideUnReadTipView(itemView, param)
                            hideLastMsgView(itemView, param)
                        }
                    } else {
                        //不是所需类型
                    }
                }

                //隐藏未读消息红点
                private fun hideUnReadTipView(itemView: View, param: MethodHookParam) {
                    val tipTvIdText = when (AppVersionUtil.getVersionCode()) {
                        Constrant.WX_CODE_8_0_32 -> "kmv"
                        else -> "tipcnt_tv"
                    }
                    val tipTvId = AppUtil.getContext().resources.getIdentifier(
                        tipTvIdText,
                        "id",
                        AppUtil.getContext().packageName
                    )
                    val tipTv = itemView.findViewById<View>(tipTvId)
                    tipTv.visibility = View.INVISIBLE
                }

                //隐藏最后一条消息
                private fun hideLastMsgView(itemView: View, param: MethodHookParam) {
                    val resource = AppUtil.getContext().resources
                    val msgTvIdName = when (AppVersionUtil.getVersionCode()) {
                        Constrant.WX_CODE_8_0_32 -> "fhs"
                        else -> "last_msg_tv"
                    }
                    val lastMsgViewId = resource.getIdentifier(msgTvIdName, "id", AppUtil.getContext().packageName)
                    LogUtil.d("mask last msg textView", lastMsgViewId)
                    val msgTv: View? = itemView.findViewById(lastMsgViewId)

                    try {
                        val ret: Any? = XposedHelpers2.callMethod(msgTv, "setText", "")
                    } catch (e: Throwable) {
                        LogUtil.w("error", msgTv)
                    }
                }

            })
        LogUtil.i(getViewMethod.toString())
        hookMethodNameRecord.add(getViewMethod.toString())
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

class EnterChattingHookAction(val context: Context, val lpparam: LoadPackageParam, val tagConst: String) {
    fun handle(param: XC_MethodHook.MethodHookParam) {
        val fragmentObj = param.thisObject
        LogUtil.w("enter chattingUI")
        //估计是class冲突，无法强转Fragment，改为反射获取
        val arguments = ReflectUtil.invokeMethod(fragmentObj, "getArguments") as Bundle?
        val activity = ReflectUtil.invokeMethod(fragmentObj, "getActivity") as Activity

        if (arguments != null) {
            LogUtil.w("hook onEnterBegin ", arguments)
            val chatUser = arguments.getString("Chat_User")
            //命中配置的微信号
            if (chatUser != null && WXMaskPlugin.containChatUser(chatUser)) {
                hideChatListUI(fragmentObj, activity, chatUser)
            } else {
                showChatListUI(fragmentObj)
            }
        } else {
            LogUtil.w("chattingUI's arguments if null")
        }
    }

    private fun findChatListView(fragmentObj: Any): View? {
        var listView = runCatching {
            XposedHelpers2.callMethod(fragmentObj, "getListView") as View
        }.getOrNull()
        if (listView == null) {
            listView = runCatching {
                val mmListViewId = context.resources.getIdentifier("b5n", "id", context.packageName)
//                val mmListView =
                XposedHelpers2.callMethod(fragmentObj, "findViewById", mmListViewId) as View
//                XposedHelpers2.callMethod(mmListView, "getListView") as View
            }.getOrNull()

        }
        if (listView == null) {
            listView = runCatching {
                val MMListViewClazz = XposedHelpers2.findClassIfExists(
                    "com.tencent.mm.ui.chatting.view.MMChattingListView",
                    context.classLoader
                )
                if (MMListViewClazz == null) {
                    null
                } else {
                    val mmListViewField =
                        XposedHelpers2.findFirstFieldByExactType(fragmentObj.javaClass, MMListViewClazz)
                    val mmListView = mmListViewField.get(fragmentObj)
                    mmListView as View
//                    XposedHelpers2.callMethod(mmListView, "getListView") as View
                }
            }.getOrNull()
        }
        LogUtil.d("findListView result：", listView)
        return listView
    }

    private fun showChatListUI(fragmentObj: Any) {
        val chatListView: View? = findChatListView(fragmentObj)
        if (chatListView != null) {
            chatListView.visibility = View.VISIBLE
            QuickCountClickListenerUtil.unRegister(chatListView.parent as? View?)
        } else {
            showChatListUIFromMask(fragmentObj)
        }
    }

    //恢复聊天页原先的ui
    private fun showChatListUIFromMask(fragmentObj: Any) {
        val contentView = ReflectUtil.invokeMethod(fragmentObj, "getView") as? ViewGroup?
        val maskView = contentView?.findViewWithTag<View?>(tagConst)
        if (maskView != null) {
            (maskView.parent as? ViewGroup)?.removeView(maskView)
        }
    }

    private fun hideChatListUI(fragmentObj: Any, activity: Activity, chatUser: String) {
        val item = try {
            ConfigUtil.getMaskList().first {
                it.maskId == chatUser
            }
        } catch (e: Exception) {
            LogUtil.w(e)
            return
        }

        val chatListView = findChatListView(fragmentObj)
        if (chatListView != null) {
            chatListView.visibility = View.INVISIBLE
            QuickCountClickListenerUtil.register(chatListView.parent as? View?) {
                chatListView.visibility = View.VISIBLE
            }
            LogUtil.i("hide chatListView")
        } else {
            hideListViewUIByMask(fragmentObj)
        }

        if (Constrant.WX_MASK_TIP_MODE_SILENT == item.tipMode) {
            // 静默模式，不弹提示框
        } else if (Constrant.WX_MASK_TIP_MODE_ALERT == item.tipMode) {
            handleAlertMode(activity, item)
        }

    }


    //对聊天页面添加水印，进行糊脸
    private fun hideListViewUIByMask(fragmentObj: Any) {
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

    }


}

class DoResumeAction(context: Context, lpparam: LoadPackageParam, tagConst: String) {
    fun handle(param: XC_MethodHook.MethodHookParam) {
        PluginProviders.from(WXConfigPlugin::class.java).doResumeHookAction(param)
    }

}

