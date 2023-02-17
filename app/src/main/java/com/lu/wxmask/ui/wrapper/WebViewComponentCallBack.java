package com.lu.wxmask.ui.wrapper;

import android.webkit.WebView;

import org.jetbrains.annotations.Nullable;

public interface WebViewComponentCallBack {
    default void onPageFinish(WebView view, String url) {

    }

    default void onReceivedTitle(@Nullable WebView view, @Nullable String title) {
    }
}
