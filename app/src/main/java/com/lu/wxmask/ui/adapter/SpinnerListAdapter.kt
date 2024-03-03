package com.lu.wxmask.ui.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.util.ext.dp

class SpinnerListAdapter(spinnerDataList: ArrayList<Pair<Int, String>>) :
    CommonListAdapter<Pair<Int, String>, AbsListAdapter.ViewHolder>() {
    init {
        setData(spinnerDataList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TextView(parent.context).also { it.setPadding(4.dp) })
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int, parent: ViewGroup) {
        val itemView = vh.itemView
        if (itemView is TextView) {
            itemView.text = getItem(position)?.second
        }
    }
}
