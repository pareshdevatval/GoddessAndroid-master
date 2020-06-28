package com.krystal.goddesslifestyle.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.Video
import com.krystal.goddesslifestyle.databinding.ListRowVideosBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class VideosAdapter : BaseBindingAdapter<Video>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ListRowVideosBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ListRowVideosBinding
        binding.ivPlay.visibility = View.VISIBLE
        binding.blackOverlay.visibility = View.VISIBLE

        val video = items[position]
        binding.tvName.text = video.vlTitle

        /*AppUtils.loadImageThroughGlide(binding.root.context, binding.imageView,
            AppUtils.generateImageUrl(video.vlImage),
            R.drawable.ic_placeholder_square)*/

        binding.imageView.post {
            Log.e("VIDEO_URL", AppUtils.generateImageUrl(video.vlImage, binding.imageView.width, binding.imageView.height,
                AppConstants.SCALE_TYPE_CONTAIN))
            AppUtils.loadImageThroughGlide(binding.root.context, binding.imageView,
                AppUtils.generateImageUrl(video.vlImage, binding.imageView.width, binding.imageView.height),
                R.drawable.ic_placeholder_rect)
        }
    }
}