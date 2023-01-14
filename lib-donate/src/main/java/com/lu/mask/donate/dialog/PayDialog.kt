package com.lu.mask.donate.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import com.lu.mask.donate.R
import com.lu.mask.donate.databinding.DialogPayBinding

class PayDialog private constructor(val P: DialogParams) : Dialog(P.context) {

    private lateinit var bindding: DialogPayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.let {
//            it.decorView.setPadding(0)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.setClipToOutline(false)
        }
        bindding = DialogPayBinding.inflate(LayoutInflater.from(context), null, false)
        setContentView(bindding.root)

        bindding.root.setOnClickListener {
            P.contentClickListener?.onClick(this, 0)
        }
        bindding.ivDownload.setOnClickListener {
            P.downloadIconClickListener?.onClick(this, 0)
        }
        bindding.ivQrPayImg.setOnClickListener {
            P.qrIconClickListener?.onClick(this, 0)
        }
        bindding.ivQrPayImg.setOnLongClickListener {
            P.qrIconLongClickListener?.onClick(this, 0)
            return@setOnLongClickListener false
        }
        bindding.ivQrPayImg.setImageResource(P.payImgResId)
    }

    class DialogParams(val context: Context) {
        var contentClickListener: DialogInterface.OnClickListener? = null
        var qrIconClickListener: DialogInterface.OnClickListener? = null
        var qrIconLongClickListener: DialogInterface.OnClickListener? = null
        var downloadIconClickListener: DialogInterface.OnClickListener? = null
        var payImgResId: Int = R.mipmap.ic_alipay_qr
    }

    class Builder(val context: Context) {
        private val P = DialogParams(context)

        fun setContentClickListener(listener: DialogInterface.OnClickListener): Builder {
            P.contentClickListener = listener
            return this
        }

        fun setQRIconClickListener(listener: DialogInterface.OnClickListener): Builder {
            P.qrIconClickListener = listener
            return this
        }

        fun setQRIconLongClickListener(listener: DialogInterface.OnClickListener): Builder {
            P.qrIconLongClickListener = listener
            return this
        }

        fun setDownloadIconClickListener(listener: DialogInterface.OnClickListener): Builder {
            P.downloadIconClickListener = listener
            return this
        }

        fun setPayImgResId(resId: Int): Builder {
            P.payImgResId = resId
            return this
        }

        fun show(): PayDialog {
            return PayDialog(P).also {
                it.show()
            }
        }
    }

}