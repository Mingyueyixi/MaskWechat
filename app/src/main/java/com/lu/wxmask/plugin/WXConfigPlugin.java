package com.lu.wxmask.plugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.core.os.HandlerCompat;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.GsonUtil;
import com.lu.magic.util.ToastUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.magic.util.thread.AppExecutor;
import com.lu.wxmask.ClazzN;
import com.lu.wxmask.Constrant;
import com.lu.wxmask.bean.MaskItemBean;
import com.lu.wxmask.plugin.ui.AddMaskItemUI;
import com.lu.wxmask.plugin.ui.ConfigManagerUI;
import com.lu.wxmask.plugin.ui.EditMaskItemUI;
import com.lu.wxmask.plugin.ui.MaskUtil;
import com.lu.wxmask.util.AppVersionUtil;
import com.lu.wxmask.util.ConfigUtil;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WXConfigPlugin implements IPlugin {
    public boolean isOnDoingConfig = false;
    private boolean isShowingAddConfigTipUI;

    private int pluginMode;

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        handleHookLauncherUI(context, lpparam);
    }

    private void handleHookLauncherUI(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers2.findAndHookMethod(
                ClazzN.LauncherUI,
                context.getClassLoader(),
                "onCreate",
                Bundle.class.getName(),
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Activity activity = (Activity) param.thisObject;
                        Intent intent = activity.getIntent();
                        onEntryWechatUI(activity, intent);
                    }
                }
        );

        XposedHelpers2.findAndHookMethod(
                ClazzN.LauncherUI,
                context.getClassLoader(),
                "onNewIntent",
                Intent.class.getName(),
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Intent intent = (Intent) param.args[0];
                        onEntryWechatUI((Activity) param.thisObject, intent);
                    }
                }
        );
    }

    //进到微信
    private void onEntryWechatUI(Activity activity, Intent intent) {
        boolean isFromMaskPlugin = intent.getBooleanExtra(Constrant.KEY_INTENT_FROM_MASK, false);
        pluginMode = intent.getIntExtra(Constrant.KEY_INTENT_PLUGIN_MODE, -1);
        if (!isFromMaskPlugin) {
            LogUtil.d("ignore not from mask");
            return;
        }
        if (!AppVersionUtil.isSupportWechat()) {
            new AlertDialog.Builder(activity)
                    .setIcon(activity.getApplicationInfo().icon)
                    .setTitle("提示")
                    .setMessage("当前WeiXin版本：" + AppVersionUtil.getSmartVersionName() + "不支持，继续使用极可能无效，请到MaskWechat主页查看支持的版本")
                    .setNegativeButton("继续使用", (dialog, which) -> {
                        onEnterConfigUI(activity, intent);
                    })
                    .setPositiveButton("糊脸主页", (dialog, which) -> {
                        try {
                            String uri = "maskwechat://com.lu.wxmask/page/webView?&url=https://github.com/Mingyueyixi/MaskWechat";
                            Intent openIntent = Intent.parseUri(uri, Intent.URI_ALLOW_UNSAFE);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(openIntent);
                        } catch (Throwable e) {
                            ToastUtil.show("打开糊脸主页失败");
                        }
                    })
                    .show();
            return;
        }
        onEnterConfigUI(activity, intent);
    }

    private void onEnterConfigUI(Activity activity, Intent intent) {
        if (pluginMode == Constrant.VALUE_INTENT_PLUGIN_MODE_ADD && !isShowingAddConfigTipUI) {
            showAddTipDialog(activity);
        } else if (pluginMode == Constrant.VALUE_INTENT_PLUGIN_MODE_MANAGER) {
            showManagerConfigUI(activity, intent);
        } else {
            LogUtil.i("entry wechat ui, but support plugin mode", pluginMode);
        }
    }

    private void showManagerConfigUI(Activity activity, Intent intent) {
        new ConfigManagerUI(activity).show();
    }

    private void showAddTipDialog(Activity activity) {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setTitle("配置提示")
                    .setIcon(activity.getApplicationInfo().icon)
                    .setMessage("点击用户发起聊天，就可以对用户进行配置噢~")
                    .setCancelable(false)
                    .setPositiveButton("继续", ((dialog, which) -> {
                        isOnDoingConfig = true;
                    }))
                    .setNeutralButton("忽略", (dialog, which) -> {
                        isOnDoingConfig = false;
                    })
                    .setOnDismissListener(dialog -> {
                        isShowingAddConfigTipUI = false;
                    })
                    .show();
            isShowingAddConfigTipUI = true;
            LogUtil.i("show WebChatTipConfigUI");
        });

    }

