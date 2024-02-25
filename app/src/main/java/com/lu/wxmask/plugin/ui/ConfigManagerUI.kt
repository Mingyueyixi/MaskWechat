package com.lu.wxmask.plugin.ui

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.setPadding
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.plugin.ui.view.BottomPopUI
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.ext.dp


interface IConfigManagerUI {
    fun onCreateView(): View
    fun dismiss()
    fun show()
}

// PopWindow全屏+返回键监听弹窗，暂不需要，没有那么多配置
internal class ConfigManagerUI(private val context: Activity) : IConfigManagerUI {
    private lateinit var listAdapter: CommonListAdapter<MaskItemBean, AbsListAdapter.ViewHolder>
    private val popwindow: BottomPopUI
    private lateinit var listView: ListView

    init {
        popwindow = BottomPopUI(onCreateView())
        popwindow.needScrollChild = listView
    }


    override fun dismiss() {
        popwindow.dismiss()
    }

    override fun show() {
        popwindow.show()
    }


    override fun onCreateView(): View {
        return LinearLayout(context).apply {
            layoutParams = MarginLayoutParams(MATCH_PARENT, 480.dp)
            setPadding(24.dp)
            orientation = LinearLayout.VERTICAL
            // 强制适配夜间模式，系统底层直接修改的颜色，不一定生效
            // isForceDarkAllowed = true
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Theme.Color.bgPrimary(context))
                cornerRadii = floatArrayOf(16f.dp, 16f.dp, 16f.dp, 16f.dp, 0f, 0f, 0f, 0f)
            }

            addView(initTopLayout())
            addView(initMaskListView())
        }
    }

    private fun initTopLayout(): FrameLayout {

        return FrameLayout(context).apply {

            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            addView(TextView(context).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                }
                setTextColor(context.getColor(android.R.color.tab_indicator_text))
                textSize = 16f
                text = "配置管理"
            })
            addView(TextView(context).apply {
                text = "+"
                textSize = SizeUtil.sp2px(context.resources, 8f)
                setTextColor(context.getColor(android.R.color.tab_indicator_text))
                setOnClickListener {
                    showAddMaskItemDialog()
                }
                RippleApplyUtil.apply(this, RectangleRippleBuilder(Color.TRANSPARENT, Theme.Color.bgRippleColor, 4))
                val size = (textSize * 1.5).toInt()
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.END
                }
                this.gravity = Gravity.CENTER
            })
        }
    }

    private fun initMaskListView(): ListView {
        initListAdapter()
        return ListView(context).apply {
            listView = this
            isVerticalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT).apply {
                topMargin = 16.dp
            }
            divider = null
            selector = ColorDrawable(Color.TRANSPARENT)
            adapter = listAdapter
        }

    }

    private fun initListAdapter() {
        listAdapter = object : CommonListAdapter<MaskItemBean, AbsListAdapter.ViewHolder>() {
            init {
                //去重
                val dataListTemp = ConfigUtil.getMaskList().let {
                    val keyMap = LinkedHashMap<String, MaskItemBean>()
                    //去重
                    it.forEach { bean ->
                        keyMap[bean.maskId] = bean
                    }
                    keyMap.values.toList()
                }
                setData(dataListTemp)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val itemView = TextView(context).also {
                    it.layoutParams = MarginLayoutParams(
                        MarginLayoutParams.MATCH_PARENT,
                        MarginLayoutParams.WRAP_CONTENT
                    )
                    it.setPadding(6.dp)
                    RippleApplyUtil.apply(it, RectangleRippleBuilder(Color.TRANSPARENT, Theme.Color.bgRippleColor))
                }

                return object : ViewHolder(itemView) {
                    init {
                        itemView.setOnLongClickListener {
                            showDeleteMaskItemDialog(layoutPosition)
                            return@setOnLongClickListener false
                        }
                        itemView.setOnClickListener {
                            showEditMaskItemDialog(layoutPosition)
                        }
                    }
                }
            }

            override fun onBindViewHolder(vh: ViewHolder, position: Int, parent: ViewGroup) {
                val itemView = vh.itemView
                val itemModel = dataList[position]

                if (itemView is TextView) {
                    itemView.text = if (itemModel.tagName.isEmpty()) {
                        itemModel.maskId
                    } else {
                        "${itemModel.maskId} (${itemModel.tagName})"
                    }
                }

            }

        }

    }

    private fun showDeleteMaskItemDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("是否删除？")
            .setNegativeButton("确定") { _, _ ->
                listAdapter.removeAt(position)
                ConfigUtil.setMaskList(listAdapter.getData())
                listAdapter.notifyDataSetChanged()
            }
            .setNeutralButton("取消") { _, _ ->

            }
            .show()
    }

    private fun showAddMaskItemDialog() {
        AddMaskItemUI(context, listAdapter.getData())
            .setConfirmListener { _, maskItemBean ->
                listAdapter.addData(maskItemBean)
                listAdapter.notifyDataSetChanged()
            }.show()
    }

    private fun showEditMaskItemDialog(position: Int) {
        EditMaskItemUI(context, listAdapter.getData(), position)
            .setOnConfigChangeListener { _, _, mode ->
//                when (mode) {
//                    EditMaskItemUI.MODE_CONFIG_UPDATE -> listAdapter.notifyItemChanged(position)
//                    EditMaskItemUI.MODE_CONFIG_REMOVE -> listAdapter.notifyItemRemoved(position)
//                }
                listAdapter.notifyDataSetChanged()
            }
            .show()
    }


}
