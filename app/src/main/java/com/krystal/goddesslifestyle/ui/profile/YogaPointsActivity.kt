package com.krystal.goddesslifestyle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewPagerAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityYogaPointBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.LeaderBoardActivity
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.profile.yoga_point_fragment.HowToEarnFragment
import com.krystal.goddesslifestyle.ui.profile.yoga_point_fragment.YourPointFragment
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.YogaPointsModel
import javax.inject.Inject

class YogaPointsActivity : BaseActivity<YogaPointsModel>(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (it.id) {
                R.id.tvGoddessShop->{
                    startActivity(MainActivity.newInstance(this, getString(R.string.title_shopping), false))
                    GoddessAnimations.finishFromLeftToRight(this)
                }
            }
        }
    }

    private lateinit var binding: ActivityYogaPointBinding
    private lateinit var mViewModel: YogaPointsModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase
    private val progressValue:Int by lazy {
        intent.getIntExtra(AppConstants.PROGRESS_VALUE,0)
    }
    private val currentLevelPoint:Int by lazy {
        intent.getIntExtra(AppConstants.CURRENT_LEVEL_POINT,0)
    }
    companion object {
        fun newInstance(context: Context, progressValue: Int,currentlevelpoint:Int=0): Intent {
            val intent = Intent(context, YogaPointsActivity::class.java)
            intent.putExtra(AppConstants.PROGRESS_VALUE,progressValue)
            intent.putExtra(AppConstants.CURRENT_LEVEL_POINT,currentlevelpoint)
            return intent
        }
    }

    override fun getViewModel(): YogaPointsModel {
        mViewModel = ViewModelProvider(this).get(YogaPointsModel::class.java)
        return mViewModel

    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectYogaPointsActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_yoga_point)

        init()
    }

    fun init() {
        val content = SpannableString(binding.tvGoddessShop.text.toString())
        content.setSpan(UnderlineSpan(), 0, binding.tvGoddessShop.text.toString().length, 0)
        binding.tvGoddessShop.setOnClickListener(this)
        binding.tvGoddessShop.text = content
        setToolBar(prefs.userDataModel?.result?.uUserName+" "+getString(R.string.lbl_your_points), R.color.pink)
        setViewPagers()
        setHeaderData()
        AppUtils.setName(this,currentLevelPoint,binding.tvMaiden)
    }

    private fun setHeaderData() {
        binding.tvPray.text=""+prefs.userDataModel?.result?.uPoints
        binding.progressBar.progress = progressValue
    }

    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color
        setToolbarColor(bgColor)

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_trophy,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    startActivity(LeaderBoardActivity.newInstance(this@YogaPointsActivity))
                }
            })
    }

    private fun setViewPagers() {
        /*[start] set viewpager adepter*/
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(
            HowToEarnFragment(),
            getString(R.string.lbl_how_to_earn)
        )
        adapter.addFragment(
            YourPointFragment(),
            getString(R.string.lbl_your_points)
        )
        var potion=0
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        val firstTab = (binding.tabLayout.getTabAt(0) as TabLayout.Tab).view
        firstTab.setBackgroundResource(R.drawable.tab_background)

        val secondTab = (binding.tabLayout.getTabAt(1) as TabLayout.Tab).view
        secondTab.setBackgroundResource(R.drawable.your_points_bg)

        binding.tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                 potion= tab?.position!!
                if(potion==0){

                    val firstTab = (binding.tabLayout.getTabAt(0) as TabLayout.Tab).view
                    firstTab.setBackgroundResource(R.drawable.tab_background)

                    val secondTab = (binding.tabLayout.getTabAt(1) as TabLayout.Tab).view
                    secondTab.setBackgroundResource(R.drawable.your_points_bg)

                }else{
                    val firstTab = (binding.tabLayout.getTabAt(0) as TabLayout.Tab).view
                    firstTab.setBackgroundResource(R.drawable.how_to_earn_bg)

                    val secondTab = (binding.tabLayout.getTabAt(1) as TabLayout.Tab).view
                    secondTab.setBackgroundResource(R.drawable.tab_background)
                }
                Log.e("SELECTED_POTION","--->"+potion)
            }
        })
        /*[END] set viewpager adepter*/
    }
}
