package com.lu.wxmask.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.lu.magic.util.AppUtil;
import com.lu.wxmask.ui.wrapper.WebViewComponent;
import com.lu.wxmask.ui.wrapper.WebViewComponentCallBack;

import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/**
 * @author Lu
 * @date 2024/9/8 15:54
 * @description
 */
public final class WebViewDialog extends AppCompatDialog {
    private String dialogTitle;
    private boolean forceHtml;
    private String webUrl;
    private final WebViewComponent webViewComponent;

    public WebViewDialog(Context context, String webUrl, String dialogTitle, boolean forceHtml) {
        super(context);
        this.webUrl = webUrl;
        this.dialogTitle = dialogTitle;
        this.forceHtml = forceHtml;
        WebViewComponent it = new WebViewComponent(context);
        it.setForceHtml(this.forceHtml);
        this.webViewComponent = it;
    }

    public final String getWebUrl() {
        return this.webUrl;
    }

    public final void setWebUrl(String str) {
        this.webUrl = str;
    }

    public final String getDialogTitle() {
        return this.dialogTitle;
    }

    public final void setDialogTitle(String str) {
        this.dialogTitle = str;
    }

    public final boolean getForceHtml() {
        return this.forceHtml;
    }

    public final void setForceHtml(boolean z) {
        this.forceHtml = z;
    }

    public final WebViewComponent getWebViewComponent() {
        return this.webViewComponent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatDialog, androidx.activity.ComponentDialog, android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String str = this.dialogTitle;
        if (!(str == null || StringsKt.isBlank(str))) {
            setTitle(this.dialogTitle);
        }
        this.webViewComponent.loadUrl(this.webUrl, new WebViewComponentCallBack() {
            @Override // com.p004lu.wxmask.p009ui.wrapper.WebViewComponentCallBack
            public void onReceivedTitle(WebView view, String title) {
                String dialogTitle = WebViewDialog.this.getDialogTitle();
                boolean z = false;
                if (dialogTitle == null || StringsKt.isBlank(dialogTitle)) {
                    String str2 = title;
                    if (str2 == null || StringsKt.isBlank(str2)) {
                        z = true;
                    }
                    if (!z) {
                        WebViewDialog.this.setTitle(title);
                    }
                }
            }
        });
        FrameLayout it = new FrameLayout(getContext());
        it.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT));
        this.webViewComponent.attachView(it);
        setContentView(it);
        float height = AppUtil.getContext().getResources().getDisplayMetrics().heightPixels * 0.6f;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.MarginLayoutParams.MATCH_PARENT, (int) height);
        }
    }
}