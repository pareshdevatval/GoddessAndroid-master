package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.Benefit
import com.krystal.goddesslifestyle.databinding.ListRowBenifitsBinding

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class SubscriptionBenefitsAdapter : BaseBindingAdapter<Benefit>() {

    var isCollapsed = true

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ListRowBenifitsBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ListRowBenifitsBinding
        if(position > 1 && isCollapsed) {
            binding.root.visibility = View.GONE
        } else {
            binding.root.visibility = View.VISIBLE
        }
    }

    fun collapse() {
        isCollapsed = true
    }

    fun expand() {
        isCollapsed = false
    }
}