//    private void handleHookChattingUIAddMaskDialog(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
//        XposedHelpers2.findAndHookMethod(
//                ClazzN.BaseChattingUIFragment,
//                context.getClassLoader(),
//                "doResume",
//                new XC_MethodHook2() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) {
//                        doResumeHookAction(param);
//                    }
//
//                });
//
//    }

    public void doResumeHookAction(XC_MethodHook.MethodHookParam param) {
        if (!isOnDoingConfig) {
            LogUtil.d("ignore show config ui");
            return;
        }
        LogUtil.i("start config ui");
        View view = XposedHelpers2.callMethod(param.thisObject, "getView");
        view.post(() -> {

            //确保数据和画面准备好了
            if (XposedHelpers2.callMethod(param.thisObject, "isHidden")) {
                LogUtil.w("isHidden");
                return;
            }
            Activity activity = XposedHelpers2.callMethod(param.thisObject, "getActivity");
            String activityClazzName = activity.getClass().getName();
            if (!ClazzN.LauncherUI.equals(activityClazzName) && !ClazzN.ChattingUI.equals(activityClazzName)) {
                LogUtil.w("isNot Match Activity", activityClazzName);
                return;
            }
            Bundle arguments = XposedHelpers2.callMethod(param.thisObject, "getArguments");
            String chatUser = arguments.getString("Chat_User");
            List<MaskItemBean> lst = ConfigUtil.Companion.getMaskList();
            int idIndex = MaskUtil.findIndex(lst, chatUser);

            Object chatUserInfo = findChatUserObject(param.thisObject);
            //备注
            String field_conRemark = "";
            //chat id == chatUser
//            String field_username = "";
            //昵称
            String field_nickname = "";
            if (chatUserInfo != null) {
                field_conRemark = XposedHelpers2.getObjectField(chatUserInfo, "field_conRemark");
//                field_username = XposedHelpers2.getObjectField(chatUserInfo, "field_username");
                field_nickname = XposedHelpers2.getObjectField(chatUserInfo, "field_nickname");
                LogUtil.d("chatUserInfo", GsonUtil.toJson(chatUserInfo));
            }

            if (idIndex < 0) {
                new AddMaskItemUI(activity, lst)
                        .setChatUserId(chatUser)
                        .setTagName((field_conRemark == null || field_conRemark.isEmpty()) ? field_nickname : field_conRemark)
                        .setFreeButton("退出配置", (dialog, which) -> isOnDoingConfig = false)
                        .show();
            } else {
                new EditMaskItemUI(activity, lst, idIndex)
                        .setFreeButton("退出配置", (dialog, which) -> isOnDoingConfig = false)
                        .show();
            }
        });

    }

    private Object findChatUserObject(Object fragmentObj) {
        if (AppVersionUtil.getVersionCode() >= Constrant.WX_CODE_8_0_32) {
            try {
                //8.0.32: of3.b
                //8.0.33: oi3.b
                //8.0.34: ck3.b
                //8.0.37: zq3.b
                Object f = XposedHelpers2.getObjectField(fragmentObj, "f");
                if (f != null) {
                    //com.tencent.mm.storage.y1
                    if (AppVersionUtil.getVersionCode() <= Constrant.WX_CODE_8_0_32) {
                        Object v = XposedHelpers2.getObjectField(f, "e");
                        if (ClazzN.from(ClazzN.BaseContact).isAssignableFrom(v.getClass())) {
                            return v;
                        }
                    } else if (AppVersionUtil.getVersionCode() <= Constrant.WX_CODE_8_0_33) {
                        Object v = XposedHelpers2.getObjectField(f, "h");
                        if (ClazzN.from(ClazzN.BaseContact).isAssignableFrom(v.getClass())) {
                            return v;
                        }
                    } else if (AppVersionUtil.getVersionCode() <= Constrant.WX_CODE_8_0_35) {
                        //8.0.34开始，数据库基类改变。包：com.tencent.mm.autogen.table 已经不存在，不再校验
                        Object v = XposedHelpers2.getObjectField(f, "h");
                        return v;
                    } else if (AppVersionUtil.getVersionCode() <= Constrant.WX_CODE_8_0_40) {
                        Object v = XposedHelpers2.getObjectField(f, "i");
                        return v;
                    } else {
                        //8.0.33与8.0.34共同的父类： com.tencent.mm.contact.d
                        Field[] hitFields = XposedHelpers2.findFieldsByExactPredicate(f.getClass(), field -> ClazzN.from("com.tencent.mm.contact.d").isAssignableFrom(field.getType()));
                        if (hitFields.length > 0) {
                            Field field = hitFields[0];
                            Object result = field.get(f);
                            LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess user info object, ", "find field: ", field.getName(), "=", result);
                            return result;
                        } else {
                            LogUtil.w(AppVersionUtil.getSmartVersionName(), "guess user info object fail!");
                        }
                    }
                }
            } catch (Throwable e) {
                LogUtil.w("找不到当前聊天的用户信息", e);
            }
        } else if (AppVersionUtil.getVersionCode() == Constrant.WX_CODE_8_0_22) {
            try {
                //com.tencent.mm.ui.chatting.d.a
                Object hED = XposedHelpers2.getObjectField(fragmentObj, "hED");
                if (hED != null) {
                    //com.tencent.mm.storage.aw
                    Object v = XposedHelpers2.getObjectField(hED, "ZfP");
                }
            } catch (Throwable e) {
                LogUtil.w("找不到当前聊天的用户信息", e);
            }
        } else {
            LogUtil.w("未适配的版本", AppVersionUtil.getSmartVersionName());
        }
        return null;
    }
