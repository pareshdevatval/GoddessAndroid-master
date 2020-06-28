package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.AppSettingsResponse
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.databinding.ActivitySplashBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppConstants.SPLASH_TIME
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.SplashViewModel
import java.security.MessageDigest
import javax.inject.Inject


class SplashActivity : BaseActivity<SplashViewModel>() {

    /*A static method to start this Activity*/
    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            return intent
        }
    }

    private lateinit var mViewModel: SplashViewModel
    private lateinit var binding: ActivitySplashBinding
    private var handler: Handler? = null

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    /*A boolean value representing the response of API is received or not
    * This is useful to detect should we start the app after fix SPLASH_TIME or after API response received*/
    private var isResponseReceived = false

    /*An another boolean value representing if SPLASH_TIME is over or not*/
    private var isTimeOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        /*window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)*/

        /*NetworkLocalComponent of DI will provide us all the necessary variables that we need to use in this activity
        * So, initializing DI here*/
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectSplashActivity(this)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.tvDisplayVersion.text = AppConstants.DISPLAY_VERSION
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        init()

        /* Observing the LiveData varaible data changes from ViewModel class*/
        mViewModel.getUserSubscriptionResponse().observe(this, userSubscriptionObserver)
        mViewModel.getAppSettingsResponse().observe(this, appSettingsResponseObserver)

        /*If user is logged in, then call an API to get the user subscription details*/
        if (prefs.isLoggedIn) {
            mViewModel.callGetUserSubscriptionApi()
        } else {
            /*Otherwise just call the settings API*/
            mViewModel.callAppSettingsApi()
        }
        // Billing APIs are all handled in the this lifecycle observer.
        /*billingManager = (application as GoddessLifeStyleApp).getBillingManager()
        lifecycle.addObserver(billingManager)*/

        try {
            val info: PackageInfo = packageManager
                .getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e("AppLog", "key:$hashKey=")
            }
        } catch (e: Exception) {
            Log.e("AppLog", "error:", e)
        }
    }

    override fun getViewModel(): SplashViewModel {
        mViewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun init() {
        /*Handler object*/
        handler = Handler()
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.setAppDatabase(appDatabase)

        // a method to store firebase token into prefs
        AppUtils.getFiresafeNotifications(prefs)
    }

    override fun onResume() {
        super.onResume()
        handler!!.postDelayed(mRunnable, SPLASH_TIME.toLong())
    }

    private val mRunnable: Runnable = Runnable {
        /*Check if activity is running or not to prevent runtime crash*/
        if (!isFinishing) {
            if (prefs.isLoggedIn) {
                if (isResponseReceived) {
                    /*If response is received, then start the app, Otherwise wait for a response*/
                    startApp()
                } else {
                    // Time is over. Now App will start after the response
                    isTimeOver = true
                }
            } else {
                startApp()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        handler!!.removeCallbacks(mRunnable)
    }

    private fun startApp() {
        /*Either user is logged in or has skipped the login
            * then we will redirect him to MainActivity*/

        if (prefs.isLoggedIn || prefs.isSkipped) {
            startActivity(MainActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
        } else {
            // Otherwise we will start from the starch
            startActivity(TutorialActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
        }
        finishAffinity()

    }

    private val userSubscriptionObserver = Observer<UserSubscriptionResponse> {
        if (it.status) {
            it.result?.let {
                // save the subscription data in database
                appDatabase.userSubscriptionDao().insert(it)
            }
        }
        mViewModel.callAppSettingsApi()
    }

    private val appSettingsResponseObserver = Observer<AppSettingsResponse> {
        isResponseReceived = true
        if (it.status) {
            // store settings in prefs
            prefs.settings = it
            it.result?.let {
                prefs.stripePublicKey = it.stripe_public_key!!
                prefs.stripeSecretKey = it.stripe_secret_key!!
            }
        }
        if (isTimeOver) {
            // if SPLASH_TIME is passed then start the app
            startApp()
        }
    }
}


