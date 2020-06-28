package com.krystal.goddesslifestyle.ui.subscription

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.SubscriptionBenefitsAdapter
import com.krystal.goddesslifestyle.adapter.SubscriptionsAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.Benefit
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.databinding.ActivitySubscriptionBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.membership_layout.view.*
import javax.inject.Inject


class SubscriptionActivity : BaseActivity<SubscriptionViewModel>(), PurchasesUpdatedListener,
    View.OnClickListener {


    /*A static method, returning an intent to start this activity*/
    companion object {
        fun newInstance(context: Context, disableBasicPlans: Boolean = false): Intent {
            val intent = Intent(context, SubscriptionActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_DISABLE_BASIC_PLAN, disableBasicPlans)
            return intent
        }
    }

    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var binding: ActivitySubscriptionBinding

    /*A flag variable, to detect benefits state(i.e. Expanded or collapsed)
     * This variable is needed, becuase we have to change the state of both the subscriptions altogether */
    private var isBenefitsCollapsed = false

    /*Again a flag variable to check which subscription is selected among the 2
    * This is needed becuase, we have same layout for both the subscriptions
    * and we have included this seperate layout in this main layout.*/
    private var isBasicPlanSelected = true

    /*Sku list from Google Play query*/
    private var skuDetailsList: MutableList<SkuDetails>? = null

    /*variable for selected basic position 0 means monthly and 1 will be yearly*/
    private var selectedBasicPosition = 0

    /*variable for selected premium position 0 means monthly and 1 will be yearly*/
    private var selectedPremiumPosition = 0

    // selected SKU details object
    private var selectedSkuDetails: SkuDetails? = null

    // list of basic SKUs
    private var basicSkuDetailsList: ArrayList<SkuDetails>? = ArrayList()

    // list of premium SKUs
    private var premierSkuDetailsList: ArrayList<SkuDetails>? = ArrayList()

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var appDatabase: AppDatabase

    // user subscription object to check if user currently having any subscription plan or not
    var userSubscription: UserSubscription? = null

    // boolean representing is user have any active plans or not
    var isUserHasActivePlan = false

    /*boolean for should we disable the basic plans or not
    * when user is already subscribed to basic plan and want to subscribe for Premium
    * then we will disable the basic*/
    var disableBasicPlans = false

    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectSubscriptionsActivity(this)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription)
        binding.activity = this

        viewModel.setInjectable(apiService, prefs)

        /*if (!prefs.isLoggedIn) {
            startActivity(LoginActivity.newInstance(this, ""))
            AppUtils.startFromRightToLeft(this)
            finish()
        }*/
        viewModel.setAppDatabase(appDatabase)

        intent?.let {
            disableBasicPlans = it.getBooleanExtra(AppConstants.EXTRA_DISABLE_BASIC_PLAN, false)
        }

        /*Check if free trail is used or not
        * based on that show the button for that*/
        if (AppUtils.isFreeTrialUsed(this)) {
            binding.btnFreeTrial.visibility = View.GONE
            binding.btnRestorePurchase.visibility = View.GONE
        } else {
            binding.btnFreeTrial.visibility = View.VISIBLE
            binding.btnRestorePurchase.visibility = View.GONE
        }

        userSubscription = viewModel.getUserSubscription()
        if (userSubscription != null) {
            isUserHasActivePlan = true
        }

        if (disableBasicPlans) {
            /*Disable the basic plans.*/
            isBasicPlanSelected = false
            binding.basicPlans.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.disabled_pink
                )
            )
            binding.basicPlans.isClickable = false
            binding.basicPlans.isEnabled = false
        } else {
            /*Do not Disable the basic plans.*/
            isBasicPlanSelected = true
            binding.basicPlans.setBackgroundColor(ContextCompat.getColor(this, R.color.pink))
            binding.basicPlans.isClickable = true
            binding.basicPlans.isEnabled = true
        }

        //AppUtils.showToast(this, AppUtilsJava.getEmailID(this))
        initializeInAppBillingClient()
        init()

    }

    private fun init() {
        //showProgress()
        // toolbar left icon and its Cclick listener
        setToolbarLeftIcon(
            R.drawable.ic_back_pink,
            object : BaseActivity.ToolbarLeftImageClickListener {
                override fun onLeftImageClicked() {
                    onBackPressed()
                }
            })

        viewModel.getVerifyResponse().observe(this, verifySubsObserver)

        // Basic benefits from AppUtils method (Its static)
        val basicBenefits: ArrayList<Benefit> = AppUtils.getBasicBenefits()
        // Premium benefits, again static
        val premiumBenefits: ArrayList<Benefit> = AppUtils.getPremiumBenefits()

        val monthlyBenefitsAdapter = SubscriptionBenefitsAdapter()
        monthlyBenefitsAdapter.setItem(basicBenefits)

        val yearlyBenefitsAdapter = SubscriptionBenefitsAdapter()
        yearlyBenefitsAdapter.setItem(basicBenefits)

        setUpSubscriptionData(
            binding.basicPlans, R.string.basic,
            "", "per month", basicBenefits
        )

        setUpSubscriptionData(
            binding.premiumPlans, R.string.premium,
            "", "per month", premiumBenefits
        )

        /*Change the selection icon based on the selection made*/
        if (isBasicPlanSelected) {
            binding.basicPlans.iv_radio_button.setImageResource(R.drawable.ic_selected)
            binding.premiumPlans.iv_radio_button.setImageResource(R.drawable.ic_unselected)
        } else {
            binding.basicPlans.iv_radio_button.setImageResource(R.drawable.ic_unselected)
            binding.premiumPlans.iv_radio_button.setImageResource(R.drawable.ic_selected)
        }

        binding.basicPlans.setOnClickListener {
            binding.basicPlans.iv_radio_button.setImageResource(R.drawable.ic_selected)
            binding.premiumPlans.iv_radio_button.setImageResource(R.drawable.ic_unselected)
            isBasicPlanSelected = true
        }

        binding.premiumPlans.setOnClickListener {
            binding.basicPlans.iv_radio_button.setImageResource(R.drawable.ic_unselected)
            binding.premiumPlans.iv_radio_button.setImageResource(R.drawable.ic_selected)
            isBasicPlanSelected = false
        }

        binding.basicPlans.tv_sub_desc.setOnClickListener {
            binding.basicPlans.tv_amount.performClick()
        }

        binding.premiumPlans.tv_sub_desc.setOnClickListener {
            binding.premiumPlans.tv_amount.performClick()
        }

        binding.basicPlans.tv_amount.setOnClickListener {
            binding.basicPlans.performClick()
            setBasicPlansDropDown(binding.basicPlans.tv_sub_desc)
        }

        binding.premiumPlans.tv_amount.setOnClickListener {
            binding.premiumPlans.performClick()
            setPremiumPlansDropDown(binding.premiumPlans.tv_sub_desc)
        }
    }

    /*Drop down for Basic plans*/
    private fun setBasicPlansDropDown(anchorView: View) {

        val basicPlansPopupWindow = ListPopupWindow(this)

        val adapter = SubscriptionsAdapter(this, basicSkuDetailsList)
        basicPlansPopupWindow.setAdapter(adapter)


        basicPlansPopupWindow.anchorView = anchorView
        basicPlansPopupWindow.isModal = true

        basicPlansPopupWindow.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedBasicPosition = position
                basicPlansPopupWindow.dismiss()
                val sku = basicSkuDetailsList?.get(position)
                sku?.let {
                    setBasicPlan(sku)
                }
            }
        })
        basicPlansPopupWindow.show()
    }

    private fun setPremiumPlansDropDown(anchorView: View) {

        val premiumPlansPopupWindow = ListPopupWindow(this)

        val adapter = SubscriptionsAdapter(this, premierSkuDetailsList)
        premiumPlansPopupWindow.setAdapter(adapter)


        premiumPlansPopupWindow.anchorView = anchorView
        premiumPlansPopupWindow.isModal = true

        premiumPlansPopupWindow.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPremiumPosition = position
                premiumPlansPopupWindow.dismiss()
                val sku = premierSkuDetailsList?.get(position)
                sku?.let {
                    setPremiumPlan(sku)
                }
            }
        })

        premiumPlansPopupWindow.show()
    }

    override fun getViewModel(): SubscriptionViewModel {
        viewModel = ViewModelProvider(this).get(SubscriptionViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun setUpSubscriptionData(
        view: View, membershipType: Int, amount: String, desc: String,
        benefits: ArrayList<Benefit>
    ) {
        view.tv_membership_name.text = getString(membershipType)
        view.tv_amount.text = amount
        view.tv_sub_desc.text = desc

        showBenefits(view, benefits)

        view.tv_expand_collapse_benifits.visibility = View.GONE
    }

    // showing benefits
    private fun showBenefits(view: View, benefits: ArrayList<Benefit>) {
        for ((i, benefit) in benefits.withIndex()) {
            val textView = LayoutInflater.from(this)
                .inflate(R.layout.list_row_benifits, null) as AppCompatTextView
            textView.text = benefit.benefit
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            /*setting an icon based on it is included in this plan or not*/
            if (benefit.included) {
                textView.setTextColor((Color.parseColor("#ffffff")))
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_right_tick, 0, 0, 0);
                textView.setPaintFlags(textView.getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv())
            } else {
                textView.setTextColor((Color.parseColor("#fad0d0")))
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cross, 0, 0, 0);
                textView.setPaintFlags(textView.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
            }
            view.ll_benifits.addView(textView)

        }
    }

    /*[START] a code for the In App Billing*/
    private lateinit var billingClient: BillingClient

    private fun initializeInAppBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    getPurchasedDetails()
                    // get our skud details
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                AppUtils.showToast(this@SubscriptionActivity, "Billing disconnected")
                hideProgress()
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    /*Query our SKU detilas that we created on Play Console*/
    private fun querySkuDetails() {
        val skuList = ArrayList<String>()
        /*skuList.add("monthly_subscription")
        skuList.add("yearly_subscription")*/

        // Below are our created SKU ids
        skuList.add(AppConstants.BASIC_MONTHLY_SKU)
        skuList.add(AppConstants.BASIC_YEARLY_SKU)
        skuList.add(AppConstants.PREMIUM_MONTHLY_SKU)
        skuList.add(AppConstants.PREMIUM_YEARLY_SKU)

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        params.build()

        /*Method to query SKU details async*/
        billingClient.querySkuDetailsAsync(params.build(), object : SkuDetailsResponseListener {
            override fun onSkuDetailsResponse(
                billingResult: BillingResult?,
                skuDetailsList: MutableList<SkuDetails>?
            ) {
                if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    //AppUtils.showToast(this@SubscriptionActivity, "SKU details response")
                    this@SubscriptionActivity.skuDetailsList = skuDetailsList
                    setDummyText()

                    /*Iterating over all SKUs*/
                    for (skuDetails in skuDetailsList) {
                        val sku = skuDetails.sku

                        /*If its basic SKU then add in basic List otherwise add in premium list*/

                        if (AppConstants.BASIC_MONTHLY_SKU == sku || AppConstants.BASIC_YEARLY_SKU == sku) {
                            basicSkuDetailsList?.add(skuDetails)
                        }
                        if (AppConstants.PREMIUM_MONTHLY_SKU == sku || AppConstants.PREMIUM_YEARLY_SKU == sku) {
                            premierSkuDetailsList?.add(skuDetails)
                        }
                        // By default set Basic Monthly as selected
                        if (AppConstants.BASIC_MONTHLY_SKU == sku) {
                            selectedSkuDetails = skuDetails
                            setBasicPlan(skuDetails)
                        } else if (AppConstants.PREMIUM_MONTHLY_SKU == sku) {
                            setPremiumPlan(skuDetails)
                        }
                        hideProgress()
                    }
                }
            }
        })
    }

    // setting dummy text as per design
    private fun setDummyText() {
        var monthlyPrice = ""
        var yearlyPrice = ""
        for (skuDetails in skuDetailsList!!) {
            val sku = skuDetails.sku

            if (AppConstants.BASIC_MONTHLY_SKU == sku) {
                monthlyPrice = skuDetails.price
            }
            if (AppConstants.BASIC_YEARLY_SKU == sku) {
                yearlyPrice = skuDetails.price
            }
        }

        binding.tvDesc.text = String.format(
            resources.getString(R.string.subscription_desc),
            monthlyPrice,
            yearlyPrice
        )
    }

    /*Set Basic plans text and Prices*/
    private fun setBasicPlan(skuDetails: SkuDetails?) {
        if (isBasicPlanSelected) {
            selectedSkuDetails = skuDetails
        }
        binding.basicPlans.tv_amount.text = skuDetails?.price
        if (skuDetails?.subscriptionPeriod == "P1M") {
            binding.basicPlans.tv_sub_desc.text = "per month"
        } else if (skuDetails?.subscriptionPeriod == "P1Y") {
            binding.basicPlans.tv_sub_desc.text = "per year"
        }
    }

    /*Set Premium plans text and Prices*/
    private fun setPremiumPlan(skuDetails: SkuDetails?) {
        if (!isBasicPlanSelected) {
            selectedSkuDetails = skuDetails
        }
        binding.premiumPlans.tv_amount.text = skuDetails?.price
        if (skuDetails?.subscriptionPeriod == "P1M") {
            binding.premiumPlans.tv_sub_desc.text = "per month"
        } else if (skuDetails?.subscriptionPeriod == "P1Y") {
            binding.premiumPlans.tv_sub_desc.text = "per year"
        }
    }

    /*Callback when purchase state updates*/
    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        //AppUtils.showToast(this, ""+billingResult!!.responseCode)
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
            //if()
            AppUtils.showToast(this, "An Error occurred. Please try again")
        }

    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            // Acknowledge the purchase if it hasn't already been acknowledged.
            showProgress()
            //AppUtils.showToast(this@SubscriptionActivity, "Purchased successfully!")
            Log.e("PURCHASE_TOKEN", "" + purchase.purchaseToken)
            Log.e("PURCHASE_STATE", "" + purchase.purchaseState)

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    //.setDeveloperPayload(purchase.developerPayload)
                    .build()
                billingClient.acknowledgePurchase(
                    acknowledgePurchaseParams,
                    object : AcknowledgePurchaseResponseListener {
                        override fun onAcknowledgePurchaseResponse(billingResult: BillingResult?) {
                            //AppUtils.showToast(this@SubscriptionActivity, "Purchase acknowledged")


                            /*Call an API to verify a purchase on our server*/

                            val params: HashMap<String, String> = HashMap()
                            params.put(ApiContants.SUBSCRIPTION_ID, purchase.sku)
                            params.put(ApiContants.TOKEN, purchase.purchaseToken)

                            var skuDetails: SkuDetails? = null
                            if (isBasicPlanSelected) {
                                skuDetails = basicSkuDetailsList?.get(selectedBasicPosition)
                            } else {
                                skuDetails = premierSkuDetailsList?.get(selectedPremiumPosition)
                            }
                            skuDetails?.let {
                                if (it.subscriptionPeriod == "P1M") {
                                    params.put(ApiContants.TYPE, ApiContants.MONTHLY_SUBS)
                                } else if (it.subscriptionPeriod == "P1Y") {
                                    params.put(ApiContants.TYPE, ApiContants.YEARLY_SUBS)
                                } else {
                                    params.put(ApiContants.TYPE, ApiContants.FREE_TRIAL_SUBS)
                                }
                            }
                            Log.e("JSON", purchase.originalJson)
                            Log.e("TIMELINE", purchase.originalJson)
                            //params.put(ApiContants.TYPE, purchase.purchaseToken)
                            params.put(ApiContants.DEVICETYPE, "android")

                            viewModel.callVerifySubscriptionApi(params = params)
                        }
                    })
                /*val ackPurchaseResult = withContext(Dispatchers.IO) {
                    client.acknowledgePurchase(acknowledgePurchaseParams.build())
                }*/
            } else {

            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            AppUtils.showToast(this@SubscriptionActivity, "Purchase pending")
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.

        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            AppUtils.showToast(this@SubscriptionActivity, "Purchase in Unspecified state")
        }

    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        when (v?.id) {
            R.id.btn_subscribe_now -> {
                if (!prefs.isLoggedIn) {
                    startActivity(LoginActivity.newInstance(this, ""))
                    AppUtils.startFromRightToLeft(this)
                    finish()
                } else {
                    /*Make a purchase based on the selected plan*/
                    if (isBasicPlanSelected) {
                        makePurchase(basicSkuDetailsList?.get(selectedBasicPosition))
                    } else {
                        makePurchase(premierSkuDetailsList?.get(selectedPremiumPosition))
                    }
                }
            }
            R.id.btn_free_trial -> {
                if (!prefs.isLoggedIn) {
                    startActivity(LoginActivity.newInstance(this, ""))
                    AppUtils.startFromRightToLeft(this)
                    finish()
                } else {
                    // call Free trial API
                    callApiForFreeTrial()
                }
            }
            R.id.btn_restore_purchase -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient.isReady) {
            Log.d("", "BillingClient can only be used once -- closing connection")
            // BillingClient can only be used once.
            // After calling endConnection(), we must create a new BillingClient.
            billingClient.endConnection()
        }
    }


    /*Make actual In App purchase*/
    private fun makePurchase(skuDetails: SkuDetails?) {
        if (isUserHasActivePlan) {
            if (userSubscription!!.usSubscriptionPlanId.equals(skuDetails!!.sku)) {
                AppUtils.showSimpleDialog(this, "You already purchased this subscription plan")
                return
            }
        }
        val flowParams: BillingFlowParams = if (!isUserHasActivePlan) {
            BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        } else {
            BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .setOldSku(userSubscription!!.usSubscriptionPlanId)
                .setReplaceSkusProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE)
                .build()
        }
        billingClient.launchBillingFlow(this@SubscriptionActivity, flowParams)
        //AppUtils.showToast(this, ""+responseCode.debugMessage)
        //AppUtils.showToast(this, ""+responseCode.responseCode)
    }

    /*This method returns the purchased made on this device*/
    private fun getPurchasedDetails() {
        val purchaseResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (purchaseResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
            purchaseResult.purchasesList != null
        ) {

            if (purchaseResult.purchasesList.isNotEmpty() && !isUserHasActivePlan) {
                binding.btnSubscribeNow.isEnabled = false
                showPurchasedViaDiffAccDialog()
            }

            /*for (purchase in purchaseResult.purchasesList) {
                //AppUtils.showToast(this, ""+purchase.sku)
            }*/
        }

    }

    private val verifySubsObserver = Observer<UserSubscriptionResponse> {
        if (it.status) {
            it.result?.let {
                appDatabase.userSubscriptionDao().insert(it)

                AppUtils.showToast(this, "Your purchase is successful!")
                startActivity(MainActivity.newInstance(this, clearAllActivities = true))
                //finish()
            }
        } else {
            AppUtils.showToast(this, it.message)
        }
    }
    /*[END] a code for the In App Billing*/

    private fun callApiForFreeTrial() {

        if (premierSkuDetailsList == null) {
            return
        }

        val premiumMonthlySku = AppConstants.PREMIUM_MONTHLY_SKU
        val params: HashMap<String, String> = HashMap()
        params.put(ApiContants.SUBSCRIPTION_ID, premiumMonthlySku)

        //params.put(ApiContants.TOKEN, "")

        params.put(ApiContants.TYPE, ApiContants.FREE_TRIAL_SUBS)
        //params.put(ApiContants.TYPE, purchase.purchaseToken)
        params.put(ApiContants.DEVICETYPE, "android")

        viewModel.callVerifySubscriptionApi(true, params)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }

    private fun showPurchasedViaDiffAccDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Subscription")
        builder.setMessage(
            "This device already purchased a subscription with different account. Please login with that account" +
                    "to use subscription features"
        )
        builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0?.dismiss()
                finish()
            }
        })

        val dialog = builder.create()
        dialog.show()
    }
}
