package com.lu.mask.donate

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.log.LogUtil
import com.lu.mask.donate.dialog.PayDialog
import java.net.URLEncoder
import kotlin.random.Random

class DonatePresenter(val vm: DonateViewModel) {
    companion object {
        val TAG: String = ">>>" + DonatePresenter::class.java.simpleName.toString()

        fun from(fragment: Fragment): DonatePresenter {
            val vm = ViewModelProvider(fragment)[DonateViewModel::class.java]
            initViewModel(vm, fragment)
            return DonatePresenter(vm)
        }

        fun from(activity: ComponentActivity): DonatePresenter {
            val vm = ViewModelProvider(activity)[DonateViewModel::class.java]
            initViewModel(vm, activity)
            return DonatePresenter(vm)
        }

        private fun initViewModel(vm: DonateViewModel, lifecycleOwner: LifecycleOwner) {
            vm.uiToastLive.removeObservers(lifecycleOwner)
            vm.uiToastLive.observe(lifecycleOwner) {
                ToastUtil.show(it)
            }
        }

    }


    fun lecturing(context: Context, alipayQRPersonLink: String = "https://qr.alipay.com/tsx18437nsf7otyumo1gc2e") {
        lecturingWith(context)
            .setAlipayQRPersonLink(alipayQRPersonLink)
            .addPayImgRes(R.mipmap.ic_alipay_qr, "alipay_qr.jpg")
            .addPayImgRes(R.mipmap.ic_wxpay_qr, "wxpay_qr.jpg")
            .applyWith { ctx, imgResId, fileName ->
                showDonateDialog(ctx, imgResId, fileName)
            }
    }

    fun lecturingWith(context: Context): LecturingAction {
        return LecturingAction(context)
    }

    private fun showDonateDialog(context: Context, payImgResId: Int, fileName: String) {
        PayDialog.Builder(context).setPayImgResId(payImgResId).setQRIconClickListener { dialog, _ ->
            dialog.dismiss()
        }.setQRIconLongClickListener { dialog, _ ->
            dialog.dismiss()
            vm.savePayImg(context, payImgResId, fileName)
        }.setDownloadIconClickListener { dialog, _ ->
            dialog.dismiss()
            vm.savePayImg(context, payImgResId, fileName)
        }.show()
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

        fun applyWith(elseBlock: (ctx: Context, imgResId: Int, fileName: String) -> Unit) {
            val index = Random.nextInt(0, payImgResIdStore.size)
            val resInt = payImgResIdStore.keys.toMutableList()[index]
            val fileName = payImgResIdStore[resInt]

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
                elseBlock.invoke(context, resInt, fileName ?: "")
            }

        }

    }

}