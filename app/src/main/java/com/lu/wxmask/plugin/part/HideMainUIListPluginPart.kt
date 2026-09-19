package com.lu.wxmask.plugin.part

import android.content.Context
import com.lu.lposed.api2.XC_MethodHook2
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.MainHook
import com.lu.wxmask.plugin.WXMaskPlugin
import com.lu.wxmask.util.AppVersionUtil
import com.lu.wxmask.util.ConfigUtil
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method

/**
 * 主页UI（微信Tab消息列表）处理插件
 * 
 * 8.0.76 终极精准拦截点：
 * 微信在主页绘制最后一条消息摘要时，调用了核心格式化方法：
 * nh5.b.l(Context, k4, x, int) -> 返回 CharSequence
 * 
 * 我们只要在此处判断：如果 k4.field_username 属于配置的私密用户：
 * 1. 直接返回 ""（空字符串），主页摘要瞬间消失！
 * 2. 同时将 k4 的 unReadCount、UnReadInvite 等清零。
 * 
 * 优势：
 * - 绝对不碰任何 View 树（零 View 错位、零红点拉伸变形）
 * - 绝对不涉及 View 复用污染（每个条目的文字都是实时调此方法生成的）
 * - 完美适配 8.0.76！
 */
class HideMainUIListPluginPart : IPlugin {

    override fun handleHook(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        hookDigestFormatMethod(context)
        hookConversationStorage(context)
    }

    private fun hookDigestFormatMethod(context: Context) {
        val nh5ClazzName = "nh5.b"
        val nh5Clazz = XposedHelpers2.findClassIfExists(nh5ClazzName, context.classLoader)
        if (nh5Clazz != null) {
            val formatMethods = XposedHelpers2.findMethodsByExactPredicate(nh5Clazz) { m ->
                m.name == "l" && m.parameterTypes.size == 4 && CharSequence::class.java.isAssignableFrom(m.returnType)
            }
            if (formatMethods.isNotEmpty()) {
                val method = formatMethods[0]
                XposedHelpers2.hookMethod(
                    method,
                    object : XC_MethodHook2() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val k4Obj = param.args[1] ?: return
                            val username = try {
                                XposedHelpers2.getObjectField<String?>(k4Obj, "field_username")
                            } catch (e: Throwable) {
                                null
                            }
                            if (!username.isNullOrEmpty() && WXMaskPlugin.containChatUser(username)) {
                                // 强制返回空，清空主页最后一条消息
                                param.result = ""
                            }
                        }
                    }
                )
                LogUtil.i("Successfully hooked nh5.b.l for WeChat 8.0.76 digest format")
            }
        }
    }

    private fun hookConversationStorage(context: Context) {
        // hook getItem / f(int) 清零未读红点与会话内容
        val adapterClazzName = when (AppVersionUtil.getVersionCode()) {
            Constrant.WX_CODE_8_0_76 -> "fh5.w0"
            else -> "fh5.w0"
        }
        val adapterClass = XposedHelpers2.findClassIfExists(adapterClazzName, context.classLoader) ?: return
        val getItemMethod = XposedHelpers2.findMethodExactIfExists(adapterClass, "f", Integer.TYPE)
            ?: XposedHelpers2.findMethodExactIfExists(adapterClass, "getItem", Integer.TYPE)

        if (getItemMethod != null) {
            XposedHelpers2.hookMethod(
                getItemMethod,
                object : XC_MethodHook2() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val itemData: Any = param.result ?: return
                        val chatUser: String? = try {
                            XposedHelpers2.getObjectField(itemData, "field_username")
                        } catch (e: Throwable) {
                            null
                        }
                        if (chatUser.isNullOrEmpty()) return

                        if (WXMaskPlugin.containChatUser(chatUser)) {
                            try {
                                XposedHelpers2.setObjectField(itemData, "field_content", "")
                                XposedHelpers2.setObjectField(itemData, "field_digest", "")
                                XposedHelpers2.setObjectField(itemData, "field_unReadCount", 0)
                                XposedHelpers2.setObjectField(itemData, "field_UnReadInvite", 0)
                                XposedHelpers2.setObjectField(itemData, "field_unReadMuteCount", 0)
                                XposedHelpers2.setObjectField(itemData, "field_msgType", "1")
                            } catch (e: Throwable) {
                            }
                        }
                    }
                }
            )
            LogUtil.i("Successfully hooked getItem method: $getItemMethod")
        }
    }
}