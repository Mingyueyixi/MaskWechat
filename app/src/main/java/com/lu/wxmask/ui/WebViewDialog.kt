package com.lu.wxmask.ui

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialog
import com.lu.magic.util.AppUtil
import com.lu.wxmask.ui.wrapper.WebViewComponent
import com.lu.wxmask.ui.wrapper.WebViewComponentCallBack


class WebViewDialog @JvmOverloads constructor(
    context: Context,
    var webUrl: String,
    var dialogTitle: String? = null,
    var forceHtml: Boolean = false
) : AppCompatDialog(context) {
    val webViewComponent = WebViewComponent(context).also {
        it.forceHtml = forceHtml
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!dialogTitle.isNullOrBlank()) {
            setTitle(dialogTitle)
        }
        webViewComponent.loadUrl(webUrl, object : WebViewComponentCallBack {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (dialogTitle.isNullOrBlank() && !title.isNullOrBlank()) {
                    setTitle(title)
                }
            }
        })

        val contentView = FrameLayout(context).also {
            it.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT)
        }
        webViewComponent.attachView(contentView)
        setContentView(contentView)

        val height = AppUtil.getContext().resources.displayMetrics.heightPixels * 0.6f
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, height.toInt())
    }


}