package com.krystal.goddesslifestyle.ui.main_activity

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityMainBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.community.GoddessCommunityOpinionsFragment
import com.krystal.goddesslifestyle.ui.community.GoddessCommunityFragment
import com.krystal.goddesslifestyle.ui.recipe.RecipesFragment
import com.krystal.goddesslifestyle.ui.shop.ShopFragment
import com.krystal.goddesslifestyle.ui.video_library.VideoListingFragment
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.MainActivityViewModel
import javax.inject.Inject


class MainActivity : BaseActivity<BaseViewModel>() {

    companion object {
        /*@Accepts
        * context -> context to start the activity
        * tabName -> tab to select when activity starts
        * clearAllActivities -> clear the backstack or not
        * returns an Intent to start MainActivity*/
        fun newInstance(
            context: Context,
            tabName: String = "Home",
            clearAllActivities: Boolean = false
        ): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_TAB_NAME, tabName)
            if (clearAllActivities) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return intent
        }
    }

    /*ViewModel*/
    private lateinit var vModel: MainActivityViewModel

    /*binding variable*/
    private lateinit var binding: ActivityMainBinding

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    /*a variable to detect click time, to prevent rapid clicks*/
    var mLastClickTime: Long = 0L

    /*variable for tabToSelect*/
    var tabToSelect = "Home"

    override fun getViewModel(): BaseViewModel {
        vModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectMainActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        super.onCreate(savedInstanceState)

        intent?.let {
            tabToSelect = it.getStringExtra(AppConstants.EXTRA_TAB_NAME)!!
        }

        val user = prefs.userDataModel
        /*val userSubscription = appDatabase.userSubscriptionDao().getUserSubscriptionData(user!!.result!!.uId!!)
        if(userSubscription != null && userSubscription.isNotEmpty()) {
            val subscription = userSubscription[0]
            AppUtils.showToast(this, "" + subscription.usSubscriptionPlanId)
        }*/

        init()
    }

    private fun init() {
        vModel.setInjectable(apiService, prefs)
        Log.e("TOKEN_BEARER", "-->" + prefs.userDataModel?.result?.token + "-->")
        Log.e("USER_DETAILS", "-->" + prefs.userDataModel?.result)
        setUpBottomMenuView()

        /*select a tab based on the name received from an intent*/
        for (i in 0 until binding.navigation.menu.size()) {
            val menuItem = binding.navigation.menu.getItem(i)
            if (menuItem.title.toString().equals(tabToSelect, true)) {
                binding.navigation.selectedItemId = menuItem.itemId
                break
            }
        }
    }

    /*Set background of the activity*/
    private fun setActivityAnimatedBackground(color: Int) {
        binding.mainLayout.setBackgroundColor(color)
    }

    /*To get the current background color of an activity*/
    fun getActivityBgColor(): Int {
        var color = Color.TRANSPARENT
        val bg = binding.mainLayout.background
        if (bg is ColorDrawable) {
            color = (bg as ColorDrawable).color
        }
        return color
    }

    // change color with an animation
    fun changeBgColor(colorToSet: Int) {
        val fromColor = getActivityBgColor()
        val toColor = ContextCompat.getColor(this, colorToSet)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
        colorAnimation.duration = 250

        colorAnimation.addUpdateListener {
            setActivityAnimatedBackground(it.animatedValue as Int)
        }

        colorAnimation.start()
    }

    private fun initBottomMenusSize() {
        val menuView = binding.navigation.getChildAt(0) as BottomNavigationMenuView
        for (i in 0 until 5) {
            /*[START] code to bigger center icon*/

            /* We need to show the play icon larger and hence increading its height programatically
            Our play icon is 3rd menu icon and hence the position is 2*/
            val iconView =
                menuView.getChildAt(i).findViewById<View>(com.google.android.material.R.id.icon)
            val layoutParams = iconView.layoutParams
            val displayMetrics = resources.displayMetrics
            // set your height here
            if (i == 2) {
                layoutParams.height =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics)
                        .toInt()
                // set your width here
                layoutParams.width =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics)
                        .toInt()
            } else {
                layoutParams.height =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, displayMetrics)
                        .toInt()
                // set your width here
                layoutParams.width =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, displayMetrics)
                        .toInt()
            }
            iconView.layoutParams = layoutParams
            /*[END] code to bigger center icon*/
        }
    }

    /*method to increase the height of a selected tab icon*/
    private fun setSelectedIconBig(position: Int) {
        val menuView = binding.navigation.getChildAt(0) as BottomNavigationMenuView
        /*[START] code to bigger center icon*/

        /* We need to show the play icon larger and hence increading its height programatically
        Our play icon is 3rd menu icon and hence the position is 2*/
        val iconView =
            menuView.getChildAt(position).findViewById<View>(com.google.android.material.R.id.icon)
        val layoutParams = iconView.layoutParams
        val displayMetrics = resources.displayMetrics
        // set your height here
        layoutParams.height =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f, displayMetrics).toInt()
        // set your width here
        layoutParams.width =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f, displayMetrics).toInt()
        iconView.layoutParams = layoutParams
        /*[END] code to bigger center icon*/
    }

    /*We have used a default google bottom navigationView
    * and customize it based on our requirements.
    * This method customize the default menu to our requirements*/
    private fun setUpBottomMenuView() {
        // disabling item icon tint color
        binding.navigation.itemIconTintList = null

        /*[START] code to bigger center icon*/
        /*val menuView = binding.navigation.getChildAt(0) as BottomNavigationMenuView

        //We need to show the play icon larger and hence increading its height programatically
        //Our play icon is 3rd menu icon and hence the position is 2
        val iconView = menuView.getChildAt(2).findViewById<View>(com.google.android.material.R.id.icon)
        val layoutParams = iconView.layoutParams
        val displayMetrics = resources.displayMetrics
        // set your height here
        layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics).toInt()
        // set your width here
        layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics).toInt()
        iconView.layoutParams = layoutParams*/
        initBottomMenusSize()
        setSelectedIconBig(0)
        /*[END] code to bigger center icon*/

        /*[START] navigationview menu item click listener*/
        binding.navigation.setOnNavigationItemSelectedListener {
            /*[START] A code to prevent rapid clicks*/
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnNavigationItemSelectedListener false
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            /*[END] A code to prevent rapid clicks*/

            when (it.itemId) {
                R.id.navigation_home -> {
                    /*This will small down all the icons*/
                    initBottomMenusSize()
                    // This will increase the height of the selected tab icon
                    setSelectedIconBig(0)
                    // start the fragment if it is currently not displayed
                    if (getCurrentFragment() != null) {
                        if (getCurrentFragment() !is HomeFragment) {
                            replaceFragment(R.id.frame_container, HomeFragment.newInstance(), false)
                        }
                    } else {
                        replaceFragment(R.id.frame_container, HomeFragment.newInstance(), false)
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_recipe -> {
                    /*This will small down all the icons*/
                    initBottomMenusSize()
                    // This will increase the height of the selected tab icon
                    setSelectedIconBig(1)
                    // start the fragment if it is currently not displayed
                    if (getCurrentFragment() != null) {
                        if (getCurrentFragment() !is RecipesFragment) {
                            replaceFragment(
                                R.id.frame_container,
                                RecipesFragment.newInstance(),
                                false
                            )
                        }
                    } else {
                        replaceFragment(R.id.frame_container, RecipesFragment.newInstance(), false)
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_play -> {
                    /*This will small down all the icons*/
                    initBottomMenusSize()
                    // start the fragment if it is currently not displayed
                    if (getCurrentFragment() != null) {
                        if (getCurrentFragment() !is VideoListingFragment) {
                            replaceFragment(
                                R.id.frame_container,
                                VideoListingFragment.newInstance(),
                                false
                            )
                        }
                    } else {
                        replaceFragment(
                            R.id.frame_container,
                            VideoListingFragment.newInstance(),
                            false
                        )
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_chat -> {
                    /*This will small down all the icons*/
                    initBottomMenusSize()
                    // This will increase the height of the selected tab icon
                    setSelectedIconBig(3)
                    // start the fragment if it is currently not displayed
                    if (getCurrentFragment() != null) {
                        if (getCurrentFragment() !is GoddessCommunityOpinionsFragment) {
                            replaceFragment(
                                R.id.frame_container,
                                GoddessCommunityFragment.newInstance(),
                                false
                            )
                        }
                    } else {
                        replaceFragment(
                            R.id.frame_container,
                            GoddessCommunityFragment.newInstance(),
                            false
                        )
                    }
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_shopping -> {
                    /*This will small down all the icons*/
                    initBottomMenusSize()
                    // This will increase the height of the selected tab icon
                    setSelectedIconBig(4)
                    // start the fragment if it is currently not displayed
                    if (getCurrentFragment() != null) {
                        if (getCurrentFragment() !is ShopFragment) {
                            replaceFragment(R.id.frame_container, ShopFragment.newInstance(), false)
                        }
                    } else {
                        replaceFragment(R.id.frame_container, ShopFragment.newInstance(), false)
                    }
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false

        }
        /*[END] navigationview menu item click listener*/
    }

    fun getBottomNavigation(): View {
        return binding.navigation
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        fragment!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        if (getCurrentFragment() != null) {
            if (getCurrentFragment() is GoddessCommunityOpinionsFragment) {
                replaceFragment(R.id.frame_container, GoddessCommunityFragment.newInstance(), false)
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}
