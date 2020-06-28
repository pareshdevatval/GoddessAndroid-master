package com.krystal.goddesslifestyle.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.custom_views.SafeClickListener
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@BindingAdapter("adapter")
fun setAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.adapter = adapter
}

/**
 * Sets an Image to an ImageView
 * @param view the ImageView on which to set the Image
 * @param url the url to get the image and set to the ImageView
 */
@BindingAdapter("imageUrl")
fun loadImageUrl(view: ImageView, url: String) {
    Glide.with(view.context).load(url).apply(RequestOptions()
            .error(R.mipmap.app_icon_round)
            .placeholder(R.mipmap.app_icon_round)
            .centerCrop())
            .into(view)
}

/**
 * Sets an Image to an ImageView
 * @param view the ImageView on which to set the Image
 * @param url the url to get the image and set to the ImageView
 * @param drawable the drawable image which you want to load as placeholder
 */
@BindingAdapter("imageUrl", "imageDefault")
fun loadImage(view: ImageView, url: String?, drawable: Drawable) {
    if (url != null && !url.isBlank()) {
        /*Glide.with(view.context).load(url).apply(RequestOptions()
                .error(drawable)
                .placeholder(drawable)
                .centerCrop())
                .into(view)*/
        view.post {
            AppUtils.loadImageThroughGlide(
                view.context, view,
                AppUtils.generateImageUrl(url, view.width, view.height),
                drawable
            )
        }


    }
}


/**
 * Sets an Image to an ImageView
 * @param view the ImageView on which to set the Image
 * @param url the url to get the image and set to the ImageView
 * @param drawable the resource id which you want to load as placeholder
 */
@BindingAdapter("imageUrl", "imageDefault")
fun loadImage(view: ImageView, url: String, resId: Int) {
    /*var drawable = ContextCompat.getDrawable(view.context, resId)
    Glide.with(view.context).load(url).apply(RequestOptions()
            .error(drawable)
            .placeholder(drawable)
            .centerCrop())
            .into(view)*/
    view.post {
        AppUtils.loadImageThroughGlide(
            view.context, view,
            AppUtils.generateImageUrl(url, view.width, view.height),
            resId
        )
    }
}

/*@BindingAdapter("isRequestedFlag", "status")
fun setStatus(view: TextView, isRequested: Boolean, status: Int) {
    if (!isRequested && status == RequestStatus.PENDING) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
        val pair = AppUtils.getRequestStatus(view.context, status)
        view.text = pair.first
        view.setTextColor(pair.second)
    }
}

@BindingAdapter("isRequested", "statusValue")
fun setGroupVisibility(view: View, isRequested: Boolean, status: Int) {
    if (!isRequested && status == RequestStatus.PENDING) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}*/

@BindingAdapter("date")
fun setDate(view: TextView, date: String) {
    if (date.isNotEmpty()) {
        view.text = AppUtils.formatDate(Date(date.toLong() * 1000), "dd MMM")
    }
}

/*@BindingAdapter("onSafeClick")
fun View.setOnSingleClickListener(safeClickListener: SafeClickListener?) {
    safeClickListener?.let {
        it.onClick()
        setOnClickListener(OnSingleClickListener(it))
    } ?: setOnClickListener(null)
}*/

@BindingAdapter("onSingleClick")
fun View.setOnSingleClickListener(clickListener: View.OnClickListener?) {
    clickListener?.also {
        setOnClickListener(OnSingleClickListener(it))
    } ?: setOnClickListener(null)
}

class OnSingleClickListener(
    private val clickListener: View.OnClickListener,
    private val intervalMs: Long = 1000
) : View.OnClickListener {
    private var canClick = AtomicBoolean(true)

    override fun onClick(v: View?) {
        if (canClick.getAndSet(false)) {
            v?.run {
                postDelayed({
                    canClick.set(true)
                }, intervalMs)
                clickListener.onClick(v)
            }
        }
    }
}
