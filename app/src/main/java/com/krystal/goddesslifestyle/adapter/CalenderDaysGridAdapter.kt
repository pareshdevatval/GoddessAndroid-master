package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.databinding.CalenderDayGridRowBinding

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class CalenderDaysGridAdapter : BaseBindingAdapter<String>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return CalenderDayGridRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding<CalenderDayGridRowBinding>(holder.itemView)
        if(!items[position].isBlank()) {
            binding?.tvDay?.text = items[position]
        }
    }
}