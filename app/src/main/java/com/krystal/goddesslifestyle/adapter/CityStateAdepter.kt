package com.krystal.goddesslifestyle.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.CityStateModel
import com.krystal.goddesslifestyle.databinding.ItemCityStateBinding


class CityStateAdepter : BaseBindingAdapter<CityStateModel?>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemCityStateBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: ItemCityStateBinding = holder.binding as ItemCityStateBinding
        val item = items[position]
        binding.cityStateData = item
        item?.let {
            binding.tvState.text = item.state + ", " + item.country
        }
    }
}