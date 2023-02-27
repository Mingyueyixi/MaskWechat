package com.lu.wxmask.plugin.part

import android.content.Context
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.Constrant
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.dev.DebugUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 隐藏搜索列表
 */
class HideSearchListUIPluginPart : IPlugin {
    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        //        val wxVersionCode = AppVersionUtil.getVersionCode()
        // 理论上 hook com.tencent.mm.plugin.fts.ui.z#getItem 也是一样的，但是无效，不清楚原因
        //全局搜索首页
        XposedHelpers2.findAndHookMethod(
            "com.tencent.mm.plugin.fts.ui.z",
            context.classLoader,
            "d",
            Integer.TYPE,
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (needHideUserName(param, param.result)) {
                        LogUtil.w(param.result)
                        param.result = try {
                            //将命中的用户数据抹除掉
                            param.result::class.java.newInstance()
                        } catch (e: Throwable) {
                            LogUtil.w("error new Instance")
                            null
                        }
                    }

                }
            }
        )
        //全局搜索详情置空
        XposedHelpers2.findAndHookMethod(
            "com.tencent.mm.plugin.fts.ui.y",
            context.classLoader,
            "d",
            Integer.TYPE,
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (needHideUserName(param, param.result)) {
                        LogUtil.w(param.result)
                        param.result = try {
                            //将命中的用户数据抹除掉
                            param.result::class.java.newInstance()
                        } catch (e: Throwable) {
                            LogUtil.w("error new Instance")
                            null
                        }
                    }

                }
            }
        )

//        hook adapter getView，因视图复用容易出问题。存在某个item，需要滑动后才消失，原因暂时不明
//        XposedHelpers2.findAndHookMethod(
//            "com.tencent.mm.plugin.fts.ui.m",
//            context.classLoader,
//            "getView",
//            Integer.TYPE,
//            View::class.java,
//            ViewGroup::class.java,
//            object : XC_MethodHook2() {
////                var viewHeight = ViewGroup.LayoutParams.WRAP_CONTENT
//
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    val adapter: BaseAdapter = param.thisObject as BaseAdapter
//                    val position = param.args[0] as Int
//                    val itemData = adapter.getItem(position) as? Any?
//                    val view = param.args[1] as? View ?: return
//                    val parent = param.args[2] as ViewGroup
//
//                    var itemType = adapter.getItemViewType(position)
////                    if (view.layoutParams.height != 1) {
////                        viewHeight = view.layoutParams.height
////                    }
//
//                    if (needHideUserName(param, itemData)) {
//                        //before serResult起到替换函数的作用
//
////                        val lp = view.layoutParams
////                        lp.height = 1
////                        view.layoutParams = lp
////                        view.visibility = View.GONE
//                        param.result = View(parent.context)
//
//                    } else {
////                        param.args[1] = null
////                        val lp = view.layoutParams
////                        lp.height = viewHeight
////                        view.layoutParams = lp
////                        view.visibility = View.VISIBLE
//                        param.args[1] = null
//                    }
//                }
//
//
//            }
//        )


    }

    private fun needHideUserName(param: XC_MethodHook.MethodHookParam, itemData: Any?): Boolean {
        if (itemData == null) {
            return false
        }

// 方法一，列举class。当前使用
// 方法二，遍历要隐藏的ID，在itemData的field.value中发现，即屏蔽
        if (BuildConfig.DEBUG) {
            DebugUtil.printAllFields(itemData)
        }

        var chatUser: String? = try {
//            when (itemData::class.java.name) {
//                //从聊天记录搜索结果取出用户名
//                "wt1.m" -> XposedHelpers2.getObjectField<String?>(itemData, "q")
//                //联系人等
//                "wt1.t" -> XposedHelpers2.getObjectField<String?>(itemData, "q")
//                "wt1.u" -> XposedHelpers2.getObjectField<String?>(itemData, "q")
//                else -> null
//            } ?: return false
            XposedHelpers2.getObjectField<String?>(itemData, "q")
        } catch (e: Throwable) {
            null
        }
        if (chatUser == null) {
            when(AppVersionUtil.getVersionCode()){
                Constrant.WX_CODE_8_0_33 ->{
                    val fieldValue: Any = XposedHelpers2.getObjectField<Any?>(itemData, "p") ?: return false
                    chatUser = XposedHelpers2.getObjectField<String?>(fieldValue, "e")
                }
                Constrant.WX_CODE_8_0_32 -> {
                    val fieldValue: Any = XposedHelpers2.getObjectField<Any?>(itemData, "o") ?: return false
                    chatUser = XposedHelpers2.getObjectField<String?>(fieldValue, "e")
                }
            }
        }
        if (chatUser == null) {
            return false
        }

        return (WXMaskPlugin.containChatUser(chatUser)).also {
            if (it) {
                LogUtil.i("need hide user from search result list after", chatUser)
            }
        }
//        try {
//            var clz: Class<*>? = itemData::class.java
//            do {
//                if (clz == null) {
//                    break
//                }
//                for (field in clz.declaredFields) {
//                    field.isAccessible = true
//                    val v = field.get(itemData) ?: continue
//                    var vText = if (v is CharSequence) {
//                            v.toString()
//                        } else if (v is Number && v is Boolean && v is Byte) {
//                            continue
//                        } else {
//                            try {
//                                GsonUtil.toJson(v)
//                            } catch (e: Throwable) {
//                                null
//                            }
//                        }
//
//                    if (vText.isNullOrBlank()) continue
//                    for (s in maskIdList) {
//                        if (s.isNullOrBlank()) continue
//                        if (vText.indexOf(s) > -1) {
//                            LogUtil.i(s)
//                            return true
//                        }
//                    }
//
//                }
//            } while (clz?.superclass.also { clz = it } != null)
//
//        } catch (e: Exception) {
//            LogUtil.e(e)
//        }
//        return false

    }

}