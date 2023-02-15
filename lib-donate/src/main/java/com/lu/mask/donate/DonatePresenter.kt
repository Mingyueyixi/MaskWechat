package com.lu.mask.donate

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.lu.magic.util.AppUtil
import com.lu.magic.util.log.LogUtil
import com.lu.mask.donate.dialog.PayDialog
import java.net.URLEncoder
import kotlin.random.Random

class DonatePresenter {

    private val model = DonateModel()

    companion object {
        val TAG: String = ">>>" + DonatePresenter::class.java.simpleName.toString()
        fun create(): DonatePresenter {
            return DonatePresenter()
        }
    }

    fun lecturing(context: Context, alipayQRPersonLink: String = "https://qr.alipay.com/tsx18437nsf7otyumo1gc2e") {
        lecturingWith(context)
            .setAlipayQRPersonLink(alipayQRPersonLink)
            .addPayImgRes(R.mipmap.ic_alipay_qr, "alipay_qr.webp")
            .runWith { imgResId, fileName ->
                showDonateDialog(context, imgResId, fileName)
            }
    }

    fun lecturingWith(context: Context): LecturingAction {
        return LecturingAction(context)
    }

    fun showDonateDialog(context: Context, payImgResId: Int, fileName: String) {
        PayDialog.Builder(context).setPayImgResId(payImgResId).setQRIconClickListener { dialog, _ ->
            dialog.dismiss()
        }.setQRIconLongClickListener { dialog, _ ->
            dialog.dismiss()
            savePayImgAndTip(AppUtil.getContext(), payImgResId, fileName)
        }.setDownloadIconClickListener { dialog, _ ->
            dialog.dismiss()
            savePayImgAndTip(AppUtil.getContext(), payImgResId, fileName)
        }.show()
    }

    private fun savePayImgAndTip(context: Context, payImgResId: Int, fileName: String) {
        model.savePayImg(context, payImgResId, fileName) {
            ExecutorUtil.runOnMain {
                if (it.isSuccess) {
                    Toast.makeText(context, R.string.donate_save_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.donate_save_fail, Toast.LENGTH_SHORT).show()
                    LogUtil.w(it.exceptionOrNull())
                }
            }
        }
    }

    class LecturingAction(private val context: Context) {
        private val payImgResIdStore = LinkedHashMap<Int, String>()
        private var alipayQRPersonLink: String = ""

        fun addPayImgRes(imgResId: Int, fileName: String): LecturingAction {
            payImgResIdStore[imgResId] = fileName
            return this
        }

        fun setAlipayQRPersonLink(link: String): LecturingAction {
            this.alipayQRPersonLink = link
            return this
        }

        fun runWith(elseBlock: (imgResId: Int, fileName: String) -> Unit) {
            val index = Random.nextInt(0, payImgResIdStore.size)
            val resInt = payImgResIdStore.keys.toMutableList()[index]
            val fileName = payImgResIdStore[resInt]

            if (alipayQRPersonLink.isNullOrBlank()) {
                elseBlock.invoke(resInt, fileName ?: "")
            } else {
                //支付宝个人收钱码，二维码内容
                val qrPersonLink = URLEncoder.encode(alipayQRPersonLink, "UTF-8")
                // 指定扫码结果并跳过扫码
                val intent = Intent.parseUri(
                    "intent://platformapi/startapp?saId=10000007&qrcode=$qrPersonLink" + "#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end",
                    Intent.URI_INTENT_SCHEME
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    LogUtil.e(TAG, "支付宝跳转失败")
                    elseBlock.invoke(resInt, fileName ?: "")
                }
            }

        }

    }

}