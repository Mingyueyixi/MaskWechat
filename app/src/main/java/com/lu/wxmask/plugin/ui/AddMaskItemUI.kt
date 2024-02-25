package com.lu.wxmask.plugin.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.kxt.toElseEmptyString
import com.lu.magic.util.kxt.toElseString
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.bean.QuickTemporaryBean
import com.lu.wxmask.util.ConfigUtil

class AddMaskItemUI(
    private val context: Context,
    private val lst: List<MaskItemBean>,
) {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private var configListener: ((DialogInterface, MaskItemBean) -> Unit)? = null
    private var onFreeButtonListener: DialogInterface.OnClickListener? = null
    private var chatUserId = ""
    private var tagName = ""

    //空闲的按钮的文字
    private var freeButtonText: CharSequence? = null

    fun setChatUserId(chatUserId: String?): AddMaskItemUI {
        this.chatUserId = chatUserId?:""
        return this
    }

    fun setTagName(tagName: String?): AddMaskItemUI {
        this.tagName = tagName?:""
        return this
    }

    fun setConfirmListener(listener: (DialogInterface, MaskItemBean) -> Unit): AddMaskItemUI {
        this.configListener = listener
        return this
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener): AddMaskItemUI {
        onDismissListener = listener
        return this
    }

    fun setFreeButton(text: CharSequence, listener: DialogInterface.OnClickListener?): AddMaskItemUI {
        freeButtonText = text
        onFreeButtonListener = listener
        return this
    }

    fun show() {
        val ui = MaskItemUIController(context, MaskItemBean(chatUserId, tagName))

        AlertDialog.Builder(context)
            .setTitle("添加配置")
            .setIcon(context.applicationInfo.icon)
            .setView(ui.root)
            .setNegativeButton("关闭", null)
            .setPositiveButton("确定", null)
            .setNeutralButton(freeButtonText, onFreeButtonListener)
            .setOnDismissListener(onDismissListener)
            .show()
            .also { dialog ->
                //重写确定按钮监听，不消失对话框
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val maskId = ui.etMaskId.text.toElseEmptyString()
                    val tipMess = ui.etTipMess.text.toElseEmptyString()

                    if (maskId.isEmpty() || tipMess.isEmpty()) {
                        ToastUtil.show("不能为空！")
                        return@setOnClickListener
                    }
                    if (MaskUtil.checkExitMaskId(lst, maskId)) {
                        ToastUtil.show("配置已存在！")
                        return@setOnClickListener
                    }
                    val maskName = ui.etTagName.text.toElseEmptyString()

                    val tipMode = ui.tipSpinnerSelectedItem.first
                    val tipData = GsonUtil.toJsonTree(MaskItemBean.TipData(tipMess)).asJsonObject

                    MaskItemBean(maskId, maskName, tipMode, tipData).let {
                        ui.etMapId.text?.apply {
                            it.mapId = this.toString()
                        }
                        ConfigUtil.addMaskList(it)
                        configListener?.invoke(dialog, it)
                    }
                    dialog.dismiss()
                }
            }
    }
}

