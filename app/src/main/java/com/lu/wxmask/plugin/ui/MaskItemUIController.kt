package com.lu.wxmask.plugin.ui

import android.content.Context
import android.text.InputType
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
import com.lu.magic.frame.baseutils.kxt.toElseString
import com.lu.magic.util.SizeUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.bean.QuickTemporaryBean
import com.lu.wxmask.util.ConfigUtil

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


    val blockTemporary = LinearLayout(context).also { block ->
        block.orientation = LinearLayout.VERTICAL

        val quick = QuickTemporaryBean(ConfigUtil.getTemporaryJson()?: JsonObject())
        block.addView(LinearLayout(context).also { row ->
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL

            row.addView(TextView(context).also {
                it.text = "聊天页临时解除（全局配置）"
            })
            row.addView(
                Spinner(context).also {
                    viewId["spTemporaryMode"] = it
                    it.gravity = Gravity.RIGHT
                    val dataList = arrayListOf(
                        Constrant.CONFIG_TEMPORARY_MODE_QUICK_CLICK to "快速点击",
                        Constrant.CONFIG_TEMPORARY_MODE_LONG_PRESS to "长按解除",
                        Constrant.CONFIG_TEMPORARY_MODE_CIPHER to "长按解除"

                    )
                    it.adapter = SpinnerListAdapter(dataList)
                    it.visibility = View.GONE
                },
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        })

        block.addView(LinearLayout(context).also { row ->
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.addView(
                TextView(context).also {
                    it.text = "间隔时长/毫秒："
                },
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            row.addView(
                EditText(context).also {
                    viewId["etDuration"] = it
                    it.inputType = InputType.TYPE_CLASS_NUMBER
                    it.setText(quick.duration.toElseString("150"))
                },
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        block.addView(LinearLayout(context).also { row ->
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.addView(
                TextView(context).also {
                    it.text = "点击次数/次："
                },
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            row.addView(EditText(context).also {
                viewId["etClickCount"] = it
                it.inputType = InputType.TYPE_CLASS_NUMBER
                it.setText(quick.clickCount.toElseString("5"))
            }, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    val etClickCount: EditText = viewId["etClickCount"] as EditText
    val etDuration: EditText = viewId["etDuration"] as EditText

    init {
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also {
            it.topMargin = dp24 / 6
        }
        root.addView(etMaskId, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(etTagName, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(tipSpinner, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(etTipMess, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(blockTemporary, lp)

    }

    inner class SpinnerListAdapter(spinnerDataList: ArrayList<Pair<Int, String>>) :
        CommonListAdapter<Pair<Int, String>, AbsListAdapter.ViewHolder>() {
        init {
            setData(spinnerDataList)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(context).also { it.setPadding(dp24 / 6) })
        }

        override fun onBindViewHolder(vh: ViewHolder, position: Int, parent: ViewGroup) {
            val itemView = vh.itemView
            if (itemView is TextView) {
                itemView.text = getItem(position)?.second
            }
        }
    }


}
