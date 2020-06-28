package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.CalenderDay
import com.krystal.goddesslifestyle.databinding.CalenderDateGridRowBinding

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class CalenderDatesGridAdapter : BaseBindingAdapter<CalenderDay>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return CalenderDateGridRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding<CalenderDateGridRowBinding>(holder.itemView)
        val calenderDay = items[position]

        if(calenderDay.date != 0) {
            binding?.tvDate?.text = calenderDay.date.toString()
            binding?.tvDesc?.text = calenderDay.desc
            binding?.root?.visibility = View.VISIBLE
        } else {
            binding?.root?.visibility = View.GONE
        }
    }
}