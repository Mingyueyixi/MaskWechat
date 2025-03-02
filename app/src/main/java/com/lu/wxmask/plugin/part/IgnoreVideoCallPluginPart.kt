package com.lu.wxmask.plugin.part

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.lu.lposed.api2.XposedHelpers2
import com.lu.lposed.plugin.IPlugin
import com.lu.magic.util.AppUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.ClazzN
import com.lu.wxmask.plugin.WXMaskPlugin
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.text.MessageFormat.Field
import java.util.regex.MatchResult

/**
 * 自动忽略视频通话
 */
class IgnoreVideoCallPluginPart : IPlugin {
    override fun handleHook(p0: Context?, p1: XC_LoadPackage.LoadPackageParam?) {
//        if (!BuildConfig.DEBUG) {
//            return
//        }
        var hookMethod: XC_MethodHook.Unhook? = null
        val VideoActivity = ClazzN.from("com.tencent.mm.plugin.voip.ui.VideoActivity")
        XposedHelpers2.findAndHookMethod(
            VideoActivity,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    hookMethod = hookFlutter(param?.thisObject as Activity)
                }
            }
        )
        XposedHelpers2.findAndHookMethod(
            VideoActivity,
            "onDestroy",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    hookMethod?.unhook()
                }
            }
        )

//            public TalkInfo(TalkState talkState, long j15, long j16, String talker, String otherParams) {

//        XposedHelpers2.hookAllConstructors(
//            ClazzN.from("com.tencent.pigeon.voipmp.TalkInfo"),
//            object : XC_MethodHook() {
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    val result = param.result
//                    val talkerUserName = param.args[3] as String? ?: return
//
//                    //XposedHelpers2.callStaticMethod<Any?>(ClazzN.from("com.tencent.pigeon.voipmp.TaskState.Companion"), "ofRaw", 4)
//                    if (WXMaskPlugin.containChatUser(talkerUserName)) {
//                        LogUtil.d("hookFlutter:", talkerUserName)
//                        val TaskStateCompanion = XposedHelpers2.getStaticObjectField(ClazzN.from("com.tencent.pigeon.voipmp.TalkState"), "Companion")
//                        val endTaskState = XposedHelpers2.callMethod<Any?>(TaskStateCompanion, "ofRaw", 4)
//                        param.args[0] = endTaskState
//                        LogUtil.d("hookFlutter-end:", talkerUserName)
//
//                    }
//                }
//            }
//        )
    }

    private fun hookFlutter(activity: Activity): XC_MethodHook.Unhook? {
        val activityRef: WeakReference<Activity> = WeakReference(activity)
        return XposedHelpers2.findAndHookMethod(
            ClazzN.from("io.flutter.plugin.common.BasicMessageChannel\$IncomingMessageHandler"),
            "onMessage",
            ClazzN.from("java.nio.ByteBuffer"),
            ClazzN.from("io.flutter.plugin.common.BinaryMessenger\$BinaryReply"),
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    //外部类实例，即
                    val byteBuffer: ByteBuffer = param.args[0] as ByteBuffer? ?: return
                    val outObj = XposedHelpers2.getObjectField<Any?>(param.thisObject, "this$0") ?: return
                    LogUtil.d("BasicMessageChannel.onMessage:", outObj)
                    val codec = XposedHelpers2.getObjectField<Any?>(outObj, "codec") ?: return
                    //恢复到0位置，重新读取
                    byteBuffer.position(0)
                    val receiveData = XposedHelpers2.callMethod<Any?>(codec, "decodeMessage", byteBuffer)
                    LogUtil.d("BasicMessageChannel.onMessage:", codec.javaClass, receiveData.javaClass, receiveData)
                    val rText = receiveData.toString()
                    //[GetFindersRequest(username=wxid_pez18fzcygxu22, timeLimit=259200000)]
                    //[GetSnsRequest(username=wxid_pez18fzcygxu22, timeLimit=259200000)]
                    //[GetTextStatusRequest(username=wxid_pez18fzcygxu22, timeLimit=259200000)]
                    val m = Regex("username=(.*?),").find(rText) ?: return
//                    val codecClazz = ClazzN.from("com.tencent.pigeon.mm_foundation.FlutterAccountHostCodec")
                    val wxId = m.groups[1]?.value ?: return
                    if (wxId is String) {
                        if (WXMaskPlugin.containChatUser(wxId)) {
                            val act = activityRef.get()
                            if (act == null) {
                                LogUtil.d("BasicMessageChannel.onMessage:", "activity is null")
                            } else {
//                                act.findViewById<ViewGroup>(android.R.id.content).removeAllViews()
                                var logData:Any? = act.intent.getParcelableExtra("page_info")

                                act.finish()
                            }
                        }

                    }
                }
            }
        )
    }
}