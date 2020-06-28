package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.HowToEarnModel
import com.krystal.goddesslifestyle.databinding.ItemHowToEarnBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding

/**
 * Created by imobdev on 23/3/20
 */
class HowToEarnAdapter : BaseBindingAdapter<HowToEarnModel?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemHowToEarnBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemHowToEarnBinding
                val item = items[position]
                item?.let {
                    binding.tvDesc.text=item.desc
                    binding.tvPoint.text="+"+item.point
                    binding.iv.setBackgroundResource(item.images)
                    binding.iv.setImageDrawable(ContextCompat.getDrawable(binding.root.context, item.images))

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

    private fun getItem(position: Int): HowToEarnModel? {
        return items[position]
    }
}