package com.krystal.goddesslifestyle.adapter

import android.content.Context
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 24 February,2020.
 */
class ViewpagerImagesAdapter(
    private val context: Context,
    private val imagesList: List<String>
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
        val imageLayout = inflater.inflate(R.layout.today_meal_pager_image, view, false)!!

        val imageView = imageLayout
            .findViewById(R.id.imageView) as AppCompatImageView



        view.addView(imageLayout, 0)

        Handler().postDelayed(Runnable {
            imageView.post {
                Log.e("RECEIPE_", AppUtils.generateImageUrl(imagesList[position], imageView.width, imageView.height))
                AppUtils.loadImageThroughGlide(context, imageView,
                    AppUtils.generateImageUrl(imagesList[position], imageView.width, imageView.height),
                    R.drawable.ic_placeholder_square)
            }
        }, 500)

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