package com.lu.wxmask;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.nfc.Tag;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.PluginRegistry;
import com.lu.magic.util.AppUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.magic.util.log.SimpleLogger;
import com.lu.wxmask.plugin.CommonPlugin;
import com.lu.wxmask.plugin.WXConfigPlugin;
import com.lu.wxmask.plugin.WXMaskPlugin;
import com.lu.wxmask.util.Rm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static final String TARGET_PACKAGE = "com.tencent.mm";
    public static CopyOnWriteArraySet<String> uniqueMetaStore = new CopyOnWriteArraySet<>();
    private boolean hasInit = false;
    private List<XC_MethodHook.Unhook> initUnHookList = new ArrayList<>();
    private static String MODULE_PATH = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
//        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
//            SelfHook.getInstance().handleLoadPackage(lpparam);
//            return;
//        }


        HashSet<String> allowList = new HashSet<>();
        allowList.add(BuildConfig.APPLICATION_ID);
        allowList.add(TARGET_PACKAGE);

        if (!allowList.contains(lpparam.processName)) {
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
        LogUtil.i("start main plugin for wechat");
        XposedHelpers2.Config
                .setCallMethodWithProxy(true)
                .setThrowableCallBack(throwable -> LogUtil.w("MaskPlugin error", throwable))
                .setOnErrorReturnFallback((method, throwable) -> {
                    Class<?> returnType = method.getReturnType();
                    // 函数执行错误时，给定一个默认的返回值值。
                    // 没什么鸟用。xposed api就没有byte/short/int/long/这些基本类型的返回值函数
                    if (String.class.equals(returnType) || CharSequence.class.isAssignableFrom(returnType)) {
                        return "";
                    }
                    if (Integer.TYPE.equals(returnType) || Integer.class.equals(returnType)) {
                        return 0;
                    }
                    if (Long.TYPE.equals(returnType) || Long.class.equals(returnType)) {
                        return 0L;
                    }
                    if (Double.TYPE.equals(returnType) || Double.class.equals(returnType)) {
                        return 0d;
                    }
                    if (Float.TYPE.equals(returnType) || Float.class.equals(returnType)) {
                        return 0f;
                    }
                    if (Byte.TYPE.equals(returnType) || Byte.class.equals(returnType)) {
                        return new byte[]{};
                    }
                    if (Short.TYPE.equals(returnType) || Short.class.equals(returnType)) {
                        return (short) 0;
                    }
                    if (BuildConfig.DEBUG) {
                        LogUtil.w("setOnErrorReturnFallback", throwable);
                    }
                    return null;
                });

        XC_MethodHook.Unhook unhook = XposedHelpers2.findAndHookMethod(
                Application.class.getName(),
                lpparam.classLoader,
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        initPlugin((Context) param.thisObject, lpparam);
                    }
                }
        );
        initUnHookList.add(unhook);

//        initHookCallBack = XposedHelpers2.findAndHookMethod(
//                Activity.class.getName(),
//                lpparam.classLoader,
//                "onCreate",
//                Bundle.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        initPlugin(((Activity) param.thisObject).getApplicationContext(), lpparam);
//                    }
//                }
//        );
//
        //"com.tencent.mm.app.com.Application"的父类
        //"tencent.tinker.loader.app.TinkerApplication"

//        XposedHelpers2.findAndHookMethod(
//                Application.class.getName(),
//                lpparam.classLoader,
//                "attach",
//                Context.class.getName(),
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        initPlugin((Context) param.args[0], lpparam);
//                    }
//                }
//        );
//
        unhook = XposedHelpers2.findAndHookMethod(
                Instrumentation.class.getName(),
                lpparam.classLoader,
                "callApplicationOnCreate",
                Application.class.getName(),
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        initPlugin((Context) param.args[0], lpparam);
                    }
                }
        );
        initUnHookList.add(unhook);
//
//        XposedHelpers2.findAndHookMethod(
//                Activity.class.getName(),
//                lpparam.classLoader,
//                "onCreate",
//                Bundle.class.getName(),
//                new XC_MethodHook2() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        initPlugin((Context) param.thisObject, lpparam);
//                    }
//                }
//        );
//

    }

    private void initPlugin(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (context == null) {
            LogUtil.w("context is null");
            return;
        }
        if (hasInit) {
            return;
        }
        LogUtil.i("start init Plugin");
        hasInit = true;
        AppUtil.attachContext(context);

        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            initSelfPlugins(context, lpparam);
        } else {
            initTargetPlugins(context, lpparam);
        }

        for (XC_MethodHook.Unhook unhook : initUnHookList) {
            if (unhook != null) {
                unhook.unhook();
            }
        }
        LogUtil.i("init plugin finish");
    }

    private void initSelfPlugins(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        SelfHook.getInstance().handleHook(context, lpparam);
    }

    private void initTargetPlugins(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        //目前生成的plugin都是单例的
        PluginRegistry.register(
                CommonPlugin.class,
                WXConfigPlugin.class,
                WXMaskPlugin.class
        ).handleHooks(context, lpparam);

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (BuildConfig.APPLICATION_ID.equals(resparam.packageName)) {
            return;
        }
        if (TARGET_PACKAGE.equals(resparam.packageName)) {
//            XModuleResources xRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
//            Rm.mask_layout_plugin_manager = resparam.res.addResource(xRes, R.layout.mask_layout_plugin_manager);
        }
    }

}