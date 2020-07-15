package com.krystal.goddesslifestyle.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.databinding.ActivityZoomImageBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

class ZoomImageActivity : BaseActivity<BaseViewModel>() {

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1.0f

    private lateinit var binding: ActivityZoomImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_zoom_image)
        scaleGestureDetector = ScaleGestureDetector(this, scaleListener)

        val mediaList = intent!!.getStringExtra(AppConstants.ZOOM_IMAGE_URL)
        /*Glide.with(this)
            .load(mediaList)
            .placeholder(R.drawable.ic_placeholder_square)
            .into(binding.imageView)*/

        binding.imageView.post {
            this.let {
                Log.e("JOURNAL_IMAGE", AppUtils.generateImageUrl(mediaList, binding.imageView.width,
                    binding.imageView.height))
                AppUtils.loadImageThroughGlide(it, binding.imageView,
                    AppUtils.generateImageUrl(mediaList, binding.imageView.width, binding.imageView.height),
                    R.drawable.ic_placeholder_square)
            }
        }

        binding.ivClose.setOnClickListener{
            finish()
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        scaleGestureDetector!!.onTouchEvent(motionEvent)
        return true
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10f))
            binding.imageView.scaleX = mScaleFactor
            binding.imageView.scaleY = mScaleFactor
            return true
        }
    }

    override fun getViewModel() = BaseViewModel(GoddessLifeStyleApp())

    override fun internetErrorRetryClicked() {}
}