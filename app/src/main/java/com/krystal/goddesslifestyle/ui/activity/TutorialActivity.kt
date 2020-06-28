package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.TutorialViewPagerAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.databinding.ActivityTutorialBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.fragment.TutorialVideoFragment
import com.krystal.goddesslifestyle.ui.fragment.TutorialWaveFragment
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.TutorialViewModel
import java.util.*
import javax.inject.Inject

class TutorialActivity : BaseActivity<TutorialViewModel>(), View.OnClickListener {


    private lateinit var mViewModel: TutorialViewModel
    private lateinit var binding: ActivityTutorialBinding
    private var mHandler: Handler? = null
    private lateinit var mRunnable: Runnable
    private var currentPage = 0

    @Inject
    lateinit var prefs: Prefs

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, TutorialActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectTutorialActivity(this)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding.activity = this

        binding.tvSkip.setOnClickListener(this)

        init()
    }

    override fun getViewModel(): TutorialViewModel {
        mViewModel = ViewModelProvider(this).get(TutorialViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun init() {
        setViewpager()
        //setupAutoPager(3)
    }


    private fun setViewpager() {
        val tutorialViewPagerAdapter = TutorialViewPagerAdapter(supportFragmentManager)

        val bundle1 = Bundle()
        bundle1.putInt(AppConstants.KEY_COLOR_CODE, R.color.color_bg_pink)
        bundle1.putInt(AppConstants.KEY_IMG_CODE, R.drawable.ic_tutorial_one)

        val bundle2 = Bundle()
        bundle2.putInt(AppConstants.KEY_COLOR_CODE, R.color.color_bg_green)
        bundle2.putInt(AppConstants.KEY_IMG_CODE, R.drawable.ic_tutorial_two)

        val bundle3 = Bundle()
        bundle3.putInt(AppConstants.KEY_COLOR_CODE, R.color.color_bg_purple)
        bundle3.putInt(AppConstants.KEY_IMG_CODE, R.drawable.ic_tutorial_three_new)


        val tutorialFragment4 = TutorialVideoFragment.newInstance()
        val tutorialFragment1 = TutorialWaveFragment.newInstance(bundle1)
        val tutorialFragment2 = TutorialWaveFragment.newInstance(bundle2)
        val tutorialFragment3 = TutorialWaveFragment.newInstance(bundle3)

        tutorialViewPagerAdapter.addFragment(tutorialFragment4)
        tutorialViewPagerAdapter.addFragment(tutorialFragment1)
        tutorialViewPagerAdapter.addFragment(tutorialFragment2)
        tutorialViewPagerAdapter.addFragment(tutorialFragment3)

        binding.viewPager.adapter = tutorialViewPagerAdapter

        binding.tabLayout.setupWithViewPager(binding.viewPager, true)
        binding.tabLayoutOne.setupWithViewPager(binding.viewPager, true)
        binding.viewPager.requestTransparentRegion(binding.viewPager)
        selectVideo()

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {

                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentPage = position
                when (position) {
                    0 -> {
                        selectVideo()
                    }

                    1 -> {
                        selectFirst()
                    }

                    2 -> {
                        selectFirst()
                    }
                    3 -> {
                        selectFirst()
                    }
                }
            }

        })
    }

    private fun setupAutoPager(size: Int) {
        val handler = Handler()

        val update = Runnable {
            binding.viewPager.setCurrentItem(currentPage, false)
            if (currentPage == size) {
                currentPage = 0
            } else {
                ++currentPage
            }

        }


        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 500, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHandler !== null) {
            mHandler!!.removeCallbacks(mRunnable)
        }
    }

    private fun selectVideo() {

        binding.clBottom.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        binding.tabLayoutOne.visibility = View.VISIBLE
    }

    private fun selectFirst() {
        binding.clBottom.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
        binding.tabLayoutOne.visibility = View.GONE
        binding.tvTutorialTitle.setTextColor(resources.getColor(R.color.color_bg_pink))
        binding.btnSignUp.setTextColor(resources.getColor(R.color.color_bg_pink))
        binding.btnSignUp.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_pink_border))
        binding.btnSignIn.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_pink_bg))
    }


    private fun selectSecond() {
        binding.clBottom.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
        binding.tvTutorialTitle.setTextColor(resources.getColor(R.color.color_bg_green))
        binding.btnSignUp.setTextColor(resources.getColor(R.color.color_bg_green))
        binding.btnSignUp.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_green_border))
        binding.btnSignIn.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_green_bg))
    }

    private fun selectThird() {
        binding.clBottom.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
        binding.tvTutorialTitle.setTextColor(resources.getColor(R.color.color_bg_purple))
        binding.btnSignUp.setTextColor(resources.getColor(R.color.color_bg_purple))
        binding.btnSignUp.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_purple_border))
        binding.btnSignIn.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_purple_bg))
    }

    fun loginBtnClick() {
        startActivity(LoginActivity.newInstance(this))
        AppUtils.startFromRightToLeft(this)
    }

    fun startSignUpActivity() {
        startActivity(SignUpActivity.newInstance(this))
        AppUtils.startFromRightToLeft(this)
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (v.id) {
                R.id.tvSkip -> {
                    //prefs.isSkipped = true
                    startActivity(LoginActivity.newInstance(this))
                    AppUtils.startFromRightToLeft(this)
                }
                else -> {

                }
            }
        }
    }
}
