package com.lu.wxmask;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.PluginRegistry;
import com.lu.magic.util.AppUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.magic.util.log.SimpleLogger;
import com.lu.wxmask.plugin.WXConfigPlugin;
import com.lu.wxmask.plugin.WXMaskPlugin;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class MainHook implements IXposedHookLoadPackage {

    private boolean hasInit = false;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            SelfHook.getInstance().handleLoadPackage(lpparam);
            return;
        }
        if (!"com.tencent.mm".equals(lpparam.processName)) {
            return;
        }
        LogUtil.setLogger(new SimpleLogger() {
            @Override
            public void onLog(int level, @NonNull Object[] objects) {
                if (BuildConfig.DEBUG) {
                    super.onLog(level, objects);
                } else {
                    //release 打印i以上级别的log，其他的忽略
                    if (level > 1) {
                        String msgText = buildLogText(objects);
                        XposedHelpers2.log(TAG + " " + msgText);
                    }
                }
            }
        });
        LogUtil.w("start mask plugin for wechat");
        XposedHelpers2.Config.setThrowableCallBack(throwable -> LogUtil.e("MaskPlugin error", throwable));

        XposedHelpers2.findAndHookMethod(
                Application.class.getName(),
                lpparam.classLoader,
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        initPlugin((Context) param.thisObject, lpparam);
                    }
                }
        );
//        XposedHelpers.findAndHookMethod(ClazzName.LauncherUI,
//                lpparam.classLoader,
//                "onCreate",
//                Bundle.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        initHook((Context) param.thisObject, lpparam);
//                    }
//                });
    }

    private void initPlugin(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (hasInit) {
            return;
        }
        LogUtil.w("start init Plugin");
        hasInit = true;
        AppUtil.attachContext(context);
        //目前生成的plugin都是单例的
        PluginRegistry.register(
                WXConfigPlugin.class,
                WXMaskPlugin.class
        ).handleHooks(context, lpparam);
        LogUtil.w("init plugin finish");
    }

}