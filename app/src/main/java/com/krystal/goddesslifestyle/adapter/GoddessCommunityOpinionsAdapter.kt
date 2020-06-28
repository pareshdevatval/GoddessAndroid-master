package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.data.response.CommunityOpinionResult
import com.krystal.goddesslifestyle.databinding.ItemCommunityOpinionsBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils


class GoddessCommunityOpinionsAdapter : BaseBindingAdapter<CommunityOpinionResult?>() {

    private var isLoadingAdded = false
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        //return ItemCommunityOpinionsBinding.inflate(inflater, parent, false)
        when (viewType) {
            ITEM ->
                viewHolder = ItemCommunityOpinionsBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemCommunityOpinionsBinding
                val item = items[position]
                if(item != null) {
                    binding.opiniounData = item
                    binding.tvCommunityTime.text = AppUtils.countDays(item.gco_created_at)
                    if (item.media.isNotEmpty()) {
                        binding.ivCommunity.visibility = View.VISIBLE
                        binding.vpIndicator.visibility = View.VISIBLE
                        val adapter = CommunityViewpagerImagesAdapter(binding.root.context, item.media)
                        binding.ivCommunity.adapter = adapter
                        binding.vpIndicator.setViewPager(binding.ivCommunity)
                    } else {
                        binding.ivCommunity.visibility = View.GONE
                        binding.vpIndicator.visibility = View.GONE
                    }

                    if (item.added_by != null && item.added_by.u_image.isEmpty()) {
                        holder.setIsRecyclable(true)
                        binding.ivUser.tag = R.drawable.ic_placeholder_square
                        //binding.ivUser.setImageDrawable(binding.root.context.resources.getDrawable(R.drawable.ic_placeholder_square))
                        binding.ivUser.setImageResource(R.drawable.ic_placeholder_square)
                    } else {
                        if (item.gco_show_identity == 1) {
                            binding.ivUser.tag = ""
                            if (item.added_by != null) {
                                AppUtils.loadImageThroughGlide(
                                    binding.root.context, binding.ivUser,
                                    AppUtils.generateImageUrl(
                                        item.added_by.u_image, binding.ivUser.width, binding.ivUser.height,
                                        AppConstants.SCALE_TYPE_CONTAIN
                                    ),
                                    R.drawable.ic_placeholder_square
                                )
                            } else {
                                //binding.ivUser.setImageDrawable(binding.root.context.resources.getDrawable(R.drawable.ic_placeholder_square))
                                binding.ivUser.setImageResource(R.drawable.ic_placeholder_square)
                            }
                        } else {
                            holder.setIsRecyclable(true)
                            binding.ivUser.tag = R.drawable.ic_placeholder_square
                            //binding.ivUser.setImageDrawable(binding.root.context.resources.getDrawable(R.drawable.ic_placeholder_square))
                            binding.ivUser.setImageResource(R.drawable.ic_placeholder_square)
                        }
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

    private fun getItem(position: Int): CommunityOpinionResult? {
        return items[position]
    }
}