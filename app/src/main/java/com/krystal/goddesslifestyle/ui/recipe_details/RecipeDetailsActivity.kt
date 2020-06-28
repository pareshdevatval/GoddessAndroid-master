package com.krystal.goddesslifestyle.ui.recipe_details

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewPagerAdapterForTabs
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.RecipeDetailsResponse
import com.krystal.goddesslifestyle.databinding.ActivityRecipeDetailsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.RecipeDetailsViewModel
import com.yalantis.colormatchtabs.colormatchtabs.adapter.ColorTabAdapter
import com.yalantis.colormatchtabs.colormatchtabs.listeners.ColorTabLayoutOnPageChangeListener
import javax.inject.Inject

class RecipeDetailsActivity : BaseActivity<RecipeDetailsViewModel>() {
    private lateinit var mViewModel: RecipeDetailsViewModel
    private lateinit var binding: ActivityRecipeDetailsBinding
    /*variable for tab to select when an activity starts*/
    private var tabIndexToSelect: Int = 0
    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService
    var recipeDetailsResponse: RecipeDetailsResponse? = null

    companion object {
        fun newInstance(context: Context, recipeName: String, recipeID: String): Intent {
            val intent = Intent(context, RecipeDetailsActivity::class.java)
            intent.putExtra(AppConstants.RECIPE_NAME, recipeName)
            intent.putExtra(AppConstants.RECIPE_ID, recipeID)
            return intent
        }
    }

    override fun getViewModel(): RecipeDetailsViewModel {
        mViewModel = ViewModelProvider(this).get(RecipeDetailsViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {
    }

    private val recipeName: String by lazy {
        intent.getStringExtra(AppConstants.RECIPE_NAME)
    }

    private val recipeID: String by lazy {
        intent.getStringExtra(AppConstants.RECIPE_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectRecipeDetailsActivity(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_details)
        super.onCreate(savedInstanceState)
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.callGetRecipeDetailsAPI(recipeID)
        setObserver()


    }

    private fun init() {

        decorateToolbar(recipeName, R.color.green)
        setUpViewPager()
        //binding.tabs.setupWithViewPager(binding.viewpager)
        customizeTab(tabIndexToSelect)
        newTabLayout()
    }


    private fun setObserver() {
        mViewModel.getRecipeDetailsResponse()
            .observe({ this.lifecycle }, { response: RecipeDetailsResponse? ->
                recipeDetailsResponse = response
                Log.e("RESPONSE", "" + recipeDetailsResponse!!.status)
                if (recipeDetailsResponse!!.status) {
                    init()
                }
            })
    }

    // setting up viewpager
    private fun setUpViewPager() {
        // adapter
        val adapter = ViewPagerAdapterForTabs(supportFragmentManager)
        //adapter.addFragment(DetailsFragment.newInstance(), getString(R.string.recipe))
        adapter.addFragment(IngredientsFragment.newInstance(), getString(R.string.recipe))
        adapter.addFragment(HowToCookFragment.newInstance(), getString(R.string.recipe))
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

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                // customize tab again based on selected tab
                //customizeTab(position, true)
            }
        })

        binding.viewpager.addOnPageChangeListener(ColorTabLayoutOnPageChangeListener(binding.tabLayout))
    }

    /*Customizing the tab with custom views*/
    private fun customizeTab(selectedPosition: Int, forUpdate: Boolean = false) {
        // setting the first tab custom view
        /*binding.tabs.getTabAt(0)?.customView = getCustomTabViewAt(0, selectedPosition, forUpdate)
        // setting the second tab custom view
        binding.tabs.getTabAt(1)?.customView = getCustomTabViewAt(1, selectedPosition, forUpdate)
        // setting the third tab custom view
        binding.tabs.getTabAt(2)?.customView = getCustomTabViewAt(2, selectedPosition, forUpdate)*/
    }

