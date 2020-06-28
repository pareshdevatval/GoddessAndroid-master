package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.RStep
import com.krystal.goddesslifestyle.databinding.ItemRowStepBinding

class RecipeStepAdapter : BaseBindingAdapter<RStep>() {
    var tempPosition=0
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemRowStepBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ItemRowStepBinding
        val item = items[position]
        binding.step = item

        binding.tvStepTitle.setBackgroundColor(binding.root.context.resources.getIntArray(R.array.colorArry)[tempPosition])
        if (tempPosition==3){
            tempPosition=0
        }else{
            tempPosition+=1
        }
    }
}