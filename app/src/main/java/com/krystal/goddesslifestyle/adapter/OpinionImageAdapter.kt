package com.krystal.goddesslifestyle.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.model.ImageData
import com.krystal.goddesslifestyle.data.response.CommentData
import com.krystal.goddesslifestyle.databinding.ItemCommunityCommentsBinding
import com.krystal.goddesslifestyle.databinding.ItemCommunityImageBinding
import com.krystal.goddesslifestyle.utils.AppUtils


class OpinionImageAdapter(val opinionImageClick: OpinionImageListener) : BaseBindingAdapter<ImageData>() {
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemCommunityImageBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ItemCommunityImageBinding
        val item = items[position]
        Log.e("IMAGE",item.imageName)
        if (item.imageId.isEmpty()){
            Glide.with(binding.root.context).load(item.imageName).apply(
                RequestOptions()
                    .error(R.drawable.ic_placeholder_square)
                    .placeholder(R.drawable.ic_placeholder_square))
                .into(binding.ivOpinion)
        }else{
            binding.ivOpinion.post {
                AppUtils.loadImageThroughGlide(
                    binding.root.context, binding.ivOpinion,
                    AppUtils.generateImageUrl(item.imageName, binding.ivOpinion.width, binding.ivOpinion.height),
                    R.drawable.ic_placeholder_square
                )
            }
        }


        binding.ivDelete.setOnClickListener {
            opinionImageClick.onImageRemove(item,position)
        }
    }


    interface OpinionImageListener{
        fun onImageRemove(data:ImageData,position: Int)
    }
}