package com.krystal.goddesslifestyle.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.MyProfileResponse
import com.krystal.goddesslifestyle.databinding.ActivityWaveBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.OrderSuccessViewModel
import javax.inject.Inject

class WaveActivity : BaseActivity<OrderSuccessViewModel>(), View.OnClickListener {

    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, WaveActivity::class.java)
            return intent
        }
    }

    /*ViewModel*/
    private lateinit var vModel: OrderSuccessViewModel
    /*binding variable*/
    private lateinit var binding: ActivityWaveBinding

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectWaveActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_wave)
        super.onCreate(savedInstanceState)

        vModel.setInjectable(apiService, prefs)
        vModel.getMyProfileResponse().observe(this, myProfileResponseObserver)
        init()
    }

    private fun init() {
        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        }

        binding.ivImage.setImageResource(R.drawable.ic_success)
        binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
        binding.tvTitle.setTextColor(ContextCompat.getColor(this, R.color.yellow))
        binding.tvTitle.text = getString(R.string.success)
        binding.tvDesc.text = getString(R.string.order_success)
        binding.btnAction.text = getString(R.string.continue_shopping)
        binding.btnAction.setOnClickListener(this)
        binding.btnAction.setBackgroundResource(R.drawable.yellow_button)
        AppUtils.startWaveAnimation(this, binding.wave, R.color.yellow)
    }

    override fun getViewModel(): OrderSuccessViewModel {
        vModel = ViewModelProvider(this).get(OrderSuccessViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        when(v?.id) {
            R.id.btn_action -> {
                //vModel.callMyProfile()
                prefs.userDataModel?.result?.uId?.let { vModel.callMyProfile(it) }

            }
        }
    }

    private val myProfileResponseObserver = Observer<MyProfileResponse>{
        val userData = prefs.userDataModel

        userData?.result?.uStripeCustomerId = it.result.uStripeCustomerId

        prefs.userDataModel = userData

        startActivity(MainActivity.newInstance(this, getString(R.string.title_shopping), true))
        //AppUtils.finishActivity(this)
        GoddessAnimations.finishFromLeftToRight(this)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        /*startActivity(MainActivity.newInstance(this, getString(R.string.title_shopping), true))
        GoddessAnimations.finishFromLeftToRight(this)*/
        prefs.userDataModel?.result?.uId?.let { vModel.callMyProfile(it) }
    }
}
