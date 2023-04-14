package com.lu.wxmask.ui.wrapper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
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
import androidx.core.view.contains
import com.lu.magic.util.kxt.toElseEmptyString
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.route.MaskAppRouter
import java.net.URL

class WebViewComponent(context: Context) {
    var forceHtml = false

    companion object {
        private val TAG: String = WebViewComponent::class.java.simpleName
    }

    private var webviewComponentCallBack: WebViewComponentCallBack? = null
    val webView = WebView(context)

    init {
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.settings.let {
            it.domStorageEnabled = true
            it.javaScriptEnabled = true
            it.databaseEnabled = true
            it.javaScriptCanOpenWindowsAutomatically = true
            it.allowFileAccess = true
            it.allowContentAccess = true
            it.allowFileAccessFromFileURLs = true
            it.allowUniversalAccessFromFileURLs = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.isAlgorithmicDarkeningAllowed = true
            }
        }
        webView.webViewClient = object : WebViewClient() {
            val webUrlInterceptor = WebUrlInterceptor()
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                Log.i(TAG, "webViewLinker onLoadResource $url")
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                LogUtil.i(TAG, "shouldOverrideUrlLoading", url)
                if (webUrlInterceptor.shouldOverrideUrlLoading(view, Uri.parse(url))) {
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun shouldInterceptRequest(view: WebView?, url: String): WebResourceResponse? {
                LogUtil.i(TAG, "shouldInterceptRequest", url)
                if (forceHtml && url.endsWith(".html")) {
                    val iStream = try {
                        URL(url).openStream()
                    } catch (e: Throwable) {
                        null
                    }
                    if (iStream != null) {
                        return WebResourceResponse("text/html", "utf-8", iStream)
                    }
                }
                return super.shouldInterceptRequest(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                LogUtil.i(TAG, "onPageStarted", url)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                LogUtil.i(TAG, "onPageFinished", url)
                webviewComponentCallBack?.onPageFinish(view, url)
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                LogUtil.w(TAG, "onReceivedError", errorCode)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                LogUtil.w(TAG, "onReceivedError", request?.url)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                LogUtil.w(TAG, "onReceivedHttpError", request?.url, errorResponse)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
                LogUtil.w(TAG, "onReceivedSslError", error)
            }

        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                val level = consoleMessage.messageLevel()
                val msgArr = arrayOf(
                    TAG,
                    "console: ",
                    level,
                    consoleMessage.lineNumber(),
                    consoleMessage.sourceId(),
                    consoleMessage.message()
                )

                when (level) {
                    ConsoleMessage.MessageLevel.DEBUG -> LogUtil.d(*msgArr)
                    ConsoleMessage.MessageLevel.WARNING -> LogUtil.w(*msgArr)
                    ConsoleMessage.MessageLevel.ERROR -> LogUtil.e(*msgArr)
                    else -> LogUtil.i(*msgArr)
                }
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                webviewComponentCallBack?.onReceivedTitle(view, title)
            }

        }

    }

    fun loadUrl(url: String, callBack: WebViewComponentCallBack? = null) {
        this.webviewComponentCallBack = callBack
        webView.loadUrl(url)
        LogUtil.i(TAG, "webview load url:", url)
    }

    fun attachView(root: ViewGroup): WebViewComponent {
        if (root.contains(webView)) {
            return this
        }
        root.addView(webView, MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT)
        return this
    }

    fun destroy() {
        webView.loadUrl("about:blank")
        webView.clearMatches()
        webView.clearHistory()
        webView.destroy()
    }

    class WebUrlInterceptor() {
        fun shouldOverrideUrlLoading(view: WebView?, uri: Uri?): Boolean {
            if (uri == null || view == null) return false

            val scheme = uri.scheme.toElseEmptyString().lowercase()
            if (uri.toString().matches(Regex("https?://.+\\.(?:apk|zip|rar|gzip)", RegexOption.IGNORE_CASE))) {
                MaskAppRouter.route(view.context, uri.toString())
                return true
            }
            if (!"http".equals(scheme, true)
                && !"https".equals(scheme, true)
                && !"file".equals(scheme, true)
                && !"about".equals(scheme, true)
            ) {
//                webViewLinker.callBack?.invoke(view, uri)
                LogUtil.i(TAG, "webView appLink:", uri)
                MaskAppRouter.route(view.context, uri.toString())
                return true
            }
            return false
        }
    }

}