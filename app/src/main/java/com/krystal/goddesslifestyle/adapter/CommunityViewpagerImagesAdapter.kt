package com.krystal.goddesslifestyle.adapter

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.data.response.MediaData
import com.krystal.goddesslifestyle.ui.activity.FullScreenImageActivity
import com.krystal.goddesslifestyle.ui.community.PlayVideoActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 24 February,2020.
 */
class CommunityViewpagerImagesAdapter(
    private val context: Context,
    private val imagesList: List<MediaData>
) : PagerAdapter() {

    private val inflater: LayoutInflater


    init {
        inflater = LayoutInflater.from(context)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imagesList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.opinion_image_adapter_item, view, false)!!

        val imageView = imageLayout.findViewById(R.id.imageView) as AppCompatImageView
        val imageViewPlay = imageLayout.findViewById(R.id.ivPlay) as AppCompatImageView

        imageView.post {
            if (imagesList[position].gcom_media!!.endsWith(".mp4")) {
                imageViewPlay.visibility = View.VISIBLE
                Glide.with(context).load(imagesList[position].gcom_media).apply(
                    RequestOptions()
                        .error(R.drawable.ic_placeholder_square)
                        .placeholder(R.drawable.ic_placeholder_square)
                )
                    .into(imageView)
            } else {
                imageViewPlay.visibility = View.GONE
                Log.e(
                    "IMAGE",
                    "" + AppUtils.generateImageUrl(
                        imagesList[position].gcom_media,
                        imageView.width,
                        imageView.height,
                        AppConstants.SCALE_TYPE_CROP
                    )
                )
                AppUtils.loadImageThroughGlide(
                    context, imageView,
                    AppUtils.generateImageUrl(
                        imagesList[position].gcom_media,
                        imageView.width,
                        imageView.height,
                        AppConstants.SCALE_TYPE_CROP
                    ),
                    R.drawable.ic_placeholder_square
                )
            }
        }
        view.addView(imageLayout, 0)

        imageView.setOnClickListener {
            if (!imagesList[position].gcom_media!!.endsWith(".mp4")) {
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putParcelableArrayListExtra(
                    AppConstants.IMAGE_LIST,
                    imagesList as ArrayList<MediaData>
                )
                intent.putExtra(AppConstants.Images_URL, imagesList[position].gcom_media)
                context.startActivity(intent)
            }
        }
        imageViewPlay.setOnClickListener {
            context.startActivity(
                PlayVideoActivity.newInstance(
                    context,
                    imagesList[position].gcom_media!!,
                    true
                )
            )
        }

        return imageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }

}