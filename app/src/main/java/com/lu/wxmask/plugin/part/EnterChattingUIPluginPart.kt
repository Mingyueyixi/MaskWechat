package com.lu.wxmask.plugin.part

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.gson.JsonObject
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.ReflectUtil
import com.lu.magic.util.ResUtil
import com.lu.magic.util.kxt.toElseEmptyString
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.view.ChildDeepCheck
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.bean.QuickTemporaryBean
import com.lu.wxmask.plugin.WXConfigPlugin
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.QuickCountClickListenerUtil
import com.lu.wxmask.util.ext.getViewId
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 聊天页页面处理（8.0.76 混淆原语闭环版本）：
 * 
 * 逆向还原微信底层真实方法名：
 * 1. onEnterBegin 混淆名为 M0() -> 进聊天框必走 M0()！
 * 2. onExitBegin  混淆名为 O0() -> 离开聊天框必走 O0()！
 * 3. 任何时候通过 M0/O0 闭环管理私密用户遮罩与临时解锁，彻底解决主页进出不锁问题！
 */
class EnterChattingUIPluginPart() : IPlugin {

    companion object {
        const val TAG_MASK_VIEW = "chatting-onEnterBegin"
        @Volatile
        var currentChattingUser: String? = null
        val unlockedUsers = HashSet<String>()
    }

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        val enterAction = EnterChattingHookAction(context, lpparam, TAG_MASK_VIEW)
        val chattingUIFragmentClazz = ClazzN.from("com.tencent.mm.ui.chatting.ChattingUIFragment", context.classLoader)
            ?: ClazzN.from(ClazzN.BaseChattingUIFragment, context.classLoader)

        // 1. Hook onEnterBegin -> 混淆名 M0()
        runCatching {
            XposedHelpers2.findAndHookMethod(
                chattingUIFragmentClazz,
                "M0",
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LogUtil.i("ChattingUIFragment.M0 (onEnterBegin) triggered")
                        enterAction.handle(param.thisObject)
                    }
                }
            )
            LogUtil.i("Successfully hooked M0 (onEnterBegin)")
        }.onFailure {
            LogUtil.w("Hook M0 failed", it)
        }

        // 2. Hook onExitBegin -> 混淆名 O0()
        runCatching {
            XposedHelpers2.findAndHookMethod(
                chattingUIFragmentClazz,
                "O0",
                object : XC_MethodHook2() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        LogUtil.i("ChattingUIFragment.O0 (onExitBegin) triggered -> reset lock!")
                        unlockedUsers.clear()
                        currentChattingUser = null
                    }
                }
            )
            LogUtil.i("Successfully hooked O0 (onExitBegin)")
        }.onFailure {
            LogUtil.w("Hook O0 failed", it)
        }

        // 3. 补充 onActivityCreated 作为初次冷启动保障
        runCatching {
            XposedHelpers2.findAndHookMethod(
                ClazzN.BaseChattingUIFragment,
                context.classLoader,
                "onActivityCreated",
                Bundle::class.java,
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        enterAction.handle(param.thisObject)
                    }
                })
        }
    }
}

