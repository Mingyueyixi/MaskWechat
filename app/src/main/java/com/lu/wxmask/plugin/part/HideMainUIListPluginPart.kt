package com.lu.wxmask.plugin.part

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.util.rangeTo
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ResUtil
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.wxmask.App
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.MainHook
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import kotlin.math.max

/**
 * 主页UI（即微信底部“微信”Tab选中时所在页面）处理，消息、小红点相关逻辑
 */
class HideMainUIListPluginPart : IPlugin {

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        handleMainUIChattingListView(context, lpparam)
    }

    //隐藏指定用户的主页的消息
    private fun handleMainUIChattingListView(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        val adapterName = when (AppVersionUtil.getVersionCode()) {
            Constrant.WX_CODE_8_0_22 -> "com.tencent.mm.ui.conversation.k"
            in Constrant.WX_CODE_8_0_32..Constrant.WX_CODE_8_0_34 -> {
                if (AppVersionUtil.getVersionName() == "8.0.35") {
                    "com.tencent.mm.ui.conversation.r"
                } else {
                    "com.tencent.mm.ui.conversation.p"
                }
            }
            in Constrant.WX_CODE_8_0_35 .. Constrant.WX_CODE_8_0_37 -> "com.tencent.mm.ui.conversation.x"

            else -> null
        }
        var adapterClazz: Class<*>? = null
        if (adapterName != null) {
            adapterClazz = XposedHelpers2.findClassIfExists(adapterName, AppUtil.getContext().classLoader)
        }
        if (adapterClazz != null) {
            LogUtil.d("WeChat MainUI main Tap List Adapter", adapterClazz)
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
                        LogUtil.w("List adapter ", adapter)
                        if (adapter::class.java.name.startsWith("com.tencent.mm.ui.conversation")) {
                            LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess adapter: ", adapter)
                            hookListViewAdapter(adapter.javaClass)
                        }
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
        val getViewMethodIDText = getViewMethod.toString()
        if (MainHook.uniqueMetaStore.contains(getViewMethodIDText)) {
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

                    LogUtil.d("after getView", adapter.javaClass, GsonUtil.toJson(itemData))
                    if (baseConversationClazz?.isAssignableFrom(itemData.javaClass) != true
                        && !itemData::class.java.name.startsWith("com.tencent.mm.storage")
                    ) {
                        //不是所需类型
                        //LogUtil.d(chatUser, GsonUtil.toJson(itemData))
                        LogUtil.w(
                            AppVersionUtil.getSmartVersionName(),
                            "类型检查错误，尝试继续",
                            itemData::class.java,
                            itemData::class.java.classes
                        )
                    }
                    val chatUser: String = XposedHelpers2.getObjectField(itemData, "field_username") ?: return
                    val itemView: View = param.args[1] as? View ?: return
                    if (WXMaskPlugin.containChatUser(chatUser)) {
                        hideUnReadTipView(itemView, param)
                        hideMsgViewItemText(itemView, param)
                        hideLastMsgTime(itemView, param)
                    }
                }

                private fun hideLastMsgTime(itemView: View, params: MethodHookParam) {
                    val viewId = AppUtil.getContext().resources.getIdentifier("l0s", "id", AppUtil.getContext().packageName)
                    itemView.findViewById<View>(viewId)?.visibility = View.INVISIBLE

                }

                //隐藏未读消息红点
                private fun hideUnReadTipView(itemView: View, param: MethodHookParam) {
                    //带文字的未读红点
                    val tipTvIdText = when (AppVersionUtil.getVersionCode()) {
                        in 0..Constrant.WX_CODE_8_0_22 -> "tipcnt_tv"
                        in Constrant.WX_CODE_8_0_22..Constrant.WX_CODE_8_0_34 -> "kmv"
                        else -> "kmv"
                    }
                    val packageName = AppUtil.getContext().packageName
                    val tipTvId = AppUtil.getContext().resources.getIdentifier(tipTvIdText, "id", packageName)
                    itemView.findViewById<View>(tipTvId)?.visibility = View.INVISIBLE

                    //头像上的小红点
                    val small_red = when (AppVersionUtil.getVersionCode()) {
                        in 0..Constrant.WX_CODE_8_0_34 -> "a2f"
                        else -> "a2f"
                    }
                    val viewId = AppUtil.getContext().resources.getIdentifier(small_red, "id", packageName)
                    itemView.findViewById<View>(viewId)?.visibility = View.INVISIBLE
                }

                //隐藏最后一条消息等
                private fun hideMsgViewItemText(itemView: View, param: MethodHookParam) {
                    val resource = AppUtil.getContext().resources
                    val msgTvIdName = when (AppVersionUtil.getVersionCode()) {
                        in 0..Constrant.WX_CODE_8_0_22 -> "last_msg_tv"
                        in Constrant.WX_CODE_8_0_22..Constrant.WX_CODE_8_0_34 -> "fhs"
                        else -> "fhs"
                    }
                    val lastMsgViewId = resource.getIdentifier(msgTvIdName, "id", AppUtil.getContext().packageName)
                    LogUtil.d("mask last msg textView", lastMsgViewId)
                    if (lastMsgViewId != 0 && lastMsgViewId != View.NO_ID) {
                        try {
                            val msgTv: View? = itemView.findViewById(lastMsgViewId)
                            XposedHelpers2.callMethod<Any?>(msgTv, "setText", "")
                        } catch (e: Throwable) {
                            LogUtil.w("error", e)
                        }
                    } else {
                        //
                        LogUtil.w("主页last消息id版本不适配，开启暴力隐藏", AppVersionUtil.getSmartVersionName())
                        val ClazzNoMeasuredTextView = ClazzN.from("com.tencent.mm.ui.base.NoMeasuredTextView")
                        ChildDeepCheck().each(itemView) { child ->
                            try {
                                if (ClazzNoMeasuredTextView?.isAssignableFrom(child::class.java) == true
                                    || TextView::class.java.isAssignableFrom(child::class.java)
                                ) {
                                    XposedHelpers2.callMethod<String?>(child, "setText", "")
                                }
                            } catch (e: Throwable) {
                            }
                        }
                    }

                }

            })
        MainHook.uniqueMetaStore.add(getViewMethodIDText)
    }


}