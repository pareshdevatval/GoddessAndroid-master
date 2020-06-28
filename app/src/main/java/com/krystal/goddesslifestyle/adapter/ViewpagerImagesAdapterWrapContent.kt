package com.krystal.goddesslifestyle.adapter

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 24 February,2020.
 */
class ViewpagerImagesAdapterWrapContent(
    private val context: Context,
    private val imagesList: List<String>,
    private val showOverlay: Boolean = false
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
        val imageLayout = inflater.inflate(R.layout.image_adapter_item, view, false)!!

        val imageView = imageLayout
            .findViewById(R.id.imageView) as AppCompatImageView

        val overlay = imageLayout.findViewById<ImageView>(R.id.overlay)

        if(showOverlay) {
            overlay.visibility = View.VISIBLE
        } else {
            overlay.visibility = View.GONE
        }

        imageView.post {
            AppUtils.loadImageThroughGlide(
                context, imageView,
                AppUtils.generateImageUrl(imagesList[position], imageView.width, imageView.height),
                R.drawable.ic_placeholder_square
            )
        }
        view.addView(imageLayout, 0)

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