    /*getting custom tab view based on tab is selected or not*/
    private fun getCustomTabViewAt(tabIndex: Int, selectedPosition: Int, forUpdate: Boolean): View {
        // if the method is called when tab changes, then we will use the tabView as from tab.getView
        // or if method called from first time then we will inflate the custom view
        /*val tabView: View = if (forUpdate) {
            binding.tabs.getTabAt(tabIndex)?.customView!!
        } else {
            LayoutInflater.from(this).inflate(R.layout.custom_tab, null)
        }
        val llContainer = tabView.findViewById<LinearLayout>(R.id.llContainer)
        val ivTab = tabView.findViewById<ImageView>(R.id.ivTab)
        val tvTab = tabView.findViewById<TextView>(R.id.tvTab)

        //If this tab is selected, then we will show a view based on that
        //an icon with a lable and backgeound based on tab index
        if (selectedPosition == tabIndex) {
            when (tabIndex) {
                0 -> {
                    llContainer.setBackgroundResource(R.drawable.right_side_rounded_bg)
                    ivTab.setImageResource(R.drawable.ic_recipe_info_gree_tab)
                    tvTab.text = getString(R.string.details)
                    tvTab.setTextColor(ContextCompat.getColor(this, R.color.green))
                }
                1 -> {
                    llContainer.setBackgroundResource(R.drawable.both_side_rounded_bg)
                    ivTab.setImageResource(R.drawable.ic_ingredients_green_tab)
                    tvTab.text = getString(R.string.ingredients)
                    tvTab.setTextColor(ContextCompat.getColor(this, R.color.green))
                }
                2 -> {
                    llContainer.setBackgroundResource(R.drawable.left_side_rounded_bg)
                    ivTab.setImageResource(R.drawable.ic_cook_green_tab)
                    tvTab.text = getString(R.string.how_to_cook)
                    tvTab.setTextColor(ContextCompat.getColor(this, R.color.green))
                }
            }
            tvTab.visibility = View.VISIBLE
        } else {
            // tab is not selected, then we will show only icon on transparent bg
            llContainer.setBackgroundResource(0)
            when (tabIndex) {
                0 -> {
                    ivTab.setImageResource(R.drawable.ic_recipe_info_white_tab)
                }
                1 -> {
                    ivTab.setImageResource(R.drawable.ic_ingredients_white_tab)
                }
                2 -> {
                    ivTab.setImageResource(R.drawable.ic_cook_white_tab)
                }
            }
            tvTab.visibility = View.GONE
        }

        val layout = (binding.tabs.getChildAt(0) as LinearLayout).getChildAt(tabIndex)
        val layoutParams: LinearLayout.LayoutParams =
            layout.layoutParams as LinearLayout.LayoutParams
        // if tab is selected then we will assign weight 2 to it otherwise assign weight 1
        if (selectedPosition == tabIndex) {
            layoutParams.weight = 2f
        } else {
            layoutParams.weight = 1f
        }
        layout.layoutParams = layoutParams

        return tabView*/
        return View(this)
    }

    /*A common method change the bg and text of the toolbar based on the tab selected*/
    private fun decorateToolbar(title: String, bgColor: Int) {
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

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_share_white,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    //AppUtils.showSnackBar(binding.dummyView, "Share")
                    if(recipeDetailsResponse != null && recipeDetailsResponse!!.result.recipe_images.isNotEmpty()) {
                        val image = recipeDetailsResponse!!.result.recipe_images[0].recipe_image_url
                        AppUtils.shareContent(this@RecipeDetailsActivity, AppUtils.generateImageUrl(image))
                    } else {
                        AppUtils.shareContent(this@RecipeDetailsActivity, "Share from Goddess LifeStyle Android App")
                    }
                }
            })
    }

    private fun newTabLayout() {
        /*binding.tabLayout.addTab(
            ColorTabAdapter.createColorTab(binding.tabLayout,
                getString(R.string.details), ContextCompat.getColor(this, R.color.white),
                resources.getDrawable(R.drawable.ic_recipe_info_white_tab)))*/
        binding.tabLayout.addTab(
            ColorTabAdapter.createColorTab(binding.tabLayout,
                getString(R.string.ingredients), ContextCompat.getColor(this, R.color.white),
                resources.getDrawable(R.drawable.ic_ingredients_white_tab)))
        binding.tabLayout.addTab(
            ColorTabAdapter.createColorTab(binding.tabLayout,
                getString(R.string.how_to_cook), ContextCompat.getColor(this, R.color.white),
                resources.getDrawable(R.drawable.ic_cook_white_tab)))


        binding.tabLayout.selectedTabWidth = AppUtils.getScreenWidth(this)/2

        binding.tabLayout.getTabAt(0)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 0
        }
        binding.tabLayout.getTabAt(1)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 1
        }
        /*binding.tabLayout.getTabAt(2)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 2
        }*/

        binding.tabLayout.selectedTabIndex = tabIndexToSelect
    }
}
