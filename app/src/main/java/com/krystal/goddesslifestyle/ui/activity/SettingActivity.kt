package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.HowToUseAppActivity
import com.krystal.goddesslifestyle.MyPlanActivity
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.SettingResponse
import com.krystal.goddesslifestyle.databinding.ActivitySettingsBinding
import com.krystal.goddesslifestyle.databinding.LayoutImagesAndTextviewSecondBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.community.PlayVideoActivity
import com.krystal.goddesslifestyle.ui.profile.YogaPointsActivity
import com.krystal.goddesslifestyle.ui.subscription.SubscriptionActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.SettingModel
import javax.inject.Inject

class SettingActivity : BaseActivity<SettingModel>(), View.OnClickListener {


    private lateinit var binding: ActivitySettingsBinding
    private lateinit var mViewModel: SettingModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    var practiceNotification: Boolean = false
    var communityNotification: Boolean = false
    var shoppingNotification: Boolean = false

    var progressValue: Int = 0

    private val currentLevelPoint: Int by lazy {
        intent.getIntExtra(AppConstants.CURRENT_LEVEL_POINT, 0)
    }

    companion object {
        fun newInstance(context: Context, progressValue: Int, currentLevelPoint: Int): Intent {
            val intent = Intent(context, SettingActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_PROGRESS_VALUE, progressValue)
            intent.putExtra(AppConstants.CURRENT_LEVEL_POINT, currentLevelPoint)
            return intent
        }
    }

    override fun getViewModel(): SettingModel {
        mViewModel = ViewModelProvider(this).get(SettingModel::class.java)
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
        requestsComponent.injectSettingActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        init()
    }

    fun init() {
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.setAppDatabase(appDatabase)

        intent?.let {
            progressValue = it.getIntExtra(AppConstants.EXTRA_PROGRESS_VALUE, 0)
        }

        binding.btnCancelAccount.setOnClickListener(this)
        mViewModel.getSettingResponse().observe(this, settingObserver)
        mViewModel.getCancelResponse().observe(this, cancelAccountObserve)
        setNotificationSetting()
        setToolBar(getString(R.string.lbl_setting), R.color.pink)
        setLink(
            binding.viewInviteFriend,
            getString(R.string.lbl_invite_friends),
            R.drawable.ic_invite_pink
        )
        setLink(
            binding.viewAboutUs,
            getString(R.string.lbl_about_us),
            R.drawable.ic_about_us
        )
        setLink(
            binding.viewMyContactUs,
            getString(R.string.lbl_contact_us),
            R.drawable.ic_contact_us
        )
        setLink(
            binding.viewFAQ,
            getString(R.string.lbl_faq_),
            R.drawable.ic_faq
        )
        setLink(
            binding.viewHowToUseApp,
            getString(R.string.lbl_how_to_use_app),
            R.drawable.ic_how_to_use
        )
        setLink(
            binding.viewHowToUseAppVideo,
            getString(R.string.lbl_how_to_use_app_video),
            R.drawable.ic_how_to_use
        )
        setLink(
            binding.viewJoinFB,
            getString(R.string.lbl_join_our_community),
            R.drawable.ic_facebook
        )
        setLink(
            binding.viewJoinInstagram,
            getString(R.string.lbl_join_our_community),
            R.drawable.ic_insta
        )
    }

