package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.databinding.ItemRowBreakfastBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding

/**
 * Created by imobdev on 23/3/20
 */
class BreakfastListAdapter : BaseBindingAdapter<BreakfastData?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemRowBreakfastBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemRowBreakfastBinding
                val item = items[position]
                item?.let {
                    binding.recipeData=item
                    if (item.recipe_is_liked==1){
                        binding.ivBreakfastFav.setImageResource(R.drawable.ic_favourite_selected)
                    }else{
                        binding.ivBreakfastFav.setImageResource(R.drawable.ic_favourite_unselected)
                    }

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