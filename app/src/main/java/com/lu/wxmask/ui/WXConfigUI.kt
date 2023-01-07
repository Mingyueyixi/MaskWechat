package com.lu.wxmask.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lu.magic.ui.recycler.MultiAdapter
import com.lu.magic.ui.recycler.MultiViewHolder
import com.lu.magic.ui.recycler.SimpleItemType
import com.lu.magic.util.GsonUtil
import com.lu.magic.util.ResUtil
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
import com.lu.wxmask.Constrant
import com.lu.wxmask.bean.MaskItemBean
import com.lu.wxmask.toElseEmptyString
import com.lu.wxmask.util.ConfigUtil


class WXConfigUI(val activity: Activity) {

    fun show() {
        PopUI(activity).initUI().show()
    }

    // PopWindow全屏+返回键监听弹窗，暂不需要，没有那么多配置
    private class PopUI(private val activity: Activity) : IConfigUI {
        private val popwindow = PopupWindow(activity)
        private val controller = ConfigUIController(activity)

        fun initUI(): PopUI {
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

            root.setOnKeyListener { v, keyCode, event ->
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

//            (popwindow.contentView.parent as? ViewGroup)?.let {
//                it.setPadding(controller.dp24)
//            }
        }

    }

    interface IConfigUI {
        fun onCreateView(): View
        fun dismiss()
        fun show()
    }

    private class ConfigUIController(private val context: Context) {
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
                        keyMap.put(it.keyWord, it)
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
                                (itemView as TextView).let {
                                    it.text = itemModel.keyWord
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
            val ui = MaskItemDialogController(context, Constrant.WX_MASK_TIP_MODE_ALERT)
            AlertDialog.Builder(context)
                .setView(ui.root)
                .setNegativeButton("确定", null)
                .setNeutralButton("取消", null)
                .show()
                .also { dialog ->
                    //重写确定按钮监听，不消失对话框
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                        val keyWord = ui.etKeyWord.text.toElseEmptyString()
                        val tipMess = ui.etTipMess.text.toElseEmptyString()

                        if (keyWord.isEmpty() || tipMess.isEmpty()) {
                            ToastUtil.show("不能为空！")
                            return@setOnClickListener
                        }
                        if (checkExitKeyWord(keyWord)) {
                            ToastUtil.show("配置已存在！")
                            return@setOnClickListener
                        }
                        val tipMode = ui.spinnerSelectedItem.first
                        val tipData = GsonUtil.toJsonTree(MaskItemBean.AlertTipData(tipMess)).asJsonObject

                        MaskItemBean(keyWord, tipMode, tipData).let {
                            ConfigUtil.addMaskList(it)
                            listAdapter.addData(it)
                        }
                        listAdapter.notifyItemInserted(listAdapter.itemCount - 1)
                        dialog.dismiss()
                    }
                }
        }

        private fun showEditMaskItemDialog(position: Int) {
            val maskItemBean = listAdapter.getItem(position)!!
            val ui = MaskItemDialogController(context, maskItemBean.tipMode)

            ui.etKeyWord.setText(maskItemBean.keyWord)
            //spinner的模式index
            val selectModeIndex = ui.spinnerDataList.map { it.first }.indexOf(maskItemBean.tipMode).let {
                if (it == -1) 0 else it
            }
            ui.spinner.setSelection(selectModeIndex)
            AlertDialog.Builder(context)
                .setView(ui.root)
                .setNegativeButton("确定", null)
                .setNeutralButton("取消", null)
                .show()
                .also { dialog ->
                    //重写确定按钮监听，不消失对话框
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                        val keyWord = ui.etKeyWord.text.toElseEmptyString()
                        val tipMess = ui.etTipMess.text.toElseEmptyString()

                        if (tipMess.isEmpty()) {
                            ToastUtil.show("不能为空！")
                            return@setOnClickListener
                        }
                        if (keyWord.isEmpty()) {
                            //删除
                            ToastUtil.show("已删除！")
                            listAdapter.getData().removeAt(position)
                            ConfigUtil.setMaskList(listAdapter.getData())
                            listAdapter.notifyItemRemoved(position)
                            dialog.dismiss()
                            return@setOnClickListener
                        }
                        //编辑需要确保已变更，且不在列表中，而新增则不存在是否变更的问题
                        if (keyWord != maskItemBean.keyWord && checkExitKeyWord(keyWord)) {
                            ToastUtil.show("配置已存在！")
                            return@setOnClickListener
                        }
                        maskItemBean.keyWord = keyWord
                        maskItemBean.tipMode = ui.spinnerSelectedItem.first
                        when (maskItemBean.tipMode) {
                            Constrant.WX_MASK_TIP_MODE_ALERT -> GsonUtil.toJsonTree(MaskItemBean.AlertTipData(tipMess)).asJsonObject.let {
                                maskItemBean.tipData = it
                            }
                        }
                        listAdapter.notifyItemChanged(position)
                        ConfigUtil.setMaskList(listAdapter.getData())
                        dialog.dismiss()
                    }
                }

        }

        private fun checkExitKeyWord(keyWord: String): Boolean {
            return listAdapter.getData().indexOfFirst { it.keyWord == keyWord } > -1
        }

    }

    private class MaskItemDialogController(private val context: Context, private var tipMode: Int) {
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
        var etKeyWord: EditText = EditText(context).also {
            it.hint = "糊脸关键字"
        }
        var etTipMess = EditText(context).also {
            it.hint = "糊脸提示内容，如：${Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT}"
            it.setText(Constrant.WX_MASK_TIP_ALERT_MESS_DEFAULT)
            if (tipMode == Constrant.WX_MASK_TIP_MODE_SILENT) {
                it.visibility = View.GONE
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
            root.addView(
                etKeyWord,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            root.addView(spinner, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            root.addView(etTipMess, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

    }

//
//    class ConfigDialog(context: Context) : Dialog(context) {
//        private val controller by lazy { ConfigDialogController(context) }
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            window?.let {
//                it.decorView.setPadding(0)
//                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                it.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//            }
//
//            val contentView = controller.onCreateView()
//
//            MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT).also { lp ->
//                lp.marginStart = controller.dp24
//                lp.marginEnd = controller.dp24
//                setContentView(contentView, lp)
//            }
//
//        }
//
//        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//            return super.dispatchKeyEvent(event)
//        }
//
//
//    }

}