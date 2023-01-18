package com.lu.wxmask.adapter

import java.util.concurrent.CopyOnWriteArrayList


abstract class CommonListAdapter<E, VH : AbsListAdapter.ViewHolder> : AbsListAdapter<VH>() {

    open val dataList: MutableList<E> = CopyOnWriteArrayList()

    open fun setData(data: List<E>): CommonListAdapter<E, VH> {
        this.dataList.clear()
        this.dataList.addAll(data)
        return this
    }

    open fun updateDataAt(data: List<E>) {
        this.dataList.clear()
        this.dataList.addAll(data)
        notifyDataSetChanged()
    }

    open fun updateDataAt(ele: E) {
        val index = dataList.indexOf(ele)
        updateDataAt(index)
    }

    open fun updateDataAt(position: Int) {
        if (position < 0 || position >= dataList.size) {
            return
        }
        notifyDataSetChanged()
    }

    open fun addData(data: List<E>): CommonListAdapter<E, VH> {
        this.dataList.addAll(data)
        return this
    }

    open fun addData(vararg ele: E): CommonListAdapter<E, VH> {
        this.dataList.addAll(ele)
        return this
    }

    open fun remove(data: List<E>): CommonListAdapter<E, VH> {
        this.dataList.removeAll(data)
        return this
    }

    open fun remove(vararg elements: E): CommonListAdapter<E, VH> {
        this.dataList.removeAll(elements.toSet())
        return this
    }

    open fun removeAt(index: Int): CommonListAdapter<E, VH> {
        this.dataList.removeAt(index)
        return this
    }

    open fun getData(): MutableList<E> {
        return dataList
    }

    override fun getItem(position: Int): E? {
        return dataList[position]
    }


    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}
