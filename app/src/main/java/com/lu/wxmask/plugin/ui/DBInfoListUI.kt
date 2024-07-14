package com.lu.wxmask.plugin.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Space
import android.widget.TextView
import androidx.core.view.setPadding
import com.lu.magic.util.CursorUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.kxt.toElseEmptyString
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.bean.DBItem
import com.lu.wxmask.plugin.ui.view.BottomPopUI
import com.lu.wxmask.util.ClipboardUtil
import com.lu.wxmask.util.ConfigUtil
import com.lu.wxmask.util.WxSQLiteManager
import com.lu.wxmask.util.ext.dp
import com.lu.wxmask.util.ext.toJson
import java.io.File

class DBInfoListUI(val context: Context) : IConfigManagerUI {
    private lateinit var listView: ListView
    private lateinit var listAdapter: DBListAdapter
    private val popwindow: BottomPopUI

    init {
        popwindow = BottomPopUI(onCreateView())
        popwindow.needScrollChild = listView
    }

    override fun onCreateView(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 480.dp)
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
            addView(initDBListView())
        }
    }

    override fun dismiss() {
        popwindow.dismiss()
    }

    override fun show() {
        popwindow.show()
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
                text = "数据库信息"
            })

        }
    }

    private fun initDBListView(): ListView {
        listAdapter = DBListAdapter()
        return ListView(context).apply {
            listView = this
            isVerticalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT).apply {
                topMargin = 16.dp
            }
            divider = null
            selector = ColorDrawable(Color.TRANSPARENT)
            adapter = listAdapter
        }

    }

    private inner class DBListAdapter() : CommonListAdapter<DBItem, Holder>() {
        init {
            setData(WxSQLiteManager.Store.values.toList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val itemView = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }
            return Holder(itemView)
        }

        override fun onBindViewHolder(vh: Holder, position: Int, parent: ViewGroup) {
            val item = dataList[position]
            vh.textLeft.text = item.name

            val dbFile = File(item.name)
            val rightText = item.password.toElseEmptyString() + if (dbFile.exists()) {
                "\n${dbFile.length() / 1024L}KB"
            } else {
                ""
            }
            vh.textRight.text = rightText
            vh.itemView.setOnClickListener {
                val text = "数据库：${item.name}\n密码：${item.password}"
                if (ClipboardUtil.copy(text)) {
                    ToastUtil.show("数据库路径和密码已复制")
                    ToastUtil.show(WxSQLiteManager.getAllTables(item.name, item.password).toJson())

                }
            }
        }

    }

    private class Holder(itemView: ViewGroup) : AbsListAdapter.ViewHolder(itemView) {
        val textLeft: TextView
        val textRight: TextView

        init {
            itemView.addView(LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(TextView(context).apply {
                    textLeft = this
                    textSize = 16f
                    gravity = Gravity.LEFT
                    layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT).apply {
                        weight = 1f
                    }

                })
                addView(TextView(context).apply {
                    textRight = this
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {

                    }
                })
            })
            itemView.addView(Space(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 8.dp)
            })

        }
    }

}