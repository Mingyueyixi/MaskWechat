package com.lu.wxmask.plugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.magic.util.log.LogUtil;
import com.lu.wxmask.Constrant;
import com.lu.wxmask.bean.MaskItemBean;
import com.lu.wxmask.plugin.ui.AddMaskItemUI;
import com.lu.wxmask.plugin.ui.ConfigManagerUI;
import com.lu.wxmask.plugin.ui.EditMaskItemUI;
import com.lu.wxmask.plugin.ui.MaskUtil;
import com.lu.wxmask.util.ConfigUtil;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WXConfigPlugin implements IPlugin {
    public boolean isOnDoingConfig = false;
    private boolean isShowingAddConfigTipUI;
    //备注
    private String field_conRemark = "";
    //chat id
    private String field_username = "";
    //昵称
    private String field_nickname = "";
    private int pluginMode;

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        handleHookLauncherUI(context, lpparam);
        handleHookGetChatInfo(context, lpparam);
        handleHookChattingUIAddMaskDialog(context, lpparam);
    }

    private void handleHookLauncherUI(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers2.findAndHookMethod(
                "com.tencent.mm.ui.LauncherUI",
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
                "com.tencent.mm.ui.LauncherUI",
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
            LogUtil.w("ignore not from mask");
            return;
        }
        if (pluginMode == Constrant.VALUE_INTENT_PLUGIN_MODE_ADD && !isShowingAddConfigTipUI) {
            showAddTipDialog(activity);
        } else if (pluginMode == Constrant.VALUE_INTENT_PLUGIN_MODE_MANAGER) {
            showManagerConfigUI(activity, intent);
        } else {
            LogUtil.w("entry wechat ui, but support plugin mode", pluginMode);
        }
    }

    private void showManagerConfigUI(Activity activity, Intent intent) {
        new ConfigManagerUI(activity).initUI().show();
    }

    private void showAddTipDialog(Activity activity) {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setMessage("去点击用户进行配置吧~")
                    .setNegativeButton("知道了", ((dialog, which) -> {
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
            LogUtil.w("show WebChatTipConfigUI");
        });

    }

    private void handleHookChattingUIAddMaskDialog(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers2.findAndHookMethod(
                "com.tencent.mm.ui.chatting.BaseChattingUIFragment",
                context.getClassLoader(),
                "doResume",
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!isOnDoingConfig) {
                            LogUtil.d("ignore show config ui");
                            return;
                        }
                        LogUtil.w("start config ui");
                        View view = XposedHelpers2.callMethod(param.thisObject, "getView");
                        view.post(() -> {

                            //确保数据和画面准备好了
                            if (XposedHelpers2.callMethod(param.thisObject, "isHidden")) {
                                LogUtil.w("isHidden");
                                return;
                            }
                            Activity activity = XposedHelpers2.callMethod(param.thisObject, "getActivity");
                            if (!"com.tencent.mm.ui.LauncherUI".equals(activity.getClass().getName())) {
                                LogUtil.w("isNot Activity");
                                return;
                            }
                            Bundle arguments = XposedHelpers2.callMethod(param.thisObject, "getArguments");
                            String chatUser = arguments.getString("Chat_User");
                            List<MaskItemBean> lst = ConfigUtil.Companion.getMaskList();
                            int idIndex = MaskUtil.findIndex(lst, chatUser);
                            if (idIndex < 0) {
                                new AddMaskItemUI(activity, lst)
                                        .setChatUserId(chatUser)
                                        .setTagName(field_conRemark.isEmpty() ? field_nickname : field_conRemark)
                                        .setFreeButton("退出配置", (dialog, which) -> isOnDoingConfig = false)
                                        .show();
                            } else {
                                new EditMaskItemUI(activity, lst, idIndex)
                                        .setFreeButton("退出配置", (dialog, which) -> isOnDoingConfig = false)
                                        .show();
                            }
                        });

                    }

                });

    }


    private void handleHookGetChatInfo(Context context, XC_LoadPackage.LoadPackageParam lpparam) {

        String fragmentClassName = "com.tencent.mm.ui.chatting.BaseChattingUIFragment";
        Method setChattingInfoMethod = null;
        try {
            setChattingInfoMethod = XposedHelpers2.findMethodExact(fragmentClassName, context.getClassLoader(), "aB", "com.tencent.mm.storage.aw");
        } catch (Throwable e) {
            LogUtil.w("can' fin aB(com.tencent.mm.storage.aw) function, try to exact BaseChattingUIFragment's methods");
        }
        final Class<?> baseContactClazz = XposedHelpers2.findClassIfExists("com.tencent.mm.autogen.table.BaseContact", context.getClassLoader());

        if (setChattingInfoMethod == null) {
            //直接遍历搜查
            Method[] methods = XposedHelpers2.findMethodsByExactPredicate(
                    "com.tencent.mm.ui.chatting.BaseChattingUIFragment",
                    context.getClassLoader(),
                    method -> {
                        for (Class<?> parameterType : method.getParameterTypes()) {
                            if (baseContactClazz.isAssignableFrom(parameterType)) {
                                return true;
                            }
                        }
                        return false;
                    }
            );
            if (methods.length == 1) {
                setChattingInfoMethod = methods[0];
            } else {
                //找到多个，暂时取参数最多的一个
                int maxIndex = -1;
                int maxSize = -1;
                for (int i = 0; i < methods.length; i++) {
                    int pSize = methods[i].getTypeParameters().length;
                    if (pSize >= maxSize) {
                        maxIndex = i;
                        maxSize = pSize;
                    }
                }
                if (maxIndex != -1) {
                    setChattingInfoMethod = methods[maxIndex];
                }
            }
        }
        if (setChattingInfoMethod == null) {
            //理论也可以从hED变量搜索
        }

        XposedHelpers2.hookMethod(setChattingInfoMethod, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object chatUserInfo = null;
                for (Object arg : param.args) {
                    if (baseContactClazz.isAssignableFrom(arg.getClass())) {
                        chatUserInfo = arg;
                        break;
                    }
                }
                if (chatUserInfo == null) {
                    LogUtil.w("chatUserInfo from method is null, ignore");
                    return;
                }

                field_conRemark = XposedHelpers2.getObjectField(chatUserInfo, "field_conRemark");
                field_username = XposedHelpers2.getObjectField(chatUserInfo, "field_username");
                field_nickname = XposedHelpers2.getObjectField(chatUserInfo, "field_nickname");

//                Bundle bundle = XposedHelpers2.callMethod(param.thisObject, "getArguments");
//                bundle.putString("field_conRemark", field_conRemark);
//                bundle.putString("field_username", field_username);
//                bundle.putString("field_nickname", field_nickname);
//
                LogUtil.d(field_nickname, field_username, field_conRemark);
            }
        });
    }

}
