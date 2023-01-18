package com.lu.wxmask.plugin.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.setPadding
import com.lu.magic.util.ResUtil
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.util.ConfigUtil

interface IConfigManagerUI {
    fun onCreateView(): View
    fun dismiss()
    fun show()
}

// PopWindow全屏+返回键监听弹窗，暂不需要，没有那么多配置
internal class ConfigManagerUI(private val activity: Activity) : IConfigManagerUI {
    private val popwindow = PopupWindow(activity)
    private val controller = ConfigManagerUIController(activity)

    fun initUI(): ConfigManagerUI {
        popwindow.apply {
//                setBackgroundDrawable(ColorDrawable(ResUtil.getAttrColor(activity, android.R.attr.windowBackground)))
            if (ResUtil.isAppNightMode(activity)) {
                setBackgroundDrawable(ColorDrawable(0xFF303030.toInt()))
            } else {
                setBackgroundDrawable(ColorDrawable(Color.WHITE))
            }
            // 强制适配夜间模式，系统底层直接修改的颜色
            // contentView.isForceDarkAllowed = true
            // setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            isOutsideTouchable = true
            //contentView监听返回键keylistener，需要焦点
            isFocusable = true

            activity.applicationContext.resources.displayMetrics.let {
                width = it.widthPixels
                height = MarginLayoutParams.WRAP_CONTENT
            }
            contentView = onCreateView()

            setOnDismissListener {
                activity.window.attributes.let {
                    it.alpha = 1f
                    activity.window.attributes = it
                }
            }
            //不剪切
            isClippingEnabled = false
        }

        return this
    }

    override fun onCreateView(): View {
        val root = controller.onCreateView()
        root.layoutParams.let {
            if (it is LinearLayout.LayoutParams) {
                it.marginStart = controller.dp24
                it.marginEnd = controller.dp24
            }
        }

        root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        return root
    }

    override fun dismiss() {
        popwindow.dismiss()
    }

    override fun show() {
        val anchor = activity.findViewById<View>(android.R.id.content)
        //避免首次，未创建完成，无法显示
        anchor.post {
            activity.window.attributes.let {
                it.alpha = 0.3f
                activity.window.attributes = it
            }
            popwindow.showAtLocation(anchor, Gravity.CENTER, 0, 0)
        }
    }

}


private class ConfigManagerUIController(private val context: Context) {
    private lateinit var listAdapter: CommonListAdapter<MaskItemBean, AbsListAdapter.ViewHolder>
    private lateinit var contentView: LinearLayout
    val dp24 = SizeUtil.dp2px(context.resources, 24f).toInt()

    fun onCreateView(): View {
        contentView = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(dp24)
            orientation = LinearLayout.VERTICAL
        }
        initTopLayout()
        initMaskListView()

        return contentView
    }

    private fun initTopLayout() {
        FrameLayout(context).apply {
            TextView(context).apply {
                setTextColor(context.getColor(android.R.color.tab_indicator_text))
                text = "配置管理"
            }.apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                addView(this)
            }
        }.apply {

            TextView(context).apply {
                text = "+"
                textSize = SizeUtil.sp2px(context.resources, 8f)
                setTextColor(context.getColor(android.R.color.tab_indicator_text))
                setOnClickListener {
                    showAddMaskItemDialog()
                }
                RippleApplyUtil.apply(this, RectangleRippleBuilder(Color.TRANSPARENT, 0x33333333, 4))
            }.apply {
                val size = (textSize * 1.5).toInt()
                layoutParams = FrameLayout.LayoutParams(size, size).also {
                    it.gravity = Gravity.END
                }
                this.gravity = Gravity.CENTER
                addView(this)
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER_VERTICAL
            }
            contentView.addView(this)
        }
    }

    private fun initMaskListView() {
        val listView = ListView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                MarginLayoutParams.MATCH_PARENT,
                MarginLayoutParams.WRAP_CONTENT
            ).also {
            }

            divider = null
        }

        listAdapter = object : CommonListAdapter<MaskItemBean,AbsListAdapter.ViewHolder>() {
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
                    val dp6 = SizeUtil.dp2px(context.resources, 6f).toInt()
                    it.setPadding(dp6)
                    RippleApplyUtil.apply(it, RectangleRippleBuilder(Color.TRANSPARENT, 0x33333333))
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

        listView.adapter = listAdapter
        contentView.addView(listView)
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
