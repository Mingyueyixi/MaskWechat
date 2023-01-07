package com.lu.wxmask.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.magic.frame.baseutils.kxt.GsonKotlinXKt;
import com.lu.magic.frame.baseutils.kxt.JsonObjectKotlin;
import com.lu.magic.util.log.LogUtil;
import com.lu.wxmask.Constrant;
import com.lu.wxmask.ui.WXConfigUI;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WXConfigPlugin implements IPlugin {

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                "com.tencent.mm.ui.LauncherUI",
                context.getClassLoader(),
                "onCreate",
                Bundle.class.getName(),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Activity activity = (Activity) param.thisObject;
                        Intent intent = activity.getIntent();
                        showAttachConfigView(activity, intent);
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                "com.tencent.mm.ui.LauncherUI",
                context.getClassLoader(),
                "onNewIntent",
                Intent.class.getName(),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Intent intent = (Intent) param.args[0];
                        showAttachConfigView((Activity) param.thisObject, intent);
                    }
                }
        );

    }

    private void showAttachConfigView(Activity activity, Intent intent) {
        boolean fromMaskWechat = intent.getBooleanExtra(Constrant.KEY_INTENT_MASK_WECHAT, false);
        if (!fromMaskWechat) {
            return;
        }
        try {
            new WXConfigUI(activity).show();
            LogUtil.w("show WebChatConfigUI");
        } catch (Exception e) {
            LogUtil.e(e);
        }

    }
}
