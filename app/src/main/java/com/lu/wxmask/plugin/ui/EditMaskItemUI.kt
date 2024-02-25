package com.lu.wxmask.plugin.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.kxt.toElseEmptyString
import com.lu.magic.util.kxt.toElseString
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.bean.QuickTemporaryBean
import com.lu.wxmask.util.ConfigUtil

class EditMaskItemUI(
    private val context: Context,
    private val lst: MutableList<MaskItemBean>,
    private val position: Int
) {
    companion object {
        val MODE_CONFIG_UPDATE = 0
        val MODE_CONFIG_REMOVE = 1
    }


    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private var onConfigChangeListener: ((DialogInterface, MaskItemBean, Int) -> Unit)? = null
    private var onFreeButtonListener: DialogInterface.OnClickListener? = null

    private var freeButtonText: CharSequence? = null

    fun setOnConfigChangeListener(listener: (dialog: DialogInterface, mask: MaskItemBean, mode: Int) -> Unit): EditMaskItemUI {
        onConfigChangeListener = listener
        return this
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener): EditMaskItemUI {
        onDismissListener = listener
        return this
    }

    fun setFreeButton(text: CharSequence, listener: DialogInterface.OnClickListener?): EditMaskItemUI {
        freeButtonText = text
        onFreeButtonListener = listener
        return this
    }

    fun show() {
        val maskItemBean = lst[position]
        val ui = MaskItemUIController(context, maskItemBean)

        //spinner的模式index
        val selectModeIndex = ui.spinnerTipDataList.map { it.first }.indexOf(maskItemBean.tipMode).let {
            if (it == -1) 0 else it
        }
        ui.tipSpinner.setSelection(selectModeIndex)
        AlertDialog.Builder(context)
            .setTitle("编辑配置")
            .setIcon(context.applicationInfo.icon)
            .setView(ui.root)
            .setPositiveButton("确定", null)
            .setNegativeButton("取消", null)
            .setNeutralButton(freeButtonText, onFreeButtonListener)
            .setOnDismissListener(onDismissListener)
            .show()
            .also { dialog ->
                //重写确定按钮监听，不消失对话框
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val maskId = ui.etMaskId.text.toElseEmptyString()
                    val tipMess = ui.etTipMess.text.toElseEmptyString()
                    val tagName = ui.etTagName.text.toElseEmptyString()

                    if (tipMess.isEmpty()) {
                        ToastUtil.show("不能为空！")
                        return@setOnClickListener
                    }
                    //编辑需要确保已变更，且不在列表中，而新增则不存在是否变更的问题
                    if (maskId.isNotEmpty() && maskId != maskItemBean.maskId && MaskUtil.checkExitMaskId(lst, maskId)) {
                        ToastUtil.show("配置已存在！")
                        return@setOnClickListener
                    }
                    if (maskId.isEmpty()) {
                        //删除
                        lst.removeAt(position)
                        ConfigUtil.setMaskList(lst)
                        ToastUtil.show("已删除！")
                        onConfigChangeListener?.invoke(dialog, maskItemBean, MODE_CONFIG_REMOVE)
                    } else {
                        maskItemBean.maskId = maskId
                        maskItemBean.tipMode = ui.tipSpinnerSelectedItem.first
                        maskItemBean.tagName = tagName
                        ui.etMapId.text?.let {
                            maskItemBean.mapId = it.toString()
                        }
                        when (maskItemBean.tipMode) {
                            Constrant.CONFIG_TIP_MODE_ALERT -> GsonUtil.toJsonTree(MaskItemBean.TipData(tipMess)).asJsonObject
                            else -> null
                        }?.let {
                            maskItemBean.tipData = it
                        }

                        ConfigUtil.setMaskList(lst)
                        ToastUtil.show("已更新！")
                        onConfigChangeListener?.invoke(dialog, maskItemBean, MODE_CONFIG_UPDATE)
                    }
                    dialog.dismiss()
                }
            }


    }
}