    private fun setNotificationSetting() {
        val respose = prefs.userDataModel?.result
        respose?.let {
            if (respose.uEnablePracticeNotification == 1) {
                binding.ivPracticeNoti.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_on
                    )
                )
                practiceNotification = true
            } else {
                binding.ivPracticeNoti.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_off
                    )
                )
                practiceNotification = false

            }
            if (respose.uEnableCommunityNotification == 1) {
                binding.ivCommunityNoti.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_on
                    )
                )
                communityNotification = true

            } else {
                binding.ivCommunityNoti.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_off
                    )
                )
                communityNotification = false
            }
            if (respose.uEnableShoppingNotification == 1) {
                binding.ivShoppingNoti.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_on
                    )
                )
                shoppingNotification = true
            } else {
                binding.ivShoppingNoti.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_off
                    )
                )
                shoppingNotification = false
            }

        }
        binding.ivCommunityNoti.setOnClickListener(this)
        binding.ivPracticeNoti.setOnClickListener(this)
        binding.ivShoppingNoti.setOnClickListener(this)
    }


    private val settingObserver = Observer<SettingResponse> {
        val userModel = prefs.userDataModel
        if (it.status) {
            userModel?.result?.uEnablePracticeNotification = it.result.uEnablePracticeNotification
            userModel?.result?.uEnableCommunityNotification = it.result.uEnableCommunityNotification
            userModel?.result?.uEnableShoppingNotification = it.result.uEnableShoppingNotification
            prefs.userDataModel = userModel

        }

    }
    private val cancelAccountObserve = Observer<BaseResponse> {
        if (it.status) {
            mViewModel.clearCartData()
            prefs.let {
                val rememberMe = it.rememberMe
                if (rememberMe) {
                    val currentUsername = it.userName
                    val currentPassword = it.password
                    it.clearPrefs()
                    it.rememberMe = true
                    it.userName = currentUsername
                    it.password = currentPassword
                } else {
                    it.clearPrefs()
                }
            }
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }
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


    }

    private fun setLink(
        view: LayoutImagesAndTextviewSecondBinding?,
        name: String,
        images: Int
    ) {
        view?.let {
            view.tvName.text = name
            view.iv.setBackgroundResource(images)

        }

        binding.viewInviteFriend.view.setOnClickListener {
            AppUtils.shareContent(
                this,
                "Share from Goddess LifeStyle Android App ReferralCode " + prefs.userDataModel!!.result!!.uReferralCode
            )

        }
        binding.viewAboutUs.view.setOnClickListener {
            startActivity(
                WebViewActivity.newInstance(
                    this@SettingActivity,
                    getString(R.string.lbl_about_us),
                    AppConstants.ABOUT_US_URL
                )
            )
        }
        binding.viewMyContactUs.view.setOnClickListener {
            startActivity(ContactUsActivity.newInstance(this))
        }
        binding.viewFAQ.view.setOnClickListener {
            startActivity(
                WebViewActivity.newInstance(
                    this@SettingActivity,
                    getString(R.string.lbl_faq_),
                    AppConstants.FAQ_URL
                )
            )
        }
        binding.viewHowToUseApp.view.setOnClickListener {
            startActivity(YogaPointsActivity.newInstance(this, progressValue,currentLevelPoint))
            AppUtils.startFromRightToLeft(this)
        }

        binding.viewHowToUseAppVideo.view.setOnClickListener {
            startActivity(
                PlayVideoActivity.newInstance(
                    this,
                    "http://goddess.reviewprototypes.com/public/uploads/intro/app-welcome.mp4"
                )
            )
            AppUtils.startFromRightToLeft(this)
        }
        binding.viewJoinFB.view.setOnClickListener {
            startActivity(
                WebViewActivity.newInstance(
                    this@SettingActivity,
                    getString(R.string.lbl_faq_),
                    AppConstants.FB_URL
                )
            )
        }
        binding.viewJoinInstagram.view.setOnClickListener {
            startActivity(
                WebViewActivity.newInstance(
                    this@SettingActivity,
                    getString(R.string.lbl_faq_),
                    AppConstants.INSTAGRAM_URL
                )
            )
        }
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (v.id) {
                R.id.ivPracticeNoti -> {
                    if (practiceNotification) {
                        practiceNotification = false
                        apicall()
                        binding.ivPracticeNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_off
                            )
                        )
                    } else {
                        practiceNotification = true
                        apicall()
                        binding.ivPracticeNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_on
                            )
                        )
                    }
                }
                R.id.ivCommunityNoti -> {

                    if (communityNotification) {
                        communityNotification = false
                        apicall()
                        binding.ivCommunityNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_off
                            )
                        )
                    } else {
                        communityNotification = true
                        apicall()
                        binding.ivCommunityNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_on
                            )
                        )
                    }
                }
                R.id.ivShoppingNoti -> {

                    if (shoppingNotification) {
                        shoppingNotification = false
                        apicall()
                        binding.ivShoppingNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_off
                            )
                        )
                    } else {
                        shoppingNotification = true
                        apicall()
                        binding.ivShoppingNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_on
                            )
                        )
                    }

                }
                R.id.btnCancelAccount -> {
                    showCancelAccountDialog()
                }
            }
        }
    }

    private fun apicall() {
        mViewModel.callSettingApi(practiceNotification, communityNotification, shoppingNotification)
    }


    fun showCancelAccountDialog() {
        val alert = AlertDialog.Builder(this)

        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_cancel_account, null)
        val btnSubmit = alertLayout.findViewById<AppCompatTextView>(R.id.tvOk)
        val btnCancel = alertLayout.findViewById<AppCompatTextView>(R.id.tvCancel)

        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnSubmit.setOnClickListener {
            mViewModel.callCancelAccountApi()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


    }

}
