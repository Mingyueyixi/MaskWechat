package com.lu.wxmask.plugin.part

import android.content.Context
import android.text.TextUtils
import android.util.LruCache
import android.util.SparseArray
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.lposed.plugin.PluginProviders
import com.lu.magic.util.AppUtil
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ReflectUtil
import com.lu.magic.util.TextUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.Constrant
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.dev.DebugUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Field
import java.sql.Ref

/**
 * 隐藏搜索列表
 */
class HideSearchListUIPluginPart : IPlugin {
    private val hideFieldInfoCache = HashMap<String, HashSet<Field>>()
    private val jsonResultLruCache = LruCache<String, CharSequence>(16)

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        if (AppVersionUtil.getVersionCode() < Constrant.WX_CODE_8_0_44) {
            handleGlobalSearch(context, lpparam)
            handleDetailSearch(context, lpparam)
            return
        }

        val getItemMethod = when (AppVersionUtil.getVersionCode()) {
            Constrant.WX_CODE_8_0_44 -> "h"
            else -> "i"
        }
//        LogUtil.d("getItem name:", getItemMethod)
        //hook getItem --> rename to h
        XposedHelpers2.findAndHookMethod("com.tencent.mm.plugin.fts.ui.a0",
            context.classLoader,
            getItemMethod,
            java.lang.Integer.TYPE,
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
//                    LogUtil.d("search hide afterMethod")
                    if (needHideUserName2(param, param.result)) {
                        LogUtil.d("search hide", param.result)
//                        param.result = try {
//                            //将命中的用户数据抹除掉
//                            param.result::class.java.newInstance()
//                        } catch (e: Throwable) {
//                            LogUtil.d("error new Instance, return null")
//                            null
//                        }
                        var f: SparseArray<*> = XposedHelpers2.getObjectField(param.thisObject, "f")
                        param.result = f.get(0)
                    }
                }
            });

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

    private fun handleDetailSearch(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        var hookClazzName = when (AppVersionUtil.getVersionCode()) {
            in Constrant.WX_CODE_8_0_38..Constrant.WX_CODE_8_0_41 -> "com.tencent.mm.plugin.fts.ui.x"
            else -> "com.tencent.mm.plugin.fts.ui.y"
        }
        //全局搜索详情置空
        XposedHelpers2.findAndHookMethod(
            hookClazzName,
            context.classLoader,
            "d",
            Integer.TYPE,
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (needHideUserName2(param, param.result)) {
                        LogUtil.d(param.result)
                        param.result = try {
                            //将命中的用户数据抹除掉
                            param.result::class.java.newInstance()
                        } catch (e: Throwable) {
                            LogUtil.d("error new Instance, return null")
                            null
                        }
                    }

                }

            }
        )
    }

    private fun handleGlobalSearch(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        //        val wxVersionCode = AppVersionUtil.getVersionCode()
        // 理论上 hook com.tencent.mm.plugin.fts.ui.z#getItem 也是一样的，但是被覆盖重命名了
        var hookClazzName = when (AppVersionUtil.getVersionCode()) {
            in Constrant.WX_CODE_8_0_38..Constrant.WX_CODE_8_0_43 -> "com.tencent.mm.plugin.fts.ui.y"
            else -> "com.tencent.mm.plugin.fts.ui.z"
        }
        //全局搜索首页
        XposedHelpers2.findAndHookMethod(
            hookClazzName,
            context.classLoader,
            "d",
            Integer.TYPE,
            object : XC_MethodHook2() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (needHideUserName(param, param.result)) {
                        LogUtil.d(param.result)
                        param.result = runCatching {
                            //将命中的用户数据抹除掉
                            param.result::class.java.newInstance()
                        }.getOrElse {
                            LogUtil.w("error new Instance, return null")
                            null
                        }
                    }

                }
            }
        )
    }

    private fun needHideUserName(param: XC_MethodHook.MethodHookParam, itemData: Any?): Boolean {
        if (itemData == null) {
            return false
        }
        if (!ConfigUtil.getOptionData().hideMainSearch) {
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

            val fieldName = when (AppVersionUtil.getVersionCode()) {
                Constrant.WX_CODE_8_0_40 -> "q1"
                else -> "q"
            }
            XposedHelpers2.getObjectField<String?>(itemData, fieldName)

        } catch (e: Throwable) {
            null
        }
        if (chatUser == null) {
            when (AppVersionUtil.getVersionCode()) {
                in Constrant.WX_CODE_8_0_33..Constrant.WX_CODE_8_0_41 -> {
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
                LogUtil.d("need hide user from search result list after", chatUser)
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


    fun needHideUserName2(param: XC_MethodHook.MethodHookParam, itemData: Any?): Boolean {
        if (itemData == null) {
            return false
        }
        if (!ConfigUtil.getOptionData().hideMainSearch) {
            return false
        }
        var clazz: Class<*>? = itemData.javaClass ?: return false
        if (hideFieldInfoCache[clazz!!.name] != null) {
            for (field in hideFieldInfoCache[clazz.name]!!) {
                if (checkFieldNeedHide(itemData, field)) {
                    LogUtil.d("hide field from cache: ", field.type.name, field.name, field.get(itemData))
                    return true
                }
            }
            return false
        }
        while (clazz != null) {
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                try {
                    if (checkFieldNeedHide(itemData, field)) {
                        LogUtil.d("hide field: ", field.type.name, field.name, field.get(itemData))
                        return true
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            clazz = try {
                clazz.superclass
            } catch (e: Exception) {
                break
            }
        }
        return false
    }

    private fun checkFieldNeedHide(itemData: Any, field: Field): Boolean {
        var fieldValue: Any? = field.get(itemData) ?: return false
        var clazzName = field.type.name
        if (field.type.isAssignableFrom(Number::class.java)
            || field.type.isAssignableFrom(Byte::class.java)
            || clazzName.startsWith("android")
        ) {
            return false
        }

        var jsonKey = fieldValue.toString().hashCode().toString()

        var compareText = if (fieldValue is CharSequence) {
            fieldValue
        } else {
            if (jsonResultLruCache[jsonKey] == null) {
                GsonUtil.toJson(fieldValue)
            } else {
                jsonResultLruCache[jsonKey]
            }
        }
        if (compareText.isBlank()) {
            return false
        }
        jsonResultLruCache.put(jsonKey, compareText)

        for (wxid in PluginProviders.from(WXMaskPlugin::class.java).maskIdList) {
            if (TextUtils.isEmpty(wxid) || TextUtils.isEmpty(wxid?.trim())) {
                continue
            }
            if (compareText.contains(wxid!!)) {
                putField2Cache(itemData::class.java.name, field)
                LogUtil.d("hit wxid compareText: ", compareText)
                return true
            }
        }
        return false

    }

    private fun putField2Cache(itemClassName: String, field: Field) {
        var pool = hideFieldInfoCache[itemClassName]
        if (pool == null) {
            pool = hashSetOf(field)
            hideFieldInfoCache[itemClassName] = pool
        } else {
            pool.add(field)
        }
    }
}