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
import com.krystal.goddesslifestyle.databinding.*

/**
 * Created by imobdev on 23/3/20
 */
class FAQAdapter : BaseBindingAdapter<Boolean?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemFaqBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemFaqBinding
                val item=items[position]
                val context = binding.tvQuestion.context
                item?.let {
                    binding.root.setOnClickListener {
                        if (binding.tvAnswer.visibility == View.GONE) {
                            binding.tvAnswer.visibility = View.VISIBLE
                            binding.ivArrow.setImageDrawable(
                                ContextCompat.getDrawable(
                                    binding.root.context, R.drawable.ic_faq_up_arrrow
                                )
                            )
                        } else {
                            binding.tvAnswer.visibility = View.GONE
                            binding.ivArrow.setImageDrawable(
                                ContextCompat.getDrawable(
                                    binding.root.context, R.drawable.ic_faq_down_arrrow
                                )
                            )
                        }
                    }

                    binding.tvQuestion.setOnClickListener { binding.root.performClick() }
                    binding.tvAnswer.setOnClickListener { binding.root.performClick() }
                    binding.ivArrow.setOnClickListener { binding.root.performClick() }

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