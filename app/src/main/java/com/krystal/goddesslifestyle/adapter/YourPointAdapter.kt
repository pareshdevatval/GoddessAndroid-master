package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.HowToEarnModel
import com.krystal.goddesslifestyle.data.response.YourPointsResponse
import com.krystal.goddesslifestyle.databinding.ItemHowToEarnBinding
import com.krystal.goddesslifestyle.databinding.ItemYourPointBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by imobdev on 23/3/20
 */
class YourPointAdapter : BaseBindingAdapter<YourPointsResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemYourPointBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemYourPointBinding
                val item = items[position]
                item?.let {

                    binding.tvDesc.text=AppUtils.getYourPointText(item.uaType)
                    binding.tvPoint.text="+"+item.uaEarnedPoints
                    binding.iv.setImageDrawable(ContextCompat.getDrawable(binding.root.context,
                        AppUtils.getYourPointImages(item.uaType)))
                    binding.tvDate.text=item.uaCreatedAt

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

}