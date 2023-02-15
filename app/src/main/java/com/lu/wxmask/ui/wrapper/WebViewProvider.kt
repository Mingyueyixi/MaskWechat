package com.lu.wxmask.ui.wrapper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.arch.core.util.Function
import androidx.core.view.contains
import com.lu.magic.util.kxt.toElseEmptyString
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.route.MaskAppRouter

class WebViewProvider(val context: Context) {
    private var onPageFinishCallBack: ((view: WebView?, url: String?) -> Unit)? = null
    val webView = WebView(context)

    init {
        webView.settings.let {
            it.domStorageEnabled = true
            it.javaScriptEnabled = true
            it.databaseEnabled = true
        }
        var webViewClient = object : WebViewClient() {
            val webUrlInterceptor = WebUrlInterceptor()
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                Log.i(">>>", "webViewLinker onLoadResource $url")
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                LogUtil.i("webViewLinker shouldOverrideUrlLoading", url)
                if (webUrlInterceptor.shouldOverrideUrlLoading(view, Uri.parse(url))) {
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                LogUtil.i("webViewLinker onPageStarted", url)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                LogUtil.i("webViewLinker onPageFinished", url)
                onPageFinishCallBack?.invoke(view, url)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                LogUtil.w("onReceivedError", request?.url)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                LogUtil.w("onReceivedHttpError", request?.url, errorResponse)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                LogUtil.i(
                    consoleMessage.messageLevel(),
                    consoleMessage.lineNumber(),
                    consoleMessage.sourceId(),
                    consoleMessage.message()
                )
                return super.onConsoleMessage(consoleMessage)
            }

        }

    }

    fun loadUrl(url: String, onPageFinishCallBack: ((view: WebView?, url: String?) -> Unit)? = null) {
        this.onPageFinishCallBack = onPageFinishCallBack
        webView.loadUrl(url)
        LogUtil.i("webview load url:", url)
    }

    fun attachView(root: ViewGroup): WebViewProvider {
        if (root.contains(webView)) {
            return this
        }
        root.addView(webView, MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT)
        return this
    }

    class WebUrlInterceptor() {
        fun shouldOverrideUrlLoading(view: WebView?, uri: Uri?): Boolean {
            if (uri == null || view == null) return false

            val scheme = uri.scheme.toElseEmptyString().lowercase()
            if (scheme.isNotBlank() && scheme != "http" && scheme != "https") {
//                webViewLinker.callBack?.invoke(view, uri)
                LogUtil.w("webView appLink:", uri)
                MaskAppRouter.route(view.context, uri.toString())
            }
            return false
        }
    }

}