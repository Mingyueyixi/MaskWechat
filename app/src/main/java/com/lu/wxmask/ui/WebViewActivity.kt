package com.lu.wxmask.ui

import android.os.Bundle
import android.webkit.WebView
import android.widget.FrameLayout
import com.lu.magic.ui.BaseActivity
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ui.wrapper.WebViewComponentCallBack
import com.lu.wxmask.ui.wrapper.WebViewComponent

class WebViewActivity : BaseActivity() {
    val webViewComponent by lazy { WebViewComponent(this) }
    var hasLoadUrl = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentLayout = FrameLayout(this)
        setContentView(contentLayout)

        val webUrl = intent.getStringExtra("url")
        val forceHtml = intent.getBooleanExtra("forceHtml", false)
        val preTitleText = intent.getStringExtra("title")
        if (!preTitleText.isNullOrBlank()) {
            title = preTitleText
        }

        if (webUrl.isNullOrBlank()) {
            finish()
            return
        }
        LogUtil.i("onCreate")
        webViewComponent.forceHtml = forceHtml
        webViewComponent.attachView(contentLayout)
        webViewComponent.loadUrl(webUrl, object : WebViewComponentCallBack {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (preTitleText.isNullOrBlank() && !title.isNullOrBlank()) {
                    setTitle(title)
                }
            }
        })
        hasLoadUrl = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasLoadUrl) {
            webViewComponent.destroy()
        }
    }
}