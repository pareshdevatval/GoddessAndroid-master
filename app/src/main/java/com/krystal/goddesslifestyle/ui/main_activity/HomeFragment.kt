package com.krystal.goddesslifestyle.ui.main_activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.MyViewPagerAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.custom_views.GoddessCalenderView
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.Theme
import com.krystal.goddesslifestyle.databinding.FragmentHomeBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.ui.activity.NotificationActivity
import com.krystal.goddesslifestyle.ui.of_the_month.OfTheMonthActivity
import com.krystal.goddesslifestyle.ui.profile.MyProfileActivity
import com.krystal.goddesslifestyle.utils.*
import com.krystal.goddesslifestyle.viewmodels.HomeViewModel
import javax.inject.Inject

/**
 * Created by Bhargav Thanki on 20 February,2020.
 */
class HomeFragment : BaseFragment<HomeViewModel>(), GoddessCalenderView.CalenderEventListener,
    View.OnClickListener {


    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): HomeFragment {
            val bundle = Bundle()
            val fragment = HomeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var adapter: MyViewPagerAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    var token = ""

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    // instance variables of all the viewpager frgaments
    lateinit var practiceFragment: TodaysPracticePagerFagment
    lateinit var mealFragment: TodaysMealPagerFragment
    lateinit var journalFragment: TodaysJournalPagerFagment

    /*calender theme image complete url strig
    * complete url will be needed to share the link*/
    var calenderThemeImage: String? = null

    // only path of the calender image
    var calenderThemeImagePathOnly: String? = null

    /*Represents the Actual Physical screen size in inches
    * this will needed to devide whethear we need to show animation or not on collapsing the calender*/
    var screenSizeInInches: Double = 0.0


    override fun getViewModel(): HomeViewModel {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectHomeFragment(this)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.fragment = this

        context?.let {
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                (AppUtils.getScreenHeight(it) * 0.4).toInt()
            )

            /*Set the height of the Goddess image to the 40% of the height of a screen*/
            binding.ivGoddess.layoutParams = layoutParams
            /*set the height of a reference guideline also to the 40% of the screen*/
            val lp = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
            lp.guideBegin = (AppUtils.getScreenHeight(it) * 0.4).toInt()
            binding.guideline.layoutParams = lp
        }
        //setTopImageHeight(0.4)

        viewModel.setAppDatabase(appDatabase)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //AppUtils.showToast(context!!, prefs.userDataModel!!.result!!.uId.toString())
        //Log.e("ACCESS_TOKEN", ""+prefs.userDataModel!!.result!!.token!!)

        /*setting activity background color based on current screen design*/
        (activity as MainActivity).changeBgColor(R.color.pink)
        /*initializing toolbar based on this screen*/
        //initToolbar()
        /*initializing current month calender*/
        //initViewPager()

        viewModel.setInjectable(apiService, prefs)

        // Observables for LiveData
        viewModel.observeIfCalenderDataStored().observe(viewLifecycleOwner, calenderDataObserver)
        viewModel.getShareApiResponse().observe(viewLifecycleOwner, shareApiResponseObserver)
        viewModel.geTokenResponse().observe(viewLifecycleOwner, tokenResponseObserver)

        viewModel.configLoadingVisibility.observe(
            viewLifecycleOwner,
            Observer { loadingVisibility ->
                loadingVisibility?.let { if (loadingVisibility) showConfigProgress() else hideProgress() }
            })

        /*If data for the calender is already available, then show from the database
        * Otherwise call an API & store the data in database*/


        if (viewModel.isDataLocallyAvailable()) {
            Handler().postDelayed(Runnable { showCalenderData() }, 500)
        } else {
            viewModel.getCalenderDataFromApi()
        }

        activity?.let {
            screenSizeInInches = AppUtilsJava.getPhysicalScreenSize(it)
        }
        // update the token in pref
        tokenUpdate()
    }

    private val calenderDataObserver = Observer<Boolean> {
        if (it) {
            //mViewModel.onApiFinish()
            //hideProgress()
            viewModel.onConfigEnd()
            showCalenderData()
        }
    }

    private val shareApiResponseObserver = Observer<BaseResponse> {
        if (it.status) {
            //AppUtils.showToast(context!!, it.message)
        }
    }
    private val tokenResponseObserver = Observer<BaseResponse> {
        if (it.status) {
            prefs.firebaseToken = token
        }
    }

    private fun showCalenderData() {
        /*initializing toolbar based on this screen*/
        initToolbar()
        /*initializing current month calender*/
        initViewPager()
    }

    private fun initToolbar() {
        initTopBarFromTheme()
        // toolbar color
        setToolbarColor(R.color.pink_60)

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_user, object : BaseActivity.ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                /*If user is logged in then open My Profile screen
                * otherwise open Login screen*/
                context?.let {
                    if (prefs.isLoggedIn) {
                        startActivity(MyProfileActivity.newInstance(context!!))
                    } else {
                        val i = Intent(context, LoginActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        it.startActivity(i)
                        AppUtils.startFromRightToLeft(context!!)
                    }
                }
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_notification,
            object : BaseActivity.ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    // open notification screen based on the Login
                    context?.let {
                        if (prefs.isLoggedIn) {
                            startActivity(NotificationActivity.newInstance(context!!))
                        } else {
                            val i = Intent(context, LoginActivity::class.java)
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            it.startActivity(i)
                            AppUtils.startFromRightToLeft(context!!)
                        }
                        //      startActivity(FAQActivity.newInstance(context!!))
                    }
                }
            })

        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context!!, R.color.colorAccent)
        }
        //binding.practiceLayout.clipToOutline = true
    }


    private fun initTopBarFromTheme() {
        // get current month theme from database and set data
        if(viewModel.getThisMonthTheme() !=null) {
            binding.shareLayout.visibility = View.VISIBLE
            viewModel.getThisMonthTheme()?.let { theme ->
                // toolbar title
                setToolbarTitle(theme.themeTitle)

                calenderThemeImage = AppUtils.generateImageUrl(theme.themeImage)
                calenderThemeImagePathOnly = theme.themeImage

                /*binding.ivGoddess.post {
                context?.let {
                    AppUtils.loadImageThroughGlide(
                        it, binding.ivGoddess,
                        AppUtils.generateImageUrl(theme.themeImage, binding.ivGoddess.width, binding.ivGoddess.height),
                        R.drawable.ic_placeholder_rect
                    )
                }
            }*/
                loadCalenderThemeImage()
                // initializing calender form calender days from db
                initCalender(theme)
            }
        }else{
            binding.shareLayout.visibility = View.GONE
        }
    }

    // initialize the calender
    private fun initCalender(theme: Theme) {
        // setting current month name
        binding.tvMonthName.text = viewModel.getCurrentMonthName()
        // setting up an actual calender

        context?.let {
            Handler().postDelayed(Runnable {
                binding.goddessCalenderView.initCalender(
                    viewModel.getCurrentMonthCalender(theme),
                    binding.dummyView.height
                )
            }, 200)
        }
        binding.goddessCalenderView.listener = this
    }

    /*A viewpager code*/
    private fun initViewPager() {
        /*[START] Customization in Viewpager for showing some part of views of prev and next items*/
        binding.viewPager.clipToPadding = false
        val padding = AppUtilsJava.getPixelsFromDp(context!!, 45f).toInt()
        binding.viewPager.setPadding(padding, 0, padding, 0)

        val pageMargin = AppUtilsJava.getPixelsFromDp(context!!, 20f).toInt()

        binding.viewPager.pageMargin = pageMargin
        binding.viewPager.offscreenPageLimit = 2
        /*[END] Customization in Viewpager for showing some part of views of prev and next items*/

        /*viewpager adapter*/
        adapter = MyViewPagerAdapter(childFragmentManager)

        /*fragments to show viewpager*/
        practiceFragment = TodaysPracticePagerFagment.newInstance()
        mealFragment = TodaysMealPagerFragment.newInstance()
        journalFragment = TodaysJournalPagerFagment.newInstance()

        adapter.addFragment(practiceFragment)
        adapter.addFragment(mealFragment)
        adapter.addFragment(journalFragment)

        binding.viewPager.adapter = adapter
    }

    /*Called when particular date is selected from calender*/
    override fun onDateSelected(selectedDate: Int) {
        // reduce the height of top bar when date selects, to show the viewpager effectively
        setTopImageHeight(0.25)
        // getting calender day id for the selected date
        val calenderDayId = appDatabase.calenderDataDao().getCalenderDayId(selectedDate)

        // setting calende day to all the viewpager fragments, so they can show the data of current date
        practiceFragment.setCalenderDay(calenderDayId)
        mealFragment.setCalenderDay(calenderDayId)
        journalFragment.setCalenderDay(calenderDayId)

        /*showing details of practice, meal and journal in viewpager*/
        binding.viewPager.currentItem = 0
        binding.viewPager.visibility = View.VISIBLE
        /*hiding the share layout*/
        binding.shareLayout.visibility = View.GONE
        /*animation to show a viewpager from right to left*/
        GoddessAnimations.animateViewFromRightToLeft(
            context,
            binding.viewPager,
            object : GoddessAnimations.GoddessAnimationListener {
                override fun onAnimationStart() {
                    // Animation start event
                }

                override fun onAnimationEnd() {
                    // Animation end event
                }
            })
    }


    /*Called when a calender expand again from WEEK_MODE to MONTH_MODE*/
    override fun onExpanded() {
        GoddessAnimations.animateViewFromLeftToRight(context, binding.viewPager,
            object : GoddessAnimations.GoddessAnimationListener {
                override fun onAnimationStart() {
                    // animation started
                }

                override fun onAnimationEnd() {
                    binding.viewPager.visibility = View.GONE
                    binding.shareLayout.visibility = View.VISIBLE
                    // Again set the height of top bar to 40% of the screen
                    setTopImageHeight(0.4)
                }
            })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnGoddess -> {
                startActivity(OfTheMonthActivity.newInstance(v.context, 0))
                GoddessAnimations.startFromRightToLeft(context!!)
            }
        }
        when (v?.id) {
            R.id.btnRecipe -> {
                startActivity(OfTheMonthActivity.newInstance(v.context, 1))
                GoddessAnimations.startFromRightToLeft(context!!)
            }
        }
        when (v?.id) {
            R.id.btnTeacher -> {
                startActivity(OfTheMonthActivity.newInstance(v.context, 2))
                GoddessAnimations.startFromRightToLeft(context!!)
            }
        }
        when (v?.id) {
            R.id.shareLayout -> {
                context?.let {
                    /*if (prefs.isLoggedIn) {
                        if (!calenderThemeImage.isNullOrBlank()) {
                            AppUtils.shareContent(it, calenderThemeImage!!)
                        } else {
                            AppUtils.shareContent(it, "Share from Goddess LifeStyle Android App")
                        }
                        callShareApi()
                    } else {
                        AppUtils.startSubscriptionActivity(it)
                    }*/
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                            //AppUtils.startSubscriptionActivity(context)
                            AppUtils.startSubscriptionActivity(context, true)
                        } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                            if (!calenderThemeImage.isNullOrBlank()) {
                                AppUtils.shareContent(it, calenderThemeImage!!)
                            } else {
                                AppUtils.shareContent(
                                    it,
                                    "Share from Goddess LifeStyle Android App"
                                )
                            }
                            callShareApi()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (prefs.syncDate!!.isNotEmpty()) {
            viewModel.callSyncCalenderApi()
        }
    }

    private fun callShareApi() {
        val params = HashMap<String, String>()
        params[ApiContants.ACTIVITY_ID] = viewModel.getThemeId().toString()
        params[ApiContants.EARNED_POINTS] = ApiContants.SHARE_CALENDER_POINTS.toString()
        params[ApiContants.TYPE] = ApiContants.SHARE_TYPE_CALENDER
        viewModel.callShareApi(params)
    }

    private fun setTopImageHeight(heightInPercent: Double) {
        /*If screen size is greater than 6 inches, then we will
        * not move the calender upward, otherwise we will move it up*/
        if (screenSizeInInches > 6) {
            return
        }

        val lp = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
        val animator = ValueAnimator.ofInt(
            lp.guideBegin,
            (AppUtils.getScreenHeight(context!!) * heightInPercent).toInt()
        )
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                val value = valueAnimator?.animatedValue as Int
                val layoutParams = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.guideBegin = value
                binding.guideline.layoutParams = layoutParams
            }
        })

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                //loadCalenderThemeImage()
            }
        })

        animator.duration = 250;
        animator.start()
    }

    private fun loadCalenderThemeImage() {
        binding.ivGoddess.post {
            context?.let {
                AppUtils.loadImageThroughGlide(
                    it, binding.ivGoddess,
                    AppUtils.generateImageUrl(
                        calenderThemeImagePathOnly,
                        binding.ivGoddess.width,
                        binding.ivGoddess.height
                    ),
                    R.drawable.ic_placeholder_rect
                )
            }
        }
    }

    fun tokenUpdate() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FIREBASE", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                if (prefs.firebaseToken != task.result?.token.toString()) {
                    token = task.result?.token.toString()
                    if (prefs.isLoggedIn) {
                        viewModel.getUpdateTokeApi(prefs.userDataModel!!.result!!.uId!!, token)
                    }
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in 0 until adapter.count) {
            val fragment = adapter.getItem(i)
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}