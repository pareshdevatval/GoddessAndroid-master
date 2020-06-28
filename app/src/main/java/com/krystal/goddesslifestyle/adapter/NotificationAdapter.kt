package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.data.response.GetNotificationListResponse
import com.krystal.goddesslifestyle.databinding.ItemFavoritesRecipesBinding
import com.krystal.goddesslifestyle.databinding.ItemNotificationBinding
import com.krystal.goddesslifestyle.databinding.ItemRowBreakfastBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding

/**
 * Created by imobdev on 23/3/20
 */
class NotificationAdapter : BaseBindingAdapter<GetNotificationListResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemNotificationBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemNotificationBinding
                val item=items[position]
                item?.let {
                    if(item.nStatus==2){
                        binding.tvDot.visibility= View.VISIBLE
                        binding.ivDelete.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context, R.drawable.ic_cross
                            )
                        )
                        binding.tvDot.setBackgroundResource(R.drawable.white_circle_second)
                        binding.tvNotification.setTextColor(ContextCompat.getColor(binding.root.context,R.color.white))
                        binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context,R.color.white))

                    }else{
                        binding.tvNotification.setTextColor(ContextCompat.getColor(binding.root.context,R.color.white_60))
                        binding.tvDot.setBackgroundResource(R.drawable.white_circle_light)
                        binding.tvDot.visibility= View.INVISIBLE
                        binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context,R.color.white_60))
                        binding.ivDelete.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context, R.drawable.ic_cross_light
                            )
                        )
                    }
                    binding.tvTime.text=item.nCreatedAt
                    binding.tvNotification.text=item.nMessage

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