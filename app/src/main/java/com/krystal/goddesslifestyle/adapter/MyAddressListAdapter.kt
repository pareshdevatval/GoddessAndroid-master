package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.MyAddressListResponse
import com.krystal.goddesslifestyle.databinding.ItemMyAddressListBinding
import com.krystal.goddesslifestyle.databinding.ItemOrderListBinding
import com.krystal.goddesslifestyle.databinding.LayoutImagesAndTextviewThreeBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding

/**
 * Created by imobdev on 23/3/20
 */
class MyAddressListAdapter : BaseBindingAdapter<MyAddressListResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemMyAddressListBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemMyAddressListBinding
                val item = items[position]
                item?.let {
                        binding.tvAddressTitle.text=item.uaAddressTitle?.capitalize()
                        binding.tvAreaName.text=item.uaHouseNo+" "+item.uaRoadAreaColony+" "+item.uaCity+","+item.uaState+","+item.uaCountry+ " - "+item.uaPinCode
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