//    private void handleHookGetChatInfo(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
//        Method setChattingInfoMethod = null;
//        if (AppVersionUtil.getVersionCode() == Constrant.WX_CODE_8_0_22) {
//            try {
//                setChattingInfoMethod = XposedHelpers2.findMethodExact(ClazzN.BaseChattingUIFragment, context.getClassLoader(), "aB", "com.tencent.mm.storage.aw");
//            } catch (Throwable e) {
//                LogUtil.w("can't find aB(com.tencent.mm.storage.aw) function, try to exact BaseChattingUIFragment's methods");
//            }
//        }
//        final Class<?> baseContactClazz = XposedHelpers2.findClassIfExists(ClazzN.BaseContact, context.getClassLoader());
//
//        if (setChattingInfoMethod == null) {
//            //直接遍历搜查
//            Method[] methods = XposedHelpers2.findMethodsByExactPredicate(
//                    ClazzN.BaseChattingUIFragment,
//                    context.getClassLoader(),
//                    method -> {
////                        LogUtil.w(method);
//                        for (Class<?> parameterType : method.getParameterTypes()) {
//                            if (baseContactClazz.isAssignableFrom(parameterType)) {
//                                return true;
//                            }
//                        }
//                        return false;
//                    }
//            );
//            if (methods.length == 1) {
//                setChattingInfoMethod = methods[0];
//            } else {
//                //找到多个，暂时取参数最多的一个
//                int maxIndex = -1;
//                int maxSize = -1;
//                for (int i = 0; i < methods.length; i++) {
//                    int pSize = methods[i].getTypeParameters().length;
//                    if (pSize >= maxSize) {
//                        maxIndex = i;
//                        maxSize = pSize;
//                    }
//                }
//                if (maxIndex != -1) {
//                    setChattingInfoMethod = methods[maxIndex];
//                }
//            }
//        }
//        if (setChattingInfoMethod == null) {
//            //理论也可以从hED变量搜索
//            LogUtil.i("遍历搜查不出");
//            return;
//        }
//        LogUtil.i("guess setChattingInfo method is: ", setChattingInfoMethod);
//        XposedHelpers2.hookMethod(setChattingInfoMethod, new XC_MethodHook2() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Object chatUserInfo = null;
//                for (Object arg : param.args) {
//                    if (baseContactClazz.isAssignableFrom(arg.getClass())) {
//                        chatUserInfo = arg;
//                        break;
//                    }
//                }
//                if (chatUserInfo == null) {
//                    LogUtil.w("chatUserInfo from method is null, ignore");
//                    return;
//                }
//
//                field_conRemark = XposedHelpers2.getObjectField(chatUserInfo, "field_conRemark");
//                field_username = XposedHelpers2.getObjectField(chatUserInfo, "field_username");
//                field_nickname = XposedHelpers2.getObjectField(chatUserInfo, "field_nickname");
//
////                Bundle bundle = XposedHelpers2.callMethod(param.thisObject, "getArguments");
////                bundle.putString("field_conRemark", field_conRemark);
////                bundle.putString("field_username", field_username);
////                bundle.putString("field_nickname", field_nickname);
////
//                LogUtil.d(field_nickname, field_username, field_conRemark);
//            }
//        });
//    }

}
