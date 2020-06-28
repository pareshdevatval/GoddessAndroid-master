package com.krystal.goddesslifestyle.ui.profile

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.krystal.goddesslifestyle.BuildConfig
import com.krystal.goddesslifestyle.MyPlanActivity
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.MyProfileResponse
import com.krystal.goddesslifestyle.databinding.ActivityMyProfileBinding
import com.krystal.goddesslifestyle.databinding.LayoutImagesAndTextviewBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.*
import com.krystal.goddesslifestyle.ui.subscription.SubscriptionActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.MyProfileModel
import javax.inject.Inject

class MyProfileActivity : BaseActivity<MyProfileModel>(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (it.id) {
                R.id.tvUserName -> {
                    startActivityForResult(EditProfileActivity.newInstance(this), 101)
                }
                R.id.tvMaiden -> {
                    val subscriptionStatus = AppUtils.getUserSubscription(this)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(this)
                    } else {
                        if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                            //AppUtils.startSubscriptionActivity(context)
                            AppUtils.startSubscriptionActivity(this, true)
                        } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                            startActivity(
                                YogaPointsActivity.newInstance(
                                    this,
                                    progressValue,
                                    currentLevelPoint
                                )
                            )
                            AppUtils.startFromRightToLeft(this)
                        }
                    }

                }
                R.id.btnLogOut -> {
                    if (prefs.isLoggedIn) {
                        logoutWithDialog(this)
                    }
                }
                R.id.tvInvite -> {
                    AppUtils.shareContent(
                        this,
                        "Share from Goddess LifeStyle Android App ReferralCode " + prefs.userDataModel!!.result!!.uReferralCode
                    )

                }
            }
        }
    }

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var mViewModel: MyProfileModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase
    var progressValue: Int = 0

    var currentLevelPoint = 0

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, MyProfileActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): MyProfileModel {
        mViewModel = ViewModelProvider(this).get(MyProfileModel::class.java)
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
        requestsComponent.injectMyProfileActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_profile)
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.setAppDatabase(appDatabase)
        init()
    }

    fun init() {

        setToolBar(getString(R.string.lbl_my_profile), R.color.pink)

        binding.tvUserName.setOnClickListener(this)
        binding.tvMaiden.setOnClickListener(this)
        binding.btnLogOut.setOnClickListener(this)
        setLink(
            binding.viewFavouritesRecipes,
            getString(R.string.lbl_favourites_recipes),
            R.drawable.ic_favourite_recipe
        )
        setLink(
            binding.viewMySubscriptionPlan,
            getString(R.string.lbl_my_plans),
            R.drawable.ic_my_plans
        )
        setLink(
            binding.viewMyAddress,
            getString(R.string.lbl_my_addresses),
            R.drawable.ic_my_addresses
        )
        setLink(
            binding.viewMyOrder,
            getString(R.string.lbl_my_order),
            R.drawable.ic_my_orders
        )
        setLink(
            binding.viewChangesPassword,
            getString(R.string.lbl_changes_password),
            R.drawable.ic_change_password
        )
        prefs.userDataModel?.result?.uId?.let { mViewModel.callMyProfile(it) }
        mViewModel.getMyProfileResponse().observe(this, myProfileResponseObserver)
        mViewModel.getLogoutResponse().observe(this, logoutResponseObserver)

        binding.tvInvite.setOnClickListener(this)


    }

    /*observer logout data*/
    private val logoutResponseObserver = Observer<BaseResponse> {
        if (it.status) {
            mViewModel.clearCartData()
            AppUtils.logoutUser(this, prefs)
        }
    }

    /*observer my profile data*/
    private val myProfileResponseObserver = Observer<MyProfileResponse> {
        val userData = prefs.userDataModel
        val user = userData?.result
        user?.uPoints = it.result.uPoints
        user?.uStripeCustomerId = it.result.uStripeCustomerId

        prefs.userDataModel = userData

        binding.progressBar1.progress =
            AppUtils.getYourPointPer(it.result.current_level_points.toInt())
        progressValue = AppUtils.getYourPointPer(it.result.current_level_points.toInt())

        binding.tvQueenPoint.text = AppUtils.getYourPointLeft(
            it.result.current_level_points.toInt(),
            binding.ivQueen,
            this
        ).toString()
        AppUtils.setName(this,it.result.current_level_points.toInt(), binding.tvMaiden)
        currentLevelPoint = it.result.current_level_points.toInt()
        setData()
    }

    private fun setData() {
        //set profile data
        val userData = prefs.userDataModel?.result
        userData?.let {
            Glide.with(this)
                .load(BuildConfig.IMAGE_BASE_URL + "" + userData.uImage)
                .placeholder(R.drawable.ic_placeholder_square)
                .into(binding.ivMyprofile)
            binding.tvUserName.text = userData.uUserName?.capitalize()
            binding.tvstartPoint.text = "" + it.uPoints
            it.uReferralCode?.let {
                binding.tvShareCode.text = "Share this code " + it
            }
        }

        binding.viewFavouritesRecipes.view.setOnClickListener {
            val subscriptionStatus = AppUtils.getUserSubscription(this)
            if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                AppUtils.startSubscriptionActivity(this)
            } else {
                startActivity(FavouriteRecipesActivity.newInstance(this))
            }
            AppUtils.startFromRightToLeft(this)
        }

        binding.viewMyAddress.view.setOnClickListener {
            val mySubscription = mViewModel.getUserSubscription()
            if (mySubscription == null) {
                startActivity(SubscriptionActivity.newInstance(this))
            } else {
                startActivity(MyAddressListActivity.newInstance(this))
            }

            AppUtils.startFromRightToLeft(this)
        }
        binding.viewMySubscriptionPlan.view.setOnClickListener {
            val mySubscription = mViewModel.getUserSubscription()
            if (mySubscription == null) {
                AppUtils.showToast(this, "you have not subscribed to any plan.. choose plan")
                startActivity(SubscriptionActivity.newInstance(this))
            } else {
                startActivity(MyPlanActivity.newInstance(this))
            }

            AppUtils.startFromRightToLeft(this)
        }
        binding.viewMyOrder.view.setOnClickListener {
            val subscriptionStatus = AppUtils.getUserSubscription(this)
            if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                AppUtils.startSubscriptionActivity(this)
            } else {
                startActivity(OrderListActivity.newInstance(this))
            }
        }
        binding.viewChangesPassword.view.setOnClickListener {
            startActivity(ChangesPasswordActivity.newInstance(this))
        }
    }

    private fun setLink(
        view: LayoutImagesAndTextviewBinding?,
        name: String,
        images: Int
    ) {
        view?.let {
            view.tvName.text = name
            view.iv.setBackgroundResource(images)

        }


    }

    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color
        setToolbarColor(bgColor)
        // bgcolor of dummyView below tab layout
        //  binding.dummyView.setBackgroundColor(ContextCompat.getColor(this, bgColor))

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_settings,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    startActivity(
                        SettingActivity.newInstance(
                            this@MyProfileActivity,
                            progressValue,
                            currentLevelPoint
                        )
                    )
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getBooleanExtra("is_update", false)
                if (result) {
                    setData()
                }
            }
        }
    }

    fun logoutWithDialog(context: Context?) {
        context?.let {
            val prefs = Prefs.getInstance(it)

            val builder = AlertDialog.Builder(it)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to logout?")

            builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.dismiss()
                    //api call logout
                    mViewModel.callLogOut()
                }
            })

            builder.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.dismiss()
                }
            })

            val dialog = builder.create()
            dialog.show()

        }
    }

}
