package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.LeaderBoradModel
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.data.response.GetNotificationListResponse
import com.krystal.goddesslifestyle.databinding.*
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by imobdev on 23/3/20
 */
class LeaderBordAdapter : BaseBindingAdapter<LeaderBoradModel?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ListRowLeaderBordBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ListRowLeaderBordBinding
                val item=items[position]
                item?.let {
                    AppUtils.loadImages(binding.root.context, item.image, binding.iv, R.drawable.ic_placeholder_square)
                    binding.tvName.text=item.name
                    binding.tvPoints.text=item.point
                    binding.tvNumber.text=item.number
                    AppUtils.setImages(item.point.toInt(),binding.tvCrownGoddess,binding.root.context)
                }
            }
            LOADING -> {

            }
        }

    }

    override
    fun getItemViewType(position: Int): Int {
        return if (position == items.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    fun removeLoadingFooter(isLast: Boolean = false) {
        isLoadingAdded = false
    }

    private fun getItem(position: Int): LeaderBoradModel? {
        return items[position]
    }
}