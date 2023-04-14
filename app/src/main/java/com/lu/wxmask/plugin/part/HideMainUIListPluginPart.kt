package com.lu.wxmask.plugin.part

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.magic.util.view.SelfDeepCheck
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.dev.DebugUtil
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
            Constrant.WX_CODE_8_0_32, Constrant.WX_CODE_8_0_33, Constrant.WX_CODE_8_0_34-> "com.tencent.mm.ui.conversation.p"
            Constrant.WX_CODE_8_0_22 -> "com.tencent.mm.ui.conversation.k"
            else -> null
        }
        var adapterClazz: Class<*>? = null
        if (adapterName != null) {
            adapterClazz = XposedHelpers2.findClassIfExists(adapterName, AppUtil.getContext().classLoader)
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
                        LogUtil.w("adapter: ", param.args[0])
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
        val baseConversationClazz = ClazzN.from(ClazzN.BaseConversation)
        XposedHelpers2.hookMethod(
            getViewMethod,
            object : XC_MethodHook2() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val adapter: ListAdapter = param.thisObject as ListAdapter
                    val position: Int = (param.args[0] as? Int?) ?: return
                    val itemData: Any = adapter.getItem(position) ?: return

//                    LogUtil.d("after getView", adapter.javaClass, GsonUtil.toJson(itemData))
                    if (baseConversationClazz?.isAssignableFrom(itemData.javaClass) != true
                        && !itemData::class.java.name.startsWith("com.tencent.mm.storage")){
                        //不是所需类型
                        //LogUtil.d(chatUser, GsonUtil.toJson(itemData))
                        LogUtil.w(AppVersionUtil.getVersionCode(), "类型检查错误，尝试继续", itemData::class.java, itemData::class.java.classes)
                    }
                    val chatUser: String = XposedHelpers2.getObjectField(itemData, "field_username") ?: return
                    val itemView: View = param.args[1] as? View ?: return
                    if (WXMaskPlugin.containChatUser(chatUser)) {
                        hideUnReadTipView(itemView, param)
                        hideLastMsgView(itemView, param)
                    }
                }

                //隐藏未读消息红点
                private fun hideUnReadTipView(itemView: View, param: MethodHookParam) {
                    //带文字的未读红点
                    val tipTvIdText = when (AppVersionUtil.getVersionCode()) {
                        Constrant.WX_CODE_8_0_32, Constrant.WX_CODE_8_0_33, Constrant.WX_CODE_8_0_34 -> "kmv"
                        else -> "tipcnt_tv"
                    }
                    val packageName = AppUtil.getContext().packageName
                    val tipTvId = AppUtil.getContext().resources.getIdentifier(tipTvIdText, "id", packageName)
                    itemView.findViewById<View>(tipTvId)?.visibility = View.INVISIBLE

                    //头像上的小红点
                    when (AppVersionUtil.getVersionCode()) {
                        Constrant.WX_CODE_8_0_32, Constrant.WX_CODE_8_0_33, Constrant.WX_CODE_8_0_34 -> {
                            val viewId = AppUtil.getContext().resources.getIdentifier("a2f", "id", packageName)
                            itemView.findViewById<View>(viewId)?.visibility = View.INVISIBLE
                        }
                    }

                }

                //隐藏最后一条消息
                private fun hideLastMsgView(itemView: View, param: MethodHookParam) {
                    val resource = AppUtil.getContext().resources
                    val msgTvIdName = when (AppVersionUtil.getVersionCode()) {
                        in Constrant.WX_CODE_8_0_32 .. Constrant.WX_CODE_8_0_34-> "fhs"
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