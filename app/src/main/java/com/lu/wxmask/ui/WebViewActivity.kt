package com.lu.wxmask.ui

import android.os.Bundle
import android.widget.FrameLayout
import com.lu.magic.ui.BaseActivity
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.ui.wrapper.WebViewProvider

class WebViewActivity : BaseActivity() {
    val webViewProvider by lazy { WebViewProvider(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentLayout = FrameLayout(this)
        webViewProvider.attachView(contentLayout)
        setContentView(contentLayout)

        val webUrl = intent.getStringExtra("url")
        intent.getStringExtra("title")?.let {
            title = it
        }
        if (webUrl.isNullOrBlank()) {
            finish()
            return
        }
        LogUtil.i("onCreate")
        webViewProvider.loadUrl(webUrl) { view, url ->
            view?.title?.let {
                title = it
            }
        }
    }
}