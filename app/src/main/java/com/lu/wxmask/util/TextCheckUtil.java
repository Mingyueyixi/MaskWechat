package com.lu.wxmask.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.util.Predicate;

import com.lu.magic.util.GsonUtil;
import com.lu.magic.util.ReflectUtil;
import com.lu.magic.util.TextUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.magic.util.view.ChildDeepCheck;
import com.lu.magic.util.view.SelfDeepCheck;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import kotlin.Result;

public class TextCheckUtil {


    public static int haveMatchText(View rootView, String... regexList) {
        int[] retIndex = new int[]{-1};

        new SelfDeepCheck().eachCheck(rootView, view -> {
            if (view instanceof TextView) {
                String input = ((TextView) view).getText() + "";
                int index = haveMatchText(input, regexList);
                if (index > -1) {
                    retIndex[0] = index;
                    return true;
                }
            } else {
                try {
                    Method m = ReflectUtil.getMatchingMethod(view.getClass(), "getText");
                    if (m != null) {
                        m.setAccessible(true);
                        Object v = m.invoke(view);
                        if (v != null) {
                            int index = haveMatchText(v + "", regexList);
                            if (index > -1) {
                                retIndex[0] = index;
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
            return false;
        });
        return retIndex[0];
    }

    private static int haveMatchText(CharSequence input, String... regexList) {
        for (int i = 0; i < regexList.length; i++) {
            String regex = regexList[i];
            if (TextUtil.matches(regex, input)) {
                return i;
            }
        }
        return -1;
    }

    public static List<String> getTextList(View rootView) {
        List<String> stringList = new ArrayList<>();
        new SelfDeepCheck().each(rootView, view -> {
            if (view instanceof TextView) {
                stringList.add(((TextView) view).getText() + "");
            } else {
                try {
                    Method m = ReflectUtil.getMatchingMethod(view.getClass(), "getText");
                    if (m != null) {
                        m.setAccessible(true);
                        Object v = m.invoke(view);
                        stringList.add(v + "");
                        LogUtil.d("reflect ", view);
                    }
                } catch (Exception e) {

                }
            }
        });
        return stringList;
    }

}
