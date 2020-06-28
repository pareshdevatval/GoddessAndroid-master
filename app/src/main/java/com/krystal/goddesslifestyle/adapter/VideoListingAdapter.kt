package com.krystal.goddesslifestyle.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.VideoCategoriesResponse
import com.krystal.goddesslifestyle.databinding.ListRowVideoListingBinding
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class VideoListingAdapter : BaseBindingAdapter<VideoCategoriesResponse.VideoCategory>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ListRowVideoListingBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ListRowVideoListingBinding
        val videoCategory = items[position]
        binding.tvName.text = videoCategory.title

        binding.imageView.post {
            Log.e("VID_CATG_URL", AppUtils.generateImageUrl(videoCategory.image, binding.imageView.width, 0))
            AppUtils.loadImageThroughGlide(binding.root.context, binding.imageView,
                AppUtils.generateImageUrl(videoCategory.image, binding.imageView.width, 0),
                R.drawable.ic_placeholder_square)
        }
        /*AppUtils.loadImageThroughGlide(binding.root.context, binding.imageView,
            AppUtils.generateImageUrl(videoCategory.image),
            R.drawable.ic_placeholder_square)*/

    }
}