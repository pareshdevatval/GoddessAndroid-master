package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.CommentData
import com.krystal.goddesslifestyle.databinding.ItemCommunityCommentsBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils


class CommunityOpinionCommentAdapter : BaseBindingAdapter<CommentData?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        //return ItemCommunityCommentsBinding.inflate(inflater, parent, false)
        when (viewType) {
            ITEM ->
                viewHolder = ItemCommunityCommentsBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemCommunityCommentsBinding
                val item = items[position]
                if(item != null) {
                    binding.commnetData = item
                    binding.tvTime.text = AppUtils.countDays(item.gcoc_created_at)

                    if (item.added_by.u_image.isEmpty()) {
                        holder.setIsRecyclable(true)
                        binding.ivUser.tag = R.drawable.ic_placeholder_square
                        binding.ivUser.setImageDrawable(binding.root.context.resources.getDrawable(R.drawable.ic_placeholder_square))
                    } else {
                        binding.ivUser.tag = ""
                        AppUtils.loadImageThroughGlide(
                            binding.root.context, binding.ivUser,
                            AppUtils.generateImageUrl(
                                item.added_by.u_image, binding.ivUser.width, binding.ivUser.height,
                                AppConstants.SCALE_TYPE_CONTAIN
                            ),
                            R.drawable.ic_placeholder_square
                        )
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

    private fun getItem(position: Int): CommentData? {
        return items[position]
    }
}