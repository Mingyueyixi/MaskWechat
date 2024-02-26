package com.lu.wxmask.util

import android.content.Context
import com.lu.lposed.api2.XposedHelpers2
import com.lu.magic.util.GsonUtil
import com.lu.wxmask.bean.PointBean
import com.lu.wxmask.plugin.point.HideSearchListPoint
import com.lu.wxmask.util.ext.toJson
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONObject
import java.lang.reflect.Modifier

class HookPointManager {
    companion object {
        @JvmStatic
        val INSTANCE = HookPointManager()
        const val key_search_pkg_adapter = "com.tencent.mm.plugin.fts.ui:BaseAdapter"
        val KEY_SEARCH_ADAPTER = "search_adapter"
    }

    fun init(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        val keyLocal = getCookPointKey()

        val hookList: HashSet<PointBean> = try {
            GsonUtil.fromJson(
                ConfigUtil.sp.getString(keyLocal, "{}"),
                GsonUtil.getType(HashSet::class.java, PointBean::class.java)
            )
        } catch (e: Exception) {
            HashSet()
        }

        if (hookList.isEmpty()) {
            hookList.addAll(getHookPointByScanner(context))
            ConfigUtil.sp.edit().putString(keyLocal, hookList.toJson()).apply()
        }
        val hideMainSearchStrong = ConfigUtil.getOptionData().hideMainSearchStrong
        hookList.forEach {
            when (it.featId) {
                KEY_SEARCH_ADAPTER -> {
                    if (hideMainSearchStrong) {
                        HideSearchListPoint(it).handleHook(context, lpparam)
                    }
                }
            }
        }

    }

    private fun getCookPointKey(): String {
        return "cookPoint-" + AppVersionUtil.getVersionCode()
    }

//    private fun getHookPointByLocal(): HashSet<PointBean> {
//        val pointJson = JSONObject(ConfigUtil.sp.getString(getCookPointKey(), "{}") ?: "{}")
//        val result = HashSet<PointBean>()
//        pointJson.keys().forEach {
//            val value = pointJson[it]
//            if (value is JSONObject) {
//                result.add(PointBean.fromJson(value))
//            }
//        }
//        return result
//    }


    fun getHookPointByScanner(context: Context): HashSet<PointBean> {
        val classLoader = context.classLoader
        val result = HashSet<PointBean>()
        CodeUtil.eachClass(context) {
            val pkgName = CodeUtil.getPackageName(it)
            when (pkgName) {
                "com.tencent.mm.plugin.fts.ui" -> {
                    val clazz = XposedHelpers2.findClassIfExists(it, classLoader)
                    if (android.widget.BaseAdapter::class.java.isAssignableFrom(clazz)) {
                        XposedHelpers2.findMethodsByExactPredicate(clazz) { m ->
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
                            if (ret && Modifier.isPublic(m.modifiers) && !Modifier.isAbstract(m.modifiers)) {
                                result.add(PointBean(KEY_SEARCH_ADAPTER, clazz.name, m.name))
                            }
                            return@findMethodsByExactPredicate ret
                        }

                    }
                }
            }
        }
        return result
    }


}
