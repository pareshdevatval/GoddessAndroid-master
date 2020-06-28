package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.data.response.FavouritesResponse
import com.krystal.goddesslifestyle.databinding.ItemFavoritesRecipesBinding
import com.krystal.goddesslifestyle.databinding.ItemRowBreakfastBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by imobdev on 23/3/20
 */
class FavouritesRecipesAdapter : BaseBindingAdapter<FavouritesResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemFavoritesRecipesBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemFavoritesRecipesBinding
                    val item=items[position]
                        binding.data=item
                item?.let {
                    if (item.recipeIsLiked==1){
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

    fun filter(string: String) {
        items = ArrayList(allItems.filter { it!!.recipeTitle.toLowerCase().contains(string.toLowerCase()) })
        notifyDataSetChanged()
    }
}