class EnterChattingHookAction(
    val context: Context,
    val lpparam: XC_LoadPackage.LoadPackageParam,
    val tagConst: String
) {
    fun handle(fragmentObj: Any) {
        val activity = ReflectUtil.invokeMethod(fragmentObj, "getActivity") as Activity? ?: return

        // 精准提取微信号：从 Fragment 的 getStringExtra("Chat_User") 或 arguments 提取
        var chatUser: String? = runCatching {
            XposedHelpers2.callMethod<String?>(fragmentObj, "getStringExtra", "Chat_User")
        }.getOrNull()

        if (chatUser.isNullOrEmpty()) {
            try {
                val arguments = ReflectUtil.invokeMethod(fragmentObj, "getArguments") as Bundle?
                chatUser = arguments?.getString("Chat_User")
            } catch (e: Throwable) {
            }
        }

        if (chatUser.isNullOrEmpty()) {
            try {
                chatUser = activity.intent?.getStringExtra("Chat_User")
            } catch (e: Throwable) {
            }
        }

        LogUtil.i("enter chattingUI action handle, resolved user: $chatUser")
        EnterChattingUIPluginPart.currentChattingUser = chatUser

        if (!chatUser.isNullOrEmpty() && WXMaskPlugin.containChatUser(chatUser)) {
            if (EnterChattingUIPluginPart.unlockedUsers.contains(chatUser)) {
                // 本次已临时解锁 -> 正常显示
                showChatListUI(fragmentObj)
            } else {
                // 强制遮挡
                hideChatListUI(fragmentObj, activity, chatUser)
            }
        } else {
            // 普通好友 -> 正常显示
            showChatListUI(fragmentObj)
        }

        if (!chatUser.isNullOrEmpty()) {
            handleUserInputMagic(activity, fragmentObj, chatUser)
            handleShowAddMaskDialog(activity, fragmentObj, chatUser)
        }
    }

    private fun handleShowAddMaskDialog(activity: Activity, fragmentObj: Any, chatUser: String) {
        PluginProviders.from(WXConfigPlugin::class.java).checkShowAddMaskConfigDialog(fragmentObj)
    }

    private fun handleUserInputMagic(activity: Activity, fragmentObj: Any, chatUser: String) {
        val userInputView: EditText = getUserChatEditText(fragmentObj) ?: return
        if (!ConfigUtil.getOptionData().enableChattingKey) return

        userInputView.addTextChangedListener {
            val editable = it ?: return@addTextChangedListener
            val text = editable.toElseEmptyString()
            when (text) {
                "#add" -> {
                    PluginProviders.from(WXConfigPlugin::class.java).showAddMaskDialog(userInputView.context, fragmentObj)
                    editable.clear()
                }
                "#del" -> {
                    AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage("是否移除wxid:$chatUser")
                        .setNegativeButton("确定") { _, _ ->
                            ConfigUtil.removeMaskItem(chatUser)
                            showChatListUI(fragmentObj)
                        }
                        .setNeutralButton("取消") { _, _ ->
                            editable.clear()
                        }
                        .show()
                    editable.clear()
                }
                "#clear" -> {
                    AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage("是否清空所有配置")
                        .setNegativeButton("确定") { _, _ ->
                            ConfigUtil.clearData()
                        }
                        .setNeutralButton("取消") { _, _ ->
                            editable.clear()
                        }
                        .show()
                    editable.clear()
                }
            }
        }
    }

    private fun getUserChatEditText(fragmentObj: Any): EditText? {
        return XposedHelpers2.callMethod<View?>(fragmentObj, "findViewById", ResUtil.getViewId("bkk"))?.let {
            ChildDeepCheck().filter(it) { child ->
                child is EditText
            }?.firstOrNull() as? EditText
        }
    }

    private fun findChatListView(fragmentObj: Any): View? {
        var listView = runCatching {
            ReflectUtil.invokeMethod(fragmentObj, "getListView") as View
        }.getOrNull()
        if (listView == null) {
            listView = runCatching {
                val mmListViewId =
                    if (AppVersionUtil.getVersionCode() < Constrant.WX_CODE_8_0_42) {
                        ResUtil.getViewId("b5n")
                    } else if (AppVersionUtil.getVersionCode() == Constrant.WX_CODE_PLAY_8_0_42) {
                        ResUtil.getViewId("bnu")
                    } else {
                        ResUtil.getViewId("bm6")
                    }
                XposedHelpers2.callMethod(fragmentObj, "findViewById", mmListViewId) as View
            }.getOrNull()
        }
        if (listView == null) {
            listView = runCatching {
                val MMListViewClazz = XposedHelpers2.findClassIfExists(
                    "com.tencent.mm.ui.chatting.view.MMChattingListView",
                    context.classLoader
                )
                if (MMListViewClazz != null) {
                    val mmListViewField = XposedHelpers2.findFirstFieldByExactType(fragmentObj.javaClass, MMListViewClazz)
                    mmListViewField.get(fragmentObj) as View
                } else null
            }.getOrNull()
        }
        return listView
    }

    private fun showChatListUI(fragmentObj: Any) {
        val chatListView: View? = findChatListView(fragmentObj)
        if (chatListView != null) {
            chatListView.visibility = View.VISIBLE
            QuickCountClickListenerUtil.unRegister(chatListView.parent as? View?)
        }
        showChatListUIFromMask(fragmentObj)
    }

    private fun showChatListUIFromMask(fragmentObj: Any) {
        val contentView = ReflectUtil.invokeMethod(fragmentObj, "getView") as? ViewGroup?
        val maskView = contentView?.findViewWithTag<View?>(tagConst)
        if (maskView != null) {
            (maskView.parent as? ViewGroup)?.removeView(maskView)
        }
    }

    private fun hideChatListUI(fragmentObj: Any, activity: Activity, chatUser: String) {
        val maskItem = try {
            ConfigUtil.getMaskList().first { it.maskId == chatUser }
        } catch (e: Exception) {
            return
        }

        val chatListView = findChatListView(fragmentObj)
        if (chatListView != null) {
            chatListView.visibility = View.INVISIBLE

            val quick = QuickTemporaryBean(ConfigUtil.getTemporaryJson() ?: JsonObject())
            QuickCountClickListenerUtil.register(chatListView.parent as? View?, quick.clickCount, quick.duration) {
                chatListView.visibility = View.VISIBLE
                EnterChattingUIPluginPart.unlockedUsers.add(chatUser)
            }
            LogUtil.i("hide chatListView by setVisible for $chatUser")
        } else {
            hideListViewUIByMask(fragmentObj)
        }

        if (Constrant.CONFIG_TIP_MODE_ALERT == maskItem.tipMode) {
            handleAlertMode(activity, maskItem)
        }
    }

    private fun handleAlertMode(uiContext: Context, item: MaskItemBean) {
        AlertDialog.Builder(uiContext)
            .setTitle("提示")
            .setIcon(uiContext.applicationInfo.icon)
            .setMessage(MaskItemBean.TipData.from(item).mess)
            .setNegativeButton("知道了", null)
            .show()
    }

    private fun hideListViewUIByMask(fragmentObj: Any) {
        val contentView = ReflectUtil.invokeMethod(fragmentObj, "getView") as? ViewGroup?
        contentView?.let {
            val pvId =
                if (AppVersionUtil.getVersionCode() == Constrant.WX_CODE_PLAY_8_0_42) {
                    ResUtil.getViewId("bm7")
                } else {
                    ResUtil.getViewId("b49")
                }
            val pv = it.findViewById<View?>(pvId)
            val vParent = (pv?.parent as? ViewGroup?) ?: it
            val maskView = vParent.findViewWithTag<View?>(tagConst)
            if (maskView != null) {
                (maskView.parent as? ViewGroup)?.removeView(maskView)
            }
            vParent.addView(
                View(it.context).apply {
                    tag = tagConst
                    background = ColorDrawable(0xFFEDEDED.toInt())
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            )
        }
    }
}