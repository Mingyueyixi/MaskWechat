package com.lu.wxmask.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


abstract class AbsListAdapter<VH: AbsListAdapter.ViewHolder> : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vh = if (convertView == null) {
            onCreateViewHolder(parent, getItemViewType(position)).also {
                it.itemView.tag = it
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            convertView.tag as VH
        }
        vh.layoutPosition = position
        onBindViewHolder(vh, position, parent)
        return vh.itemView
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onBindViewHolder(vh: VH, position: Int, parent: ViewGroup)

    open class ViewHolder(var itemView: View){
        var layoutPosition = 0
    }

}
