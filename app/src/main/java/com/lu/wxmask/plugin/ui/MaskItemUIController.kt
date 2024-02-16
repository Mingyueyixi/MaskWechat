package com.lu.wxmask.plugin.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.setPadding
import com.google.gson.JsonObject
import com.lu.magic.util.SizeUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.bean.QuickTemporaryBean
import com.lu.wxmask.ui.adapter.SpinnerListAdapter
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.ext.dp

internal class MaskItemUIController(private val context: Context, private val mask: MaskItemBean) {
    private val viewId: MutableMap<String, View> = mutableMapOf()

    val dp24 = SizeUtil.dp2px(context.resources, 24f).toInt()
    var root: LinearLayout = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also {
            orientation = LinearLayout.VERTICAL
        }
        setPadding(dp24)
    }
    var etMaskId: EditText = EditText(context).also {
        it.hint = "糊脸Id（抓取获得）"
        it.setText(mask.maskId)
    }
    var etTagName = EditText(context).also {
        it.hint = "备注（可空，仅用于显示）"
        it.setText(mask.tagName)
    }
    var etTipMess = EditText(context).also {
        it.hint = "糊脸提示，如：${Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT}"
        when (mask.tipMode) {
            Constrant.WX_MASK_TIP_MODE_SILENT -> {
                it.setText(MaskItemBean.TipData.from(mask).mess)
                it.visibility = View.GONE
            }

            Constrant.CONFIG_TIP_MODE_ALERT -> it.setText(MaskItemBean.TipData.from(mask).mess)
            else -> it.setText(Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT)
        }
    }
    val spinnerTipDataList = arrayListOf(
        Constrant.CONFIG_TIP_MODE_ALERT to "提示模式",
        Constrant.WX_MASK_TIP_MODE_SILENT to "静默模式"
    )
    var tipSpinner: Spinner = Spinner(context).also {
        it.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        it.adapter = SpinnerListAdapter(spinnerTipDataList)
        it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val itemData = spinnerTipDataList[position]
                when (itemData.first) {
                    Constrant.CONFIG_TIP_MODE_ALERT -> {
                        etTipMess.visibility = View.VISIBLE
                    }

                    Constrant.WX_MASK_TIP_MODE_SILENT -> {
                        etTipMess.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    val tipSpinnerSelectedItem: Pair<Int, String>
        get() = spinnerTipDataList[tipSpinner.selectedItemPosition]


    init {
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 4.dp
        }
        root.addView(etMaskId, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(etTagName, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(tipSpinner, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(etTipMess, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

    }


}
