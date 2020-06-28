package com.krystal.goddesslifestyle.ui.of_the_month

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewPagerAdapterForTabs
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.OfTheMonth
import com.krystal.goddesslifestyle.data.response.OfTheMonthResponse
import com.krystal.goddesslifestyle.databinding.ActivityOfTheMonthBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.OfTheMonthViewModel
import com.yalantis.colormatchtabs.colormatchtabs.adapter.ColorTabAdapter
import com.yalantis.colormatchtabs.colormatchtabs.listeners.ColorTabLayoutOnPageChangeListener
import javax.inject.Inject

class OfTheMonthActivity : BaseActivity<OfTheMonthViewModel>() {

    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context, tabIndex: Int): Intent {
            val intent = Intent(context, OfTheMonthActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_TAB_INDEX, tabIndex)
            return intent
        }
    }

    private lateinit var viewModel: OfTheMonthViewModel
    private lateinit var binding: ActivityOfTheMonthBinding

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    /*variable for tab to select when an activity starts*/
    var tabIndexToSelect: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectOfTheMonthActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_of_the_month)
        super.onCreate(savedInstanceState)

        // getting value of tab to select from intent
        tabIndexToSelect = intent?.getIntExtra(AppConstants.EXTRA_TAB_INDEX, 0) ?: 0

        viewModel.setInjectable(apiService, prefs)

        viewModel.getOfTheMonthResponse().observe(this, ofTheMonthResponseObserver)

        viewModel.callOfTheMonthApi()
    }

    private fun init() {
        // setting toolbar according to the selected tab
        setUpToolbar(tabIndexToSelect)
        // setting up viewPager
        setUpViewPager()
        customizeTabLayout()
    }

    // setting up viewpager
    private fun setUpViewPager() {
        // adapter
        val adapter = ViewPagerAdapterForTabs(supportFragmentManager)
        adapter.addFragment(GoddessOfTheMonthFragment.newInstance(), getString(R.string.goddess))
        adapter.addFragment(RecipeOfTheMonthFragment.newInstance(), getString(R.string.recipe))
        adapter.addFragment(TeacherOfTheMonthFragment.newInstance(), getString(R.string.teacher))
        binding.viewpager.adapter = adapter

        // selecting the default item based on the tab index received in intent
        binding.viewpager.currentItem = tabIndexToSelect

        /*If we would have written the code of toolbar and tab in individual fragments
        * then it will not behave as per our requirments.
        * Becasue, defualt offScreenPageLimit for a viewpager is 0. So next fragment
        * is loaded by default and that fragment overrides the toolbar behaviour for that fragment
        * and hence ambiguity occurs. Thats why we have written a toolbar decoration code
        * here in activity in viewpager onPageSelected method*/
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                // setup toolbar again for selected tab
                setUpToolbar(position)
                // customize tab again based on selected tab
                //customizeTab(position, true)
            }
        })

        binding.viewpager.addOnPageChangeListener(ColorTabLayoutOnPageChangeListener(binding.tabLayout))
    }


    /*setting toolbar changes from the activity itself instead of fragments
    * becuase it includes changing in the background of activity and dummy view also*/
    private fun setUpToolbar(position: Int) {
        when (position) {
            0 -> {
                decorateToolbar(R.string.goddess_of_the_month, R.color.violet)
            }
            1 -> {
                decorateToolbar(R.string.recipe_of_the_month, R.color.green)
            }
            2 -> {
                decorateToolbar(R.string.teacher_of_the_month, R.color.yellow)
            }
        }
        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : BaseActivity.ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(R.drawable.ic_share_white, object : BaseActivity.ToolbarRightImageClickListener {
            override fun onRightImageClicked() {
                //AppUtils.showSnackBar(binding.dummyView, "Share")
                val vpIndex = binding.viewpager.currentItem
                var shareMessage : String? = null
                when(vpIndex) {
                    0 -> {
                        shareMessage = AppUtils.generateImageUrl(ofTheMonthData?.goddesses?.goddessImage)
                    }
                    1 -> {
                        val recipeImages = ofTheMonthData?.recipes?.recipeImages
                        val firstImageObject = recipeImages?.get(0)
                        val image = firstImageObject?.image
                        if(!image.isNullOrBlank()) {
                            shareMessage = AppUtils.generateImageUrl(image)
                        } else {
                            shareMessage = ofTheMonthData?.recipes?.recipeDescription
                        }
                    }
                    2 -> {
                        shareMessage = AppUtils.generateImageUrl(ofTheMonthData?.teachers?.teacherImage)
                    }
                }
                if(shareMessage != null) {
                    AppUtils.shareContent(this@OfTheMonthActivity, shareMessage)
                }
            }
        })


    }

    /*A common method change the bg and text of the toolbar based on the tab selected*/
    private fun decorateToolbar(title: Int, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color
        setToolbarColor(bgColor)
        // bgcolor of dummyView below tab layout
        binding.dummyView.setBackgroundColor(ContextCompat.getColor(this, bgColor))
        // tabs background color
        binding.tabLayout.setBackgroundColor(ContextCompat.getColor(this, bgColor))
        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, bgColor)
        }
    }

    override fun getViewModel(): OfTheMonthViewModel {
        viewModel = ViewModelProvider(this).get(OfTheMonthViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    var ofTheMonthData : OfTheMonth? = null

    private val ofTheMonthResponseObserver = Observer<OfTheMonthResponse> { response ->
        ofTheMonthData = response.result
        init()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }

    /*[START] TODO Change the following to revert back customization of tablayout*/
    // Uncomment the followinn method call
    // customizeTab(tabIndexToSelect) from all occurances

    // uncomment getCustomTabViewAt method code
    // uncomment customizeTab method code
    // uncommnet binding.tabs.setBackgroundColor( inside decorToolbar
    /*[END] TODO Change the following to revert back customization of tablayout*/

    /*Method to customize the original TabLayout.Tabs based on our app design*/
    private fun customizeTabLayout() {
        binding.tabLayout.addTab(ColorTabAdapter.createColorTab(binding.tabLayout,
            "Goddess", ContextCompat.getColor(this, R.color.white),
            resources.getDrawable(R.drawable.ic_goddess_tab_white)))
        binding.tabLayout.addTab(ColorTabAdapter.createColorTab(binding.tabLayout,
            "Recipe", ContextCompat.getColor(this, R.color.white),
            resources.getDrawable(R.drawable.ic_recipe_tab_white)))
        binding.tabLayout.addTab(ColorTabAdapter.createColorTab(binding.tabLayout,
            "Teacher", ContextCompat.getColor(this, R.color.white),
            resources.getDrawable(R.drawable.ic_teacher_tab_white)))

        binding.tabLayout.selectedTabWidth = AppUtils.getScreenWidth(this)/2

        binding.tabLayout.getTabAt(0)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 0
        }
        binding.tabLayout.getTabAt(1)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 1
        }
        binding.tabLayout.getTabAt(2)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 2
        }

        binding.tabLayout.selectedTabIndex = tabIndexToSelect
    }
}
