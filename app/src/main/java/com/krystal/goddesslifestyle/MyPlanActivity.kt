package com.krystal.goddesslifestyle

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.Benefit
import com.krystal.goddesslifestyle.databinding.ActivityMyPlanBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.MyPlansViewModel
import javax.inject.Inject

class MyPlanActivity : BaseActivity<MyPlansViewModel>() {

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, MyPlanActivity::class.java)
            return intent
        }
    }

    /*ViewModel*/
    private lateinit var vModel: MyPlansViewModel
    /*binding variable*/
    private lateinit var binding: ActivityMyPlanBinding

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectMyPlansActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_plan)
        super.onCreate(savedInstanceState)

        vModel.injectPrefs(prefs)
        vModel.setAppDatabase(appDatabase)
        setUpData()
        //init()
    }

    private fun setUpData() {
        val subscription = vModel.getUserSubscription()
        subscription?.let {
            it.usSubscriptionPlanId?.let { planId ->
                if(planId.startsWith("basic")) {
                    init("Basic")
                    binding.tvMostPopular.visibility = View.GONE
                    setBenefits(AppUtils.getBasicBenefits())
                    binding.btnUpdatePlan.text = "Upgrade Plan"
                } else {
                    init("Premium")
                    it.usPrice?.let { price ->
                        if(price.toDouble() != 0.0) {
                            binding.tvMostPopular.visibility = View.VISIBLE
                        } else {
                            binding.tvMostPopular.visibility = View.GONE
                        }
                    }
                    setBenefits(AppUtils.getPremiumBenefits())
                    binding.btnUpdatePlan.text = "Update Plan"
                }
            }
            it.usPrice?.let {price ->
                /*if(price.toDouble() == 0.0) {
                    binding.tvPrice.text = "Free Trial"
                } else {
                    binding.tvPrice.text = price
                }*/
                binding.tvPrice.text = price
            }
            it.usType?.let { type ->
                if(type == ApiContants.MONTHLY_SUBS.toInt()) {
                    binding.tvBillingCycle.text = "/month"
                } else if(type == ApiContants.YEARLY_SUBS.toInt()) {
                    binding.tvBillingCycle.text = "/year"
                } else {
                    binding.tvBillingCycle.text = "/7 days"
                }
            }
        }

    }

    private fun init(title: String) {
        setToolbarTitle(title)
        setToolbarColor(R.color.pink)
        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.pink)
        }

        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })
    }

    private fun setBenefits(benefits: ArrayList<Benefit>) {
        for ((i, benefit) in benefits.withIndex()) {
            val textView = LayoutInflater.from(this).inflate(R.layout.list_row_benifits_big, null) as AppCompatTextView
            textView.text = benefit.benefit
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if(benefit.included) {
                textView.setTextColor((Color.parseColor("#605e5e")))
                textView.setPaintFlags(textView.getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv())
            } else {
                textView.setTextColor((Color.parseColor("#a09e9e")))
                textView.setPaintFlags(textView.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
            }
            binding.llBenefits.addView(textView)
        }
    }

    override fun getViewModel(): MyPlansViewModel {
        vModel = ViewModelProvider(this).get(MyPlansViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromRightToLeft(this)
    }
}
