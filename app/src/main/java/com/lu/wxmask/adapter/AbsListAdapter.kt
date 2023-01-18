package com.lu.wxmask.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


abstract class AbsListAdapter : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vh = if (convertView == null) {
            onCreateViewHolder(parent, getItemViewType(position)).also {
                it.itemView.tag = it
            }
        } else {
            convertView.tag as ViewHolder
        }
        vh.layoutPosition = position
        onBindViewHolder(vh, position, parent)
        return vh.itemView
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    abstract fun onBindViewHolder(vh: ViewHolder, position: Int, parent: ViewGroup)

    open class ViewHolder(var itemView: View){
        var layoutPosition = 0
    }

}
