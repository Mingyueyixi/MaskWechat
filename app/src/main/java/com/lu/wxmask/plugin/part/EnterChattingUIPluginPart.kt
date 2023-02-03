package com.lu.wxmask.plugin.part

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.ReflectUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.plugin.WXConfigPlugin
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.QuickCountClickListenerUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 聊天页页面处理：
 * 1、隐藏单聊/群聊聊天记录
 */
class EnterChattingUIPluginPart() : IPlugin {
    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        handleChattingUIFragment(context, lpparam)
    }

    private fun handleChattingUIFragment(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
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
            LogUtil.w("经过遍历查找，仍然不支持的版本：", AppVersionUtil.getVersionName())
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

}

class EnterChattingHookAction(
    val context: Context,
    val lpparam: XC_LoadPackage.LoadPackageParam,
    val tagConst: String
) {
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
        val maskItem = try {
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

            val quick = MaskItemBean.QuickTemporary.from(maskItem)
            QuickCountClickListenerUtil.register(chatListView.parent as? View?, quick.clickCount, quick.duration) {
                chatListView.visibility = View.VISIBLE
            }
            LogUtil.i("hide chatListView")
        } else {
            hideListViewUIByMask(fragmentObj)
        }

        if (Constrant.WX_MASK_TIP_MODE_SILENT == maskItem.tipMode) {
            // 静默模式，不弹提示框
        } else if (Constrant.CONFIG_TIP_MODE_ALERT == maskItem.tipMode) {
            handleAlertMode(activity, maskItem)
        }

    }

    private fun handleAlertMode(uiContext: Context, item: MaskItemBean) {
        //提示模式
        AlertDialog.Builder(uiContext)
            .setMessage(MaskItemBean.TipData.from(item).mess)
            .setNegativeButton("知道了", null)
            .show()
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

class DoResumeAction(context: Context, lpparam: XC_LoadPackage.LoadPackageParam, tagConst: String) {
    fun handle(param: XC_MethodHook.MethodHookParam) {
        PluginProviders.from(WXConfigPlugin::class.java).doResumeHookAction(param)
    }

}


