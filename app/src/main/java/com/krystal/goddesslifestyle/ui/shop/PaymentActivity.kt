package com.krystal.goddesslifestyle.ui.shop

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.Stripe3DAuthResponse
import com.krystal.goddesslifestyle.databinding.ActivityPaymentBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.WaveActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.PaymentViewModel
import com.stripe.android.model.PaymentMethod
import com.stripe.android.view.PaymentMethodsActivityStarter
import kotlinx.android.synthetic.main.activity_wave.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class PaymentActivity : BaseActivity<PaymentViewModel>(), View.OnClickListener {

    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context, addressId: Int): Intent {
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra(AppConstants.ADDRESS_ID, addressId)
            return intent
        }
    }

    /*ViewModel*/
    private lateinit var vModel: PaymentViewModel
    /*binding variable*/
    private lateinit var binding: ActivityPaymentBinding

    @Inject
    lateinit var appDatabase: AppDatabase
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var apiService: ApiService

    // logged in user stripe customer Id
    var stripeCustomerId = ""

    // selected payment method by user
    var selectedPaymentMethod: PaymentMethod? = null

    // selected address id
    var addressId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectPaymentActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment)
        super.onCreate(savedInstanceState)

        val user = prefs.userDataModel?.result
        user?.let {
            if (it.uStripeCustomerId != null) {
                stripeCustomerId = it.uStripeCustomerId!!
            }
        }

        intent?.let {
            addressId = it.getIntExtra(AppConstants.ADDRESS_ID, -1)
        }

        vModel.setInjectable(apiService, prefs)
        vModel.setAppDatabase(appDatabase)

        vModel.getPlaceOrderResponse().observe(this, placeOrderResponseObserver)
        //vModel.getPlaceOrderResponseTwo().observe(this, placeOrderResponseObserverTwo)
        vModel.getPaymentMethod().observe(this, paymentMethodObserver)
        vModel.get3dAuthResponse().observe(this, threeDAuthResponseObserver)

        vModel.initStripeSCA()
        init()
    }

    private fun init() {
        setToolbarTitle("Payment")
        setToolbarColor(R.color.yellow)
        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        }

        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        val cartAmount = appDatabase.cartAmountDao().getCartData()
        binding.tvAmount.text = AppUtils.getPriceWithCurrency(this, cartAmount?.newTotalAmount?.toString())

        binding.btnPlaceOrder.setOnClickListener(this)
        binding.selectMethodLayout.setOnClickListener(this)

        if (stripeCustomerId.isBlank()) {
            /*if stripe customer id is empty, then user has not done payment yet in our app
            * so we will show a card layout in this case*/
            binding.createMethodLayout.visibility = View.VISIBLE
            binding.selectMethodLayout.visibility = View.GONE
        } else {
            // User has already done a payment on our app, So we will show a select label
            // and user would be able to select already used cards
            binding.createMethodLayout.visibility = View.GONE
            binding.selectMethodLayout.visibility = View.VISIBLE

            vModel.generateEphemeralKey(stripeCustomerId)
        }

    }

    override fun getViewModel(): PaymentViewModel {
        vModel = ViewModelProvider(this).get(PaymentViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        when (v?.id) {
            R.id.btn_place_order -> {
                //callPlaceOrderApi()
                if (stripeCustomerId.isBlank() && vModel.selectedPaymentMethod == null) {
                    vModel.generatePaymentMethod(binding.cardWidget)
                } else {
                    if(selectedPaymentMethod == null || selectedPaymentMethod?.id == null) {
                        AppUtils.showToast(this, "Select Payment method first")
                    } else {
                        callPlaceOrderApi()
                    }
                }
            }
            R.id.select_method_layout -> {
                if (AppUtils.hasInternet(getApplication())) {
                    PaymentMethodsActivityStarter(this).startForResult()
                } else {
                    AppUtils.showToast(this, "No Internet")
                }
            }
        }
    }

    private fun callPlaceOrderApi() {

        if(addressId == -1) {
            AppUtils.showToast(this, "Go Back and select a Delivery address!")
            return
        }

        val cartAmount = appDatabase.cartAmountDao().getCartData() ?: return

        val params: HashMap<String, String> = HashMap()
        params.put(ApiContants.ADDRESS_ID, ""+addressId)
        params.put(ApiContants.PRODUCTS, vModel.getCartProducts())
        params.put(ApiContants.SUBTOTAL, cartAmount.subTotal.toString())
        params.put(ApiContants.REDEEM_POINTS, cartAmount.redeemPoints.toString())
        params.put(ApiContants.TOTAL_PAYABLE, cartAmount.newTotalAmount.toString())
        params.put(ApiContants.DELIVERY_PRICE, cartAmount.deliveryChargees.toString())

        if (stripeCustomerId.isBlank()) {
            params.put(ApiContants.PAYMENT_METHOD_ID, selectedPaymentMethod?.id!!)
        } else {
            params.put(ApiContants.PAYMENT_METHOD, selectedPaymentMethod?.id!!)
        }

        vModel.callPlaceOrderApi(params)
    }

    private val placeOrderResponseObserver = Observer<ResponseBody> {
        val json = it.string()
        try {
            val jsonObject = JSONObject(json)
            if (jsonObject.getBoolean("status")) {
                val result = jsonObject.getJSONObject("result")
                if (result.has("requires_action") && result.getBoolean("requires_action") == true) {
                    // If user entered/selected payment method requires 3D authentication ,
                    // then we have to perform that to complete the transaction
                    val clientSecret = result.getString("payment_intent_client_secret")
                    vModel.mStripe?.authenticatePayment(this@PaymentActivity, clientSecret!!)
                } else {
                    // Clearing entire cart data from Room db
                    vModel.clearCartData()
                    startActivity(WaveActivity.newInstance(this))
                    AppUtils.startFromRightToLeft(this)
                }
            } else {
                AppUtils.showSnackBar(binding.btnPlaceOrder.root, jsonObject.getString("message"))
            }
        } catch (e: Exception) {
            Log.e("JSON_ERROR", ""+e.message)
        }
    }

    private val placeOrderResponseObserverTwo = Observer<BaseResponse> {
        if (it.status) {
            // Clearing entire cart data from Room db
            vModel.clearCartData()
            startActivity(WaveActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
        } else {
            AppUtils.showSnackBar(binding.btnPlaceOrder.root, it.message)
        }
    }

    private val paymentMethodObserver = Observer<PaymentMethod> {
        selectedPaymentMethod = it
        callPlaceOrderApi()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PaymentMethodsActivityStarter.REQUEST_CODE && resultCode == RESULT_OK) run {
            selectedPaymentMethod = Objects.requireNonNull(
                PaymentMethodsActivityStarter.Result.fromIntent(data)
            )?.paymentMethod
            if (selectedPaymentMethod?.card != null) {
                // Note: only PaymentMethods of type 'card' are supported at this time
                Log.e("Payment Method", "Success")
                val selectedCard = selectedPaymentMethod!!.card
                //selectedPaymentMethod.card.
                binding.tvSelectPaymentMethod.setText(selectedCard!!.brand + " ending " + selectedCard.last4)
            }
        } else {
            vModel.start3DAuthenticationFlow(requestCode, data!!)
        }
    }

    private val threeDAuthResponseObserver = Observer<Stripe3DAuthResponse> {
        if (it.status) {
            if(addressId == -1) {
                AppUtils.showToast(this, "Go Back and select a Delivery address!")

            } else {
                val paymentIntentId = it.paymentIntentId

                val cartAmount = appDatabase.cartAmountDao().getCartData()

                if (cartAmount != null) {
                    val params: HashMap<String, String> = HashMap()
                    params.put(ApiContants.ADDRESS_ID, "" + addressId)
                    params.put(ApiContants.PRODUCTS, vModel.getCartProducts())
                    params.put(ApiContants.SUBTOTAL, cartAmount.subTotal.toString())
                    params.put(ApiContants.REDEEM_POINTS, cartAmount.redeemPoints.toString())
                    params.put(ApiContants.TOTAL_PAYABLE, cartAmount.newTotalAmount.toString())
                    params.put(ApiContants.DELIVERY_PRICE, cartAmount.deliveryChargees.toString())

                    params.put(ApiContants.PAYMENT_INTENT_ID, paymentIntentId)

                    vModel.callPlaceOrderApi(params)
                }
            }
        } else {
            AppUtils.showSimpleDialog(this, it.message)
        }
    }
}
