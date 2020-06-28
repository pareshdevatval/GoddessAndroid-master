package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.ProductModel
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.databinding.*
import com.krystal.goddesslifestyle.utils.AppUtils
import java.text.SimpleDateFormat

/**
 * Created by imobdev on 23/3/20
 */
class OrderDetailsAdapter : BaseBindingAdapter<ProductModel?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemOrderDetailsBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemOrderDetailsBinding
                val item = items[position]
                item?.let {
                    AppUtils.loadImages(binding.root.context,item.images,binding.ivItem, R.drawable.ic_placeholder_square)
                    binding.tvItemName.text=item.name
                    binding.tvPrice.text=item.totalPrice
                    binding.tvItemCount.text="$"+item.price+" x "+item.qty
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