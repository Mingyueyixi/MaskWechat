package com.lu.wxmask.util;

import android.content.Context;

import com.lu.magic.util.ReflectUtil;
import com.lu.magic.util.function.Consumer;
import com.lu.magic.util.function.Predicate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import dalvik.system.DexFile;

public class CodeUtil {
    /**
     * 获取应用程序下的所有Dex文件
     *
     * @param packageCodePath 包路径
     * @return Set<DexFile>
     */
    private static List<DexFile> getDexFileList(String packageCodePath) {
        List<DexFile> dexFiles = new ArrayList<>();
        File dir = new File(packageCodePath).getParentFile();
        File[] files = dir.listFiles();
        for (File file : files) {
            try {
                String absolutePath = file.getAbsolutePath();
                if (!absolutePath.contains(".")) continue;
                String suffix = absolutePath.substring(absolutePath.lastIndexOf("."));
                if (!suffix.equals(".apk")) continue;
                DexFile dexFile = getDexFile(file.getAbsolutePath());
                if (dexFile == null) continue;
                dexFiles.add(dexFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dexFiles;
    }

    /**
     * 获取DexFile文件
     *
     * @param path 路径
     * @return DexFile
     */
    public static DexFile getDexFile(String path) {
        try {
            return new DexFile(path);
        } catch (IOException e) {
            return null;
        }
    }


    public static List<String> filterClass(Context context, Predicate<String> func) {
        List<DexFile> dexFiles = getDexFileListNewApi(context.getClassLoader());
        if (dexFiles.isEmpty()) {
            //老方法需要重新读取，耗时大
            dexFiles = getDexFileList(context.getApplicationContext().getPackageCodePath());
        }
        return filterClassInternal(dexFiles.iterator(), func);
    }

    public static void eachClass(Context context, Consumer<String> consumer) {
        List<DexFile> dexFiles = getDexFileListNewApi(context.getClassLoader());
        if (dexFiles.isEmpty()) {
            //老方法需要重新读取，耗时大
            dexFiles = getDexFileList(context.getApplicationContext().getPackageCodePath());
        }
        eachClassInternal(dexFiles.iterator(), consumer);
    }

    /**
     * 读取类路径下的所有类
     *
     * @param context 上下文
     * @param pkgName 包名
     * @return List<String>
     */
    public static List<String> filterClass(Context context, String pkgName, boolean includeChildDir) {
        List<DexFile> dexFiles = getDexFileListNewApi(context.getClassLoader());
        if (dexFiles.isEmpty()) {
            //老方法需要重新读取，耗时大
            dexFiles = getDexFileList(context.getApplicationContext().getPackageCodePath());
        }

        return filterClassInternal(dexFiles.iterator(), currentClassPath -> {
            if (!currentClassPath.startsWith(pkgName)) {
                return false;
            }
            if (includeChildDir) {
                return true;
            } else {
                return '.' == currentClassPath.charAt(pkgName.length());
            }
        });
    }


    public static List<DexFile> getDexFileListNewApi(ClassLoader classLoader) {
        List<DexFile> result = new ArrayList<>();
        try {
            Object pathList = ReflectUtil.getFieldValue(classLoader, classLoader.getClass().getSuperclass(), "pathList");
            Object[] dexElements = (Object[]) ReflectUtil.getFieldValue(pathList, "dexElements");
            for (Object dexElement : dexElements) {
                DexFile dexFile = (DexFile) ReflectUtil.getFieldValue(dexElement, "dexFile");
                result.add(dexFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private static void eachClassInternal(Iterator<DexFile> dexFiles, Consumer<String> func) {

        while (dexFiles.hasNext()) {
            DexFile dexFile = dexFiles.next();
            if (dexFile == null) continue;

            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                try {
                    String currentClassPath = entries.nextElement();
                    if (currentClassPath == null || currentClassPath.isEmpty()) {
                        continue;
                    }
                    func.accept(currentClassPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static List<String> filterClassInternal(Iterator<DexFile> dexFiles, Predicate<String> predicateFunc) {
        HashSet<String> result = new HashSet<>();
        eachClassInternal(dexFiles, s -> {
            if (predicateFunc.test(s)) {
                result.add(s);
            }
        });
        return new ArrayList<>(result);
    }


    public static String getPackageName(String typeName) {
        int index = typeName.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return typeName.substring(0, index);
    }
}
