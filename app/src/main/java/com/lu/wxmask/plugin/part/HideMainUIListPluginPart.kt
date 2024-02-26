package com.lu.wxmask.plugin.part

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ResUtil
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.MainHook
import com.lu.wxmask.plugin.WXConfigPlugin
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.plugin.ui.MaskUtil
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.ext.getViewId
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 主页UI（即微信底部“微信”Tab选中时所在页面）处理，消息、小红点相关逻辑
 */
class HideMainUIListPluginPart : IPlugin {
    val GetItemMethodName = when (AppVersionUtil.getVersionCode()) {
        Constrant.WX_CODE_8_0_22 -> "aCW"
        in Constrant.WX_CODE_8_0_22..Constrant.WX_CODE_8_0_43 -> "k"
        else -> "m"
    }

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        runCatching {
            handleMainUIChattingListView2(context, lpparam)
        }.onFailure {
            LogUtil.w("hide mainUI listview fail, try to old function.")
            handleMainUIChattingListView(context, lpparam)
        }

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

            Constrant.WX_CODE_8_0_35 -> "com.tencent.mm.ui.conversation.r"
            in Constrant.WX_CODE_8_0_35..Constrant.WX_CODE_8_0_41 -> "com.tencent.mm.ui.conversation.x"
            Constrant.WX_CODE_8_0_47 -> "com.tencent.mm.ui.conversation.p3"
            else -> null
        }
        var adapterClazz: Class<*>? = null
        if (adapterName != null) {
            adapterClazz = ClazzN.from(adapterName, context.classLoader)
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
            if (setAdapterMethod == null) {
                LogUtil.w("setAdapterMethod is null")
                return
            }
            XposedHelpers2.hookMethod(
                setAdapterMethod,
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val adapter = param.args[0] ?: return
                        LogUtil.i("hook List adapter ", adapter)
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
        LogUtil.w(getViewMethod)
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
//                        hideLastMsgTime(itemView, param)
                    }
                }

                //消息条目，时间，暂不隐藏？改成去年？
                private fun hideLastMsgTime(itemView: View, params: MethodHookParam) {
                    val viewId = ResUtil.getViewId("l0s")
                    itemView.findViewById<View>(viewId)?.visibility = View.INVISIBLE

                }

                //隐藏未读消息红点
                private fun hideUnReadTipView(itemView: View, param: MethodHookParam) {
                    //带文字的未读红点
                    val tipTvIdTextID = when (AppVersionUtil.getVersionCode()) {
                        in 0..Constrant.WX_CODE_8_0_22 -> "tipcnt_tv"
                        in Constrant.WX_CODE_8_0_22..Constrant.WX_CODE_8_0_41 -> "kmv"
                        else -> "kmv"
                    }
                    val tipTvId = ResUtil.getViewId(tipTvIdTextID)
                    itemView.findViewById<View>(tipTvId)?.visibility = View.INVISIBLE

                    //头像上的小红点
                    val small_red = when (AppVersionUtil.getVersionCode()) {
                        in 0..Constrant.WX_CODE_8_0_40 -> "a2f"
                        Constrant.WX_CODE_8_0_41 -> "o_u"
                        else -> "o_u"
                    }
                    val viewId = ResUtil.getViewId(small_red)
                    itemView.findViewById<View>(viewId)?.visibility = View.INVISIBLE
                }

                //隐藏最后一条消息等
                private fun hideMsgViewItemText(itemView: View, param: MethodHookParam) {
                    val msgTvIdName = when (AppVersionUtil.getVersionCode()) {
                        in 0..Constrant.WX_CODE_8_0_22 -> "last_msg_tv"
                        in Constrant.WX_CODE_8_0_22..Constrant.WX_CODE_8_0_40 -> "fhs"
                        Constrant.WX_CODE_8_0_41 -> "ht5"
                        else -> "ht5"
                    }
                    val lastMsgViewId = ResUtil.getViewId(msgTvIdName)
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


    private fun findGetItemMethod(adapterClazz: Class<*>?): Method? {
        if (adapterClazz == null) {
            return null
        }
        var method: Method? = XposedHelpers2.findMethodExactIfExists(adapterClazz, GetItemMethodName, Integer.TYPE)
        if (method != null) {
            return method
        }
        var methods = XposedHelpers2.findMethodsByExactPredicate(adapterClazz) { m ->
            val ret = !arrayOf(
                Object::class.java,
                String::class.java,
                Byte::class.java,
                Short::class.java,
                Long::class.java,
                Float::class.java,
                Double::class.java,
                String::class.java,
                java.lang.Byte.TYPE,
                java.lang.Short.TYPE,
                java.lang.Integer.TYPE,
                java.lang.Long.TYPE,
                java.lang.Float.TYPE,
                java.lang.Double.TYPE,
                java.lang.Void.TYPE
            ).contains(m.returnType)
            val paramVail = m.parameterTypes.size == 1 && m.parameterTypes[0] == Integer.TYPE
            return@findMethodsByExactPredicate paramVail && ret && Modifier.isPublic(m.modifiers) && !Modifier.isAbstract(m.modifiers)
        }
        if (methods.size > 0) {
            method = methods[0]
            if (methods.size > 1) {
                LogUtil.d("find getItem methods: []--> " + methods.joinToString("\n"))
            }
            LogUtil.d("guess getItem method $method")
        }
        return method
    }

    private fun handleMainUIChattingListView2(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        //listAdapter getItem方法被重命名了
        //8.0.32-8.0.34 com.tencent.mm.ui.y
        //8.0.35-8.0.37　　com.tencent.mm.ui.z
        //搞实际Adapter的父类，是个抽象类
        val adapterClazzName = when (AppVersionUtil.getVersionCode()) {
            Constrant.WX_CODE_8_0_22 -> "com.tencent.mm.ui.g"
            in Constrant.WX_CODE_8_0_32..Constrant.WX_CODE_8_0_34 -> "com.tencent.mm.ui.y"
            in Constrant.WX_CODE_8_0_35..Constrant.WX_CODE_8_0_38 -> "com.tencent.mm.ui.z"
            in Constrant.WX_CODE_8_0_40..Constrant.WX_CODE_8_0_43 -> "com.tencent.mm.ui.b0"
            in Constrant.WX_CODE_8_0_43..Constrant.WX_CODE_8_0_44 -> "com.tencent.mm.ui.h3"
            in Constrant.WX_CODE_8_0_43..Constrant.WX_CODE_8_0_47 -> "com.tencent.mm.ui.i3"
            else -> null
        }
        var getItemMethod = if (adapterClazzName != null) {
            findGetItemMethod(ClazzN.from(adapterClazzName))
        } else {
            null
        }
        if (getItemMethod != null) {
            hookListViewGetItem(getItemMethod)
            return
        }


        LogUtil.w("WeChat MainUI ListView not found adapter, guess start.")
        XposedHelpers2.findAndHookMethod(
            ListView::class.java,
            "setAdapter",
            ListAdapter::class.java,
            object : XC_MethodHook2() {
                private var isHookGetItemMethod = false

                override fun afterHookedMethod(param: MethodHookParam) {
                    val adapter = param.args[0] ?: return
                    LogUtil.d("List adapter ", adapter)
                    if (adapter::class.java.name.startsWith("com.tencent.mm.ui.conversation")) {
                        if (isHookGetItemMethod) {
                            return
                        }
                        LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess setAdapter: ", adapter, adapter.javaClass.superclass)
                        var getItemMethod = findGetItemMethod(adapter::class.java.superclass)
//                        getItemMethod = XposedHelpers2.findMethodExactIfExists(adapter::class.java.superclass, GetItemMethodName, Integer.TYPE)
                        if (getItemMethod == null) {
                            getItemMethod = XposedHelpers2.findMethodExactIfExists(adapter::class.java.superclass, "getItem", Integer.TYPE)
                        }
                        if (getItemMethod != null) {
                            hookListViewGetItem(getItemMethod!!)
                            isHookGetItemMethod = true
                        } else {
                            LogUtil.w("guess getItem method is ", getItemMethod)
                        }
                    }
                }
            }
        )

    }

    private fun hookListViewGetItem(getItemMethod: Method) {
        LogUtil.d(">>>>>>>>>>.", getItemMethod)

        XposedHelpers2.hookMethod(
            getItemMethod,
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val itemData: Any = param.result ?: return
                    LogUtil.v("item-data", GsonUtil.toJson(itemData))
                    val chatUser: String? = XposedHelpers2.getObjectField(itemData, "field_username")
                    if (chatUser == null) {
                        LogUtil.w("chat user is null")
                        return
                    }
                    if (WXMaskPlugin.containChatUser(chatUser)) {
                        if (ConfigUtil.getOptionData().enableMapConversation) {
                            var maskBean = WXMaskPlugin.getMaskBeamById(chatUser)?.let {
                                XposedHelpers2.setObjectField(itemData, "field_username", it.mapId)
                            }

                        }
                        XposedHelpers2.setObjectField(itemData, "field_content", "")
                        XposedHelpers2.setObjectField(itemData, "field_digest", "")
                        XposedHelpers2.setObjectField(itemData, "field_unReadCount", 0)
                        XposedHelpers2.setObjectField(itemData, "field_UnReadInvite", 0)
                        XposedHelpers2.setObjectField(itemData, "field_unReadMuteCount", 0)
                        //文本消息
                        XposedHelpers2.setObjectField(itemData, "field_msgType", "1")

//                        try {
//                            var cTime = XposedHelpers2.getObjectField<Long>(itemData, "field_conversationTime")
//                            if (cTime != null) {
//                                val cTime2 = cTime - Constrant.ONE_YEAR_MILLS
//                                XposedHelpers2.setObjectField(itemData, "field_flag", cTime2)
//                                XposedHelpers2.setObjectField(itemData, "field_conversationTime", cTime2)
//                            }
//                        } catch (e: Exception) {
//                        }

                    }


                }

            }
        )
    }


}