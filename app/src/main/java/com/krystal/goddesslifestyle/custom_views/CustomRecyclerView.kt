package com.krystal.goddesslifestyle.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

//Created by imobdev-rujul on 22/1/19
class  CustomRecyclerView : RecyclerView {

    var emptyView: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, styleId: Int) : super(context, attrs, styleId)

    private val emptyObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            checkData()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkData()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkData()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            checkData()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            checkData()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            checkData()
        }
    }

    private fun checkData() {
        val adapter = adapter
        if (emptyView != null) {
            /*if (adapter is PagedListAdapter<*, *>) {
                setEmptyView(adapter.currentList?.size)
            } else {
                setEmptyView(adapter?.itemCount)
            }*/
            setEmptyView(adapter?.itemCount)
        }
    }

    private fun setEmptyView(size: Int?) {
        if (size == 0) {
            emptyView?.visibility = View.VISIBLE
            this@CustomRecyclerView.visibility = View.GONE
        } else {
            emptyView?.visibility = View.GONE
            this@CustomRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

}