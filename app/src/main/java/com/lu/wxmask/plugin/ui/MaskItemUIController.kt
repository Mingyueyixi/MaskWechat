package com.lu.wxmask.plugin.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.setPadding
import com.lu.magic.util.SizeUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean

internal class MaskItemUIController(private val context: Context, private val mask: MaskItemBean) {
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
        it.hint = "糊脸提示内容，如：${Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT}"
        when (mask.tipMode) {
            Constrant.WX_MASK_TIP_MODE_SILENT -> {
                it.setText(MaskItemBean.AlertTipData.from(mask).mess)
                it.visibility = View.GONE
            }
            Constrant.WX_MASK_TIP_MODE_ALERT -> it.setText(MaskItemBean.AlertTipData.from(mask).mess)
            else -> it.setText(Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT)
        }
    }
    val spinnerDataList = arrayListOf(
        Constrant.WX_MASK_TIP_MODE_ALERT to "提示模式",
        Constrant.WX_MASK_TIP_MODE_SILENT to "静默模式"
    )
    var spinner: Spinner = Spinner(context).also {
        it.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        it.adapter = object : BaseAdapter() {

            override fun getCount(): Int {
                return spinnerDataList.size
            }

            override fun getItem(position: Int): Pair<Int, String> {
                return spinnerDataList[position]
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var itemView = convertView
                if (convertView == null) {
                    itemView = TextView(context)
                    itemView.setPadding(dp24 / 6)
                }
                if (itemView is TextView) {
                    itemView.text = getItem(position).second
                }
                return itemView!!
            }
        }
        it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val itemData = spinnerDataList[position]
                when (itemData.first) {
                    Constrant.WX_MASK_TIP_MODE_ALERT -> {
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
    val spinnerSelectedItem: Pair<Int, String>
        get() = spinnerDataList[spinner.selectedItemPosition]

    init {
        root.addView(etMaskId, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(etTagName, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(spinner, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(etTipMess, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

}
