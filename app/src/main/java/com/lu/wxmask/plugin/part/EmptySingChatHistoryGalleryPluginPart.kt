package com.lu.wxmask.plugin.part

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method

/**
 * 置空单聊页面菜单的“查找聊天记录”搜索结果
 */
class EmptySingChatHistoryGalleryPluginPart : IPlugin {
    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        setEmptyDetailHistoryUI(context, lpparam)
        setEmptyActionBarTabPageUI(context, lpparam)
    }

    private fun setEmptyDetailHistoryUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        setEmptyDetailHistoryUIForMedia(context, lpparam)
        setEmptyDetailHistoryUIForGallery(context, lpparam)
    }

    private fun setEmptyDetailHistoryUIForMedia(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        var mediaMethodName = when (AppVersionUtil.getVersionCode()) {
            in Constrant.WX_CODE_8_0_32..Constrant.WX_CODE_8_0_35 -> "k"
            in Constrant.WX_CODE_8_0_35..Constrant.WX_CODE_8_0_37 -> "l"
            else -> "l"
        }
        val MediaHistoryListUI = "com.tencent.mm.ui.chatting.gallery.MediaHistoryListUI"
        var mediaMethod: Method? = XposedHelpers2.findMethodExactIfExists(
            MediaHistoryListUI,
            context.classLoader,
            mediaMethodName,
            java.lang.Boolean.TYPE,
            java.lang.Integer.TYPE
        )

        if (mediaMethod == null) {
            val guessMethods = XposedHelpers2.findMethodsByExactParameters(
                ClazzN.from(MediaHistoryListUI),
                Void.TYPE,
                java.lang.Boolean.TYPE,
                Integer.TYPE
            )
            if (guessMethods.size >= 1) {
                mediaMethod = guessMethods[0]
            }
            LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess MediaHistoryListUI empty method is ", mediaMethod)
        }
        if (mediaMethod == null) {
            return
        }
        XposedHelpers2.hookMethod(mediaMethod, object : XC_MethodHook2() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity: Activity = param.thisObject as Activity
                val intent = activity.intent
                val userName = intent.getStringExtra("kintent_talker")
                if (userName.isNullOrBlank()) {
                    LogUtil.w("MediaHistoryListUI‘s user is empty", userName)
                    return
                }
                if (WXMaskPlugin.containChatUser(userName)) {
                    param.args[1] = 0
                    LogUtil.i("empty MediaHistoryListUI data")
                }
            }
        })
    }

    private fun setEmptyDetailHistoryUIForGallery(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        val MediaHistoryGalleryUI = "com.tencent.mm.ui.chatting.gallery.MediaHistoryGalleryUI"
        val methodName = when (AppVersionUtil.getVersionCode()) {
            in Constrant.WX_CODE_8_0_22..Constrant.WX_CODE_8_0_35 -> "k"
            Constrant.WX_CODE_8_0_37 -> "l"
            else -> null
        }
        var galleryMethod: Method? = null
        if (methodName != null) {
            galleryMethod = XposedHelpers2.findMethodExactIfExists(
                MediaHistoryGalleryUI,
                context.classLoader,
                methodName,
                java.lang.Boolean.TYPE,
                java.lang.Integer.TYPE,
            )
        }
        if (galleryMethod == null) {
            val guessMethods = XposedHelpers2.findMethodsByExactParameters(
                ClazzN.from(MediaHistoryGalleryUI),
                Void.TYPE,
                java.lang.Boolean.TYPE,
                java.lang.Integer.TYPE,
            )
            if (guessMethods.isNotEmpty()) {
                galleryMethod = guessMethods[0]
            }
            LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess MediaHistoryGalleryUI empty method is ", galleryMethod)
        }

        XposedHelpers2.hookMethod(
            galleryMethod,
            object : XC_MethodHook2() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val activity: Activity = param.thisObject as Activity
                    val intent = activity.intent
                    val userName = intent.getStringExtra("kintent_talker")
                    if (userName.isNullOrBlank()) {
                        LogUtil.w("MediaHistoryListUI‘s user is empty", userName)
                        return
                    }
                    if (WXMaskPlugin.containChatUser(userName)) {
                        param.args[1] = 0
                        LogUtil.i("empty MediaHistoryGalleryUI data")
                    }
                }
            })

    }


    /**
     * 处理通过顶部ActionBar搜索框进行的结果
     */
    private fun setEmptyActionBarTabPageUI(context: Context, lpparam: XC_LoadPackage.LoadPackageParam?) {
        val Clazz_FTSMultiAllResultFragment = "com.tencent.mm.ui.chatting.search.multi.fragment.FTSMultiAllResultFragment"
        var commonHookMethodName: String? = when (AppVersionUtil.getVersionCode()) {
            Constrant.WX_CODE_8_0_32 -> "N"
            Constrant.WX_CODE_8_0_33 -> "O"
            Constrant.WX_CODE_8_0_34 -> {
                if (AppVersionUtil.getVersionName() == "8.0.35") "P"
                else "R"
            }
            Constrant.WX_CODE_8_0_35 -> "P"
            Constrant.WX_CODE_8_0_37 -> "Q"
            else -> null
        }
        LogUtil.d("setEmptyActionBarTabPageUI method is :", commonHookMethodName)
        var preHookMethod: Method? = null
        if (commonHookMethodName != null) {
            preHookMethod = XposedHelpers2.findMethodExactIfExists(
                ClazzN.from(Clazz_FTSMultiAllResultFragment),
                commonHookMethodName,
                java.util.ArrayList::class.java
            )
        }
        if (preHookMethod == null) {
            val methods = XposedHelpers2.findMethodsByExactParameters(
                ClazzN.from(Clazz_FTSMultiAllResultFragment),
                Void.TYPE,
                java.util.ArrayList::class.java
            )
            if (methods.isNotEmpty()) {
                preHookMethod = methods[0]
                commonHookMethodName = methods[0].name
            }
            LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess setEmptyActionBarTabPageUI method:", preHookMethod)
        }

        if (preHookMethod == null) {
            LogUtil.w(AppVersionUtil.getSmartVersionName(), "setEmptyActionBarTabPageUI is method null")
            return
        }

        //tab==全部，搜索结果置空
        XposedHelpers2.hookMethod(
            preHookMethod,
            object : XC_MethodHook2() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    debugLog(param)
                    if (isHitMaskId(param.thisObject)) {
                        val arrayList: java.util.ArrayList<*> = param.args[0] as java.util.ArrayList<*>
                        arrayList.clear()
                    }
                }
            }
        )
        if (commonHookMethodName == null) {
            LogUtil.i("setEmptyActionBarTabPageUI is null")
            return
        }
        //其他的/普通的/一般的tab，搜索结果置空
        XposedHelpers2.findAndHookMethod(
            "com.tencent.mm.ui.chatting.search.multi.fragment.FTSMultiNormalResultFragment",
            context.classLoader,
            commonHookMethodName,
            java.util.ArrayList::class.java,
            object : XC_MethodHook2() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    debugLog(param)
                    if (isHitMaskId(param.thisObject)) {
                        val arrayList: java.util.ArrayList<*> = param.args[0] as java.util.ArrayList<*>
                        arrayList.clear()
                    }
                }
            }
        )

