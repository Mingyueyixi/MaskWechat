package com.lu.wxmask.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.FrameLayout
import com.lu.magic.ui.BaseActivity
import com.lu.wxmask.ui.wrapper.WebViewProvider

class WebViewActivity : BaseActivity() {
    val webViewProvider by lazy { WebViewProvider(this) }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val contentLayout = FrameLayout(this)
        webViewProvider.attachView(contentLayout)
        setContentView(contentLayout)

        val webUrl = intent.getStringExtra("url")
        if (webUrl.isNullOrBlank()) {
            finish()
            return
        }
        webViewProvider.loadUrl(webUrl)
    }
}