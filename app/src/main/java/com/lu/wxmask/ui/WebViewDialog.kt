package com.lu.wxmask.ui

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialog
import com.lu.magic.util.AppUtil
import com.lu.wxmask.ui.wrapper.WebViewProvider

class WebViewDialog(context: Context, var webUrl: String, var dialogTitle: String) : AppCompatDialog(context) {
    val webViewProvider = WebViewProvider(context).also {
        it.loadUrl(webUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(dialogTitle)
        val contentView = FrameLayout(context).also {
            it.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT)
        }
        webViewProvider.attachView(contentView)
        setContentView(contentView)

        val height = AppUtil.getContext().resources.displayMetrics.heightPixels * 0.6f
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, height.toInt())
    }


}