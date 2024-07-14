package com.lu.wxmask.util;

import java.util.List;

/**
 * @author Lu
 * @date 2024/6/10 23:39
 * @description
 */
public class TextKit {
    public static boolean isContain(List<String> texts, String kw) {
        if (texts == null || texts.isEmpty()) {
            return false;
        }

        for (String text : texts) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
