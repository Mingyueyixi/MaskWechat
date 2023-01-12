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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lu.magic.ui.recycler.MultiAdapter
import com.lu.magic.ui.recycler.MultiViewHolder
import com.lu.magic.ui.recycler.SimpleItemType
import com.lu.magic.util.ResUtil
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
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
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            contentView = onCreateView()

            setOnDismissListener {
                activity.window.attributes.let {
                    it.alpha = 1f
                    activity.window.attributes = it
                }
            }
            //全屏。显示到状态栏
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
    private lateinit var listAdapter: MultiAdapter<MaskItemBean>
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
        val recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        listAdapter = MultiAdapter<MaskItemBean>()
            .addData(ConfigUtil.getMaskList().let {
                val keyMap = LinkedHashMap<String, MaskItemBean>()
                //去重
                it.forEach {
                    keyMap.put(it.maskId, it)
                }
                keyMap.values.toList()
            })
            .addItemType(object : SimpleItemType<MaskItemBean>() {
                override fun createViewHolder(
                    adapter: MultiAdapter<MaskItemBean>,
                    parent: ViewGroup,
                    viewType: Int
                ): MultiViewHolder<MaskItemBean> {

                    val itemView = TextView(context).also {
                        it.layoutParams = RecyclerView.LayoutParams(
                            RecyclerView.LayoutParams.MATCH_PARENT,
                            RecyclerView.LayoutParams.WRAP_CONTENT
                        )
                        val dp6 = SizeUtil.dp2px(context.resources, 6f).toInt()
                        it.setPadding(dp6)
                        RippleApplyUtil.apply(it, RectangleRippleBuilder(Color.TRANSPARENT, 0x33333333))
                    }

                    return object : MultiViewHolder<MaskItemBean>(itemView) {
                        init {
                            itemView.setOnLongClickListener {
                                val position = layoutPosition
                                showDeleteMaskItemDialog(position)
                                return@setOnLongClickListener false
                            }
                            itemView.setOnClickListener {
                                showEditMaskItemDialog(layoutPosition)
                            }
                        }

                        override fun onBindView(
                            adapter: MultiAdapter<MaskItemBean>,
                            itemModel: MaskItemBean,
                            position: Int
                        ) {
                            itemView.text = if (itemModel.tagName.isNullOrEmpty()) {
                                itemModel.maskId
                            } else {
                                "${itemModel.maskId} (${itemModel.tagName})"
                            }
                        }
                    }
                }

            })
        recyclerView.adapter = listAdapter
        contentView.addView(recyclerView)
    }

    private fun showDeleteMaskItemDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("是否删除？")
            .setNegativeButton("确定") { _, _ ->
                listAdapter.getData().removeAt(position)
                ConfigUtil.setMaskList(listAdapter.getData())
                listAdapter.notifyItemRemoved(position)
            }
            .setNeutralButton("取消") { _, _ ->

            }
            .show()
    }

    private fun showAddMaskItemDialog() {
        AddMaskItemUI(context, listAdapter.getData())
            .setConfirmListener { _, maskItemBean ->
                listAdapter.addData(maskItemBean)
                listAdapter.notifyItemInserted(listAdapter.itemCount - 1)
            }.show()
    }

    private fun showEditMaskItemDialog(position: Int) {
        EditMaskItemUI(context, listAdapter.getData(), position)
            .setOnConfigChangeListener { _, _, mode ->
                when (mode) {
                    EditMaskItemUI.MODE_CONFIG_UPDATE -> listAdapter.notifyItemChanged(position)
                    EditMaskItemUI.MODE_CONFIG_REMOVE -> listAdapter.notifyItemRemoved(position)
                }
            }
            .show()
    }


}
