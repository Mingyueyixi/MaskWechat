package com.lu.wxmask.plugin.part

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.AppUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method

/**
 * 主页UI（即微信底部“微信”Tab选中时所在页面）处理，消息、小红点相关逻辑
 */
class HideMainUIListPluginPart : IPlugin {
    //咩啥用
    private val hookMethodNameRecord = linkedSetOf<String>()
    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        handleMainUIChattingListView(context, lpparam)
    }

    //隐藏指定用户的主页的消息
    private fun handleMainUIChattingListView(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
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
                        if (WXMaskPlugin.containChatUser(chatUser)) {
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
                    val packageName = AppUtil.getContext().packageName
                    val tipTvId = AppUtil.getContext().resources.getIdentifier(tipTvIdText, "id", packageName)
                    itemView.findViewById<View>(tipTvId)?.visibility = View.INVISIBLE

                    //头像上的红点
                    when (AppVersionUtil.getVersionCode()) {
                        Constrant.WX_CODE_8_0_32 -> {
                            val viewId = AppUtil.getContext().resources.getIdentifier("a2f", "id", packageName)
                            itemView.findViewById<View>(viewId)?.visibility = View.INVISIBLE
                        }
                    }

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
                        XposedHelpers2.callMethod<Any?>(msgTv, "setText", "")
                    } catch (e: Throwable) {
                        LogUtil.w("error", msgTv)
                    }
                }

            })
        LogUtil.i(getViewMethod.toString())
        hookMethodNameRecord.add(getViewMethod.toString())
    }


}