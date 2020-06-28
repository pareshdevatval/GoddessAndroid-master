package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.github.vipulasri.timelineview.TimelineView
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.TrackData
import com.krystal.goddesslifestyle.databinding.ItemOderStatusBinding

/**
 * Created by imobdev on 23/3/20
 */
class OrderStatusAdapter : BaseBindingAdapter<TrackData?>() {

    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemOderStatusBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        val binding = holder.binding as ItemOderStatusBinding
        val item = items[position]
        binding.statusData=item
        binding.timeline.initLine( holder.itemViewType)
        item?.let {
            if (item.isActive){
                binding.timeline.lineStyle=TimelineView.LineStyle.NORMAL
            }else{
                binding.timeline.lineStyle=TimelineView.LineStyle.DASHED
            }
        }
    }


    override
    fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }
    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    fun removeLoadingFooter(isLast: Boolean = false) {
        isLoadingAdded = false
    }

}