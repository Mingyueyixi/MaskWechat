package com.lu.wxmask.plugin.part

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.ReflectUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.ConfigUtil
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 单聊页面“查找聊天记录”（日期、图片/视频、文件、表情、多标签搜索）智能处理：
 * 
 * 规则：
 * 1. 只有未解锁状态下，进入查询页面才拦截显示“无结果”；
 * 2. 一旦用户在聊天界面点击完成了临时解锁（EnterChattingUIPluginPart.unlockedUsers 包含当前好友），
 *    则所有查询功能（按日期查找、图片/视频、文件、表情、聊天记录搜索等）完全放行，正常展示真实数据！
 * 3. 未解锁状态下：
 *    - 允许正常打开日期选择界面（SelectDateUI），日历主体隐藏，提示“无内容”
 *    - 允许打开多媒体历史界面（MediaHistoryGalleryUI / MediaHistoryListUI），数据加载方法拦截置空，提示“无内容”
 *    - 允许打开表情历史界面（EmojiHistoryListUI / EmojiHistoryListFragment），拦截 s0(List) 置空，提示“无内容”
 *    - 多标签搜索结果（FTSMultiAllResultFragment 等）清空列表，展示“无结果”
 */
class EmptySingChatHistoryGalleryPluginPart : IPlugin {
    val MediaHistoryGalleryUI = "com.tencent.mm.ui.chatting.gallery.MediaHistoryGalleryUI"
    val MediaHistoryListUI = "com.tencent.mm.ui.chatting.gallery.MediaHistoryListUI"
    val EmojiHistoryListUI = "com.tencent.mm.ui.chatting.gallery.EmojiHistoryListUI"
    val EmojiHistoryListFragment = "com.tencent.mm.ui.chatting.gallery.EmojiHistoryListFragment"
    val SelectDateUI = "com.tencent.mm.chatroom.ui.SelectDateUI"

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        handleImageQueryMainUI(context, lpparam)
        handleSelectDateUI(context, lpparam)
        handleMediaHistoryUI(context, lpparam)
        handleEmojiHistoryUI(context, lpparam)
        setEmptyActionBarTabPageUI(context, lpparam)
    }

    /**
     * 判断当前操作的用户是否处于“锁定”状态（是私密好友，且未在当前被临时解锁）
     */
    private fun isUserLocked(userName: String?): Boolean {
        if (userName.isNullOrBlank()) return false
        if (!WXMaskPlugin.containChatUser(userName)) return false
        // 如果当前好友已被临时解锁，则放行
        if (EnterChattingUIPluginPart.unlockedUsers.contains(userName)) {
            LogUtil.i("User $userName is currently temporary unlocked, allow querying chat history")
            return false
        }
        return true
    }

    private fun handleImageQueryMainUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        val ImageQueryMainUI = ClazzN.from("com.tencent.mm.view.activity.ImageQueryMainUI") ?: return
        XposedHelpers2.findAndHookMethod(
            ImageQueryMainUI,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook2() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val act: Activity = param.thisObject as Activity
                    val userName = act.intent?.getStringExtra("Chat_User")
                        ?: act.intent?.getStringExtra("detail_username")
                    if (isUserLocked(userName)) {
                        act.finish()
                    }
                }
            }
        )
    }

    /**
     * 1. 拦截“按日期查找聊天记录”
     */
    private fun handleSelectDateUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        try {
            XposedHelpers2.findAndHookMethod(
                SelectDateUI,
                context.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? Activity ?: return
                        val intent = activity.intent ?: return
                        val userName = intent.getStringExtra("detail_username")
                            ?: intent.getStringExtra("kintent_talker")
                            ?: intent.getStringExtra("Chat_User")

                        LogUtil.i("SelectDateUI onCreate, talker: $userName, isLocked: ${isUserLocked(userName)}")
                        if (isUserLocked(userName)) {
                            applyDateEmptyUI(activity)
                        }
                    }
                }
            )

            val lcClazz = XposedHelpers2.findClassIfExists("com.tencent.mm.chatroom.ui.lc", context.classLoader)
            if (lcClazz != null) {
                XposedHelpers2.findAndHookMethod(
                    lcClazz,
                    "run",
                    object : XC_MethodHook2() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val uiObj = XposedHelpers2.getObjectField(param.thisObject, "d") as? Activity ?: return
                            val intent = uiObj.intent ?: return
                            val userName = intent.getStringExtra("detail_username")
                                ?: intent.getStringExtra("kintent_talker")
                                ?: intent.getStringExtra("Chat_User")
                            if (isUserLocked(userName)) {
                                LogUtil.i("SelectDateUI.lc.run finished, force empty for $userName")
                                applyDateEmptyUI(uiObj)
                            }
                        }
                    }
                )
            }
        } catch (e: Throwable) {
            LogUtil.w("Hook SelectDateUI error", e)
        }
    }

    private fun applyDateEmptyUI(activity: Activity) {
        runCatching {
            val dateMap = XposedHelpers2.getObjectField(activity, "f") as? java.util.HashMap<*, *>
            dateMap?.clear()

            val dayPickerView = XposedHelpers2.getObjectField(activity, "d") as? View
            dayPickerView?.visibility = View.GONE

            val emptyTv = XposedHelpers2.getObjectField(activity, "m") as? TextView
            emptyTv?.visibility = View.VISIBLE
            emptyTv?.text = "无内容"
        }
    }

    /**
     * 2. 拦截图片/视频历史（MediaHistoryGalleryUI）与文件/链接等历史（MediaHistoryListUI）
     */
    private fun handleMediaHistoryUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        // 直接拦截媒体 Presenter (n3) 的核心数据加载方法 j(boolean, int)
        try {
            val n3Clazz = XposedHelpers2.findClassIfExists("com.tencent.mm.ui.chatting.presenter.n3", context.classLoader)
            if (n3Clazz != null) {
                XposedHelpers2.findAndHookMethod(
                    n3Clazz,
                    "j",
                    Boolean::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook2() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val talker = XposedHelpers2.getObjectField(param.thisObject, "g") as? String
                            if (isUserLocked(talker)) {
                                LogUtil.i("Intercept n3.j for locked talker: $talker, block data loading")
                                param.result = null
                            }
                        }
                    }
                )
            }
        } catch (e: Throwable) {
            LogUtil.w("Hook presenter n3 error", e)
        }

        val activities = listOf(
            MediaHistoryGalleryUI,
            MediaHistoryListUI
        )

        activities.forEach { clazzName ->
            try {
                XposedHelpers2.findAndHookMethod(
                    clazzName,
                    context.classLoader,
                    "initView",
                    object : XC_MethodHook2() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val activity = param.thisObject as? Activity ?: return
                            val intent = activity.intent ?: return
                            val userName = intent.getStringExtra("kintent_talker")
                                ?: intent.getStringExtra("detail_username")
                                ?: intent.getStringExtra("Chat_User")
                                ?: intent.getStringExtra("RoomInfo_Id")

                            if (isUserLocked(userName)) {
                                runCatching {
                                    val recyclerView = (XposedHelpers2.getObjectField(activity, "f") as? View)
                                        ?: (XposedHelpers2.getObjectField(activity, "g") as? View)
                                    recyclerView?.visibility = View.GONE

                                    val emptyTv = (XposedHelpers2.getObjectField(activity, "g") as? TextView)
                                        ?: (XposedHelpers2.getObjectField(activity, "m") as? TextView)
                                    emptyTv?.visibility = View.VISIBLE
                                    emptyTv?.text = "无内容"
                                }
                            }
                        }
                    }
                )
            } catch (e: Throwable) {
                LogUtil.w("Hook $clazzName error", e)
            }
        }
    }

    /**
     * 3. 拦截表情历史（EmojiHistoryListUI / EmojiHistoryListFragment）
     */
    private fun handleEmojiHistoryUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        try {
            XposedHelpers2.findAndHookMethod(
                EmojiHistoryListFragment,
                context.classLoader,
                "s0",
                java.util.List::class.java,
                String::class.java,
                object : XC_MethodHook2() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val talker = XposedHelpers2.getObjectField(param.thisObject, "n") as? String
                        if (isUserLocked(talker)) {
                            LogUtil.i("EmojiHistoryListFragment.s0 for locked talker: $talker, clear emoji list!")
                            val list = param.args[0] as? java.util.List<*>
                            list?.clear()
                        }
                    }
                }
            )

            XposedHelpers2.findAndHookMethod(
                EmojiHistoryListFragment,
                context.classLoader,
                "onViewCreated",
                View::class.java,
                Bundle::class.java,
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val talker = XposedHelpers2.getObjectField(param.thisObject, "n") as? String
                        if (isUserLocked(talker)) {
                            runCatching {
                                val recyclerView = XposedHelpers2.getObjectField(param.thisObject, "p") as? View
                                recyclerView?.visibility = View.GONE

                                val emptyTv = XposedHelpers2.getObjectField(param.thisObject, "q") as? TextView
                                emptyTv?.visibility = View.VISIBLE
                                emptyTv?.text = "无内容"
                            }
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            LogUtil.w("Hook EmojiHistoryListFragment error", e)
        }
    }

    /**
     * 4. 拦截多标签搜索页（包含图片、视频、文件搜索等）
     */
    private fun setEmptyActionBarTabPageUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        val hookMethodName = "s0"
        val fragmentsToHook = listOf(
            "com.tencent.mm.ui.chatting.search.multi.fragment.FTSMultiAllResultFragment",
            "com.tencent.mm.ui.chatting.search.multi.fragment.FTSMultiNormalResultFragment"
        )

        fragmentsToHook.forEach { fragClazz ->
            try {
                XposedHelpers2.findAndHookMethod(
                    fragClazz,
                    context.classLoader,
                    hookMethodName,
                    java.util.ArrayList::class.java,
                    object : XC_MethodHook2() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isHitMaskId(param.thisObject)) {
                                LogUtil.i("$fragClazz.$hookMethodName invoked for locked user, clear results!")
                                val arrayList = param.args[0] as? java.util.ArrayList<*>
                                arrayList?.clear()
                            }
                        }
                    }
                )

                XposedHelpers2.findAndHookMethod(
                    fragClazz,
                    context.classLoader,
                    "onActivityCreated",
                    Bundle::class.java,
                    object : XC_MethodHook2() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (isHitMaskId(param.thisObject)) {
                                runCatching {
                                    val recyclerView = XposedHelpers2.getObjectField(param.thisObject, "o") as? View
                                    recyclerView?.visibility = View.GONE

                                    val emptyTv = XposedHelpers2.getObjectField(param.thisObject, "p") as? TextView
                                    emptyTv?.visibility = View.VISIBLE
                                    emptyTv?.text = "无搜索结果"
                                }
                            }
                        }
                    }
                )
            } catch (e: Throwable) {
                LogUtil.w("Hook multi search fragment $fragClazz error", e)
            }
        }
    }

    private fun isHitMaskId(fragmentObj: Any?): Boolean {
        val activity = XposedHelpers2.callMethod<Activity>(fragmentObj, "getActivity") as? Activity ?: return false
        val intent = activity.intent ?: return false
        val username = intent.getStringExtra("detail_username")
            ?: intent.getStringExtra("kintent_talker")
            ?: intent.getStringExtra("Chat_User")
        return isUserLocked(username)
    }
}