//        tab==图片，全体视图替换置空
        XposedHelpers2.findAndHookMethod(
            "com.tencent.mm.ui.chatting.search.multi.fragment.FTSMultiImageResultFragment",
            context.classLoader,
            "onCreateView",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Bundle::class.java,
            object : XC_MethodHook2() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    debugLog(param)
                    if (isHitMaskId(param.thisObject)) {
                        val inflater = param.args[0] as LayoutInflater
                        val viewGroup: ViewGroup = param.args[1] as ViewGroup
                        val layoutId = XposedHelpers2.callMethod<Int>(param.thisObject, "getLayoutId")
                        param.result = inflater.inflate(layoutId, viewGroup, false)
                    }
                }
            }
        )
    }

    private fun debugLog(param: XC_MethodHook.MethodHookParam) {
        LogUtil.d(
            "set empty for ${param.thisObject}",
            "hook method args:",
            param.args,
            "fragment arguments:",
            XposedHelpers2.callMethod(param.thisObject, "getArguments"),
        )
    }

    private fun isHitMaskId(fragmentObj: Any?): Boolean {
        val activity = XposedHelpers2.callMethod<Activity>(fragmentObj, "getActivity") as Activity?
        if (activity == null) {
            LogUtil.w("Not attach Activity for ", fragmentObj)
            return false
        }
        val intent = activity.intent
        LogUtil.d(activity, activity.intent.extras)

        val username = intent.getStringExtra("detail_username")
        return WXMaskPlugin.containChatUser(username)
    }
}