package com.lu.wxmask.route

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import com.lu.magic.util.AppUtil
import com.lu.magic.util.log.LogUtil
import com.lu.mask.donate.DonatePresenter
import com.lu.wxmask.App
import com.lu.wxmask.ui.WebViewActivity
import com.lu.wxmask.ui.vm.AppUpdateViewModel

/**
 * app内部跳转协议实现，如：
 * maskwechat://com.lu.wxmask/feat/checkAppUpdate
 * maskwechat://com.lu.wxmask/page/about
 * maskwechat://com.lu.wxmask/page/webView?url=http://www.baidu.com
 */
class MaskAppRouter {
    companion object {
        val vailScheme = "maskwechat"
        val vailHost = "com.lu.wxmask"
        private val appUpdateViewModel = ViewModelProvider(App.instance)[AppUpdateViewModel::class.java]
        private val donatePresenter by lazy { DonatePresenter.create() }
        fun routeCheckAppUpdateFeat() {
            route(AppUtil.getContext(), "maskwechat://com.lu.wxmask/feat/checkAppUpdate")
        }

        fun routeDonateFeat() {
            route(AppUtil.getContext(), "maskwechat://com.lu.wxmask/feat/donate")
        }

        fun isMaskAppLink(uri: Uri?): Boolean {
            if (uri == null) {
                return false
            }
            return vailScheme == uri.scheme && vailHost == uri.host
        }


        @JvmOverloads
        fun route(context: Context = AppUtil.getContext(), url: String) {
            val uri = Uri.parse(url)
            if (isMaskAppLink(uri)) {
                val pathSegments = uri.pathSegments
                if (pathSegments.size == 2) {
                    val group = pathSegments[0] ?: ""
                    val name = pathSegments[1] ?: ""
                    routeMaskAppLink(context, uri, group, name)
                } else {
                    LogUtil.w("is Mask App link ,but pathSegments‘s size is not enough")
                }
            } else {
                val intent = Intent.parseUri(url, Intent.URI_ALLOW_UNSAFE)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }

//        private fun routeOtherAppLink(context: Context, uri: Uri) {
//            val intent = Intent()
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.action = Intent.ACTION_VIEW
//            intent.data = uri
//            context.startActivity(intent)
//        }

        private fun routeMaskAppLink(context: Context, uri: Uri, group: String, name: String) {
            when (group) {
                "feat" -> routeFeatGroup(context, uri, name)
                "page" -> routePageGroup(context, uri, name)
                else -> {
                    LogUtil.w(group, "for mask link 's group not impl")
                }
            }
        }

        private fun routeFeatGroup(context: Context, uri: Uri, name: String) {
            when (name) {
                "checkAppUpdate" -> appUpdateViewModel.checkOnce(context)
                "donate" -> donatePresenter.lecturing(context)
                else -> LogUtil.w(name, "for mask link featGroup not impl")
            }

        }

        private fun routePageGroup(context: Context, uri: Uri, name: String) {
            when (name) {
                "webView" -> {
                    val url = uri.getQueryParameter("url")
                    val intent = Intent(AppUtil.getContext(), WebViewActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("url", url)
                    context.startActivity(intent)
                }

                else -> LogUtil.w(name, "for mask link pageGroup not impl")
            }
        }

    }
}