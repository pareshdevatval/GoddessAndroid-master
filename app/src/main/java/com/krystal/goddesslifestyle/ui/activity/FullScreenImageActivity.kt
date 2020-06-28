package com.krystal.goddesslifestyle.ui.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewPager2Adapter
import com.krystal.goddesslifestyle.adapter.ViewPagerAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.MediaData
import com.krystal.goddesslifestyle.databinding.ActivityFullScreenImageBinding
import com.krystal.goddesslifestyle.ui.fragment.FullScreenImageFragment
import com.krystal.goddesslifestyle.utils.AppConstants

class FullScreenImageActivity : BaseActivity<BaseViewModel>(), View.OnClickListener {

    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_image)

        initImageListAdapter()

        binding.ivNext.setOnClickListener(this)
        binding.ivPrevious.setOnClickListener(this)
    }

    private fun initImageListAdapter() {

        val mediaList = intent!!.getParcelableArrayListExtra<MediaData>(AppConstants.IMAGE_LIST)
        val imageList = ArrayList<String>()

        for (media in mediaList!!) {
            if (!media.gcom_media!!.endsWith(".mp4")) {
                imageList.add(media.gcom_media!!)
            }
        }
        val clickedImage = intent.getStringExtra(AppConstants.Images_URL)

        val fragmentList = ArrayList<Fragment>()
        var position = 0
        for (image in imageList) {
            val bundle = Bundle()
            bundle.putString(AppConstants.Images_URL, image)
            val fullScreenImageFragment = FullScreenImageFragment.newInstance(bundle)
            fragmentList.add(fullScreenImageFragment)

            if (clickedImage == image) {
                position = imageList.indexOf(image)
            }
        }
        binding.vpImageList.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        val fullScreenFragmentListAdapter = ViewPager2Adapter(this, fragmentList)
        binding.vpImageList.adapter = fullScreenFragmentListAdapter
        binding.vpImageList.currentItem = position

        binding.vpImageList.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    binding.ivPrevious.visibility = View.INVISIBLE
                } else {
                    binding.ivPrevious.visibility = View.VISIBLE
                }

                if (position == imageList.size - 1) {
                    binding.ivNext.visibility = View.INVISIBLE
                } else {
                    binding.ivNext.visibility = View.VISIBLE
                }
            }
        })

    }

    override fun getViewModel() = BaseViewModel(GoddessLifeStyleApp())

    override fun internetErrorRetryClicked() {

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivPrevious -> {
                binding.vpImageList.currentItem = binding.vpImageList.currentItem - 1
            }

            R.id.ivNext -> {
                binding.vpImageList.currentItem = binding.vpImageList.currentItem + 1
            }
        }
    }

}