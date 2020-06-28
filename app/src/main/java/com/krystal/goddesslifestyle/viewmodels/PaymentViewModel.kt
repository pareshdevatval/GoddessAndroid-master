package com.krystal.goddesslifestyle.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.Stripe3DAuthResponse
import com.krystal.goddesslifestyle.data.response.PlaceOrderResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import com.stripe.android.*
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardMultilineWidget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.*

/**
 * Created by imobdev on 21/2/20
 */
class PaymentViewModel(application: Application) : BaseViewModel(application) {

    val context = (getApplication() as GoddessLifeStyleApp).applicationContext

    private lateinit var appDatabase: AppDatabase

    /*RxJava Subscription object for api calling*/
    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    /*[START] Place order API*/
    private var subscription: Disposable? = null

    private val placeOrderResponse: MutableLiveData<ResponseBody> by lazy {
        MutableLiveData<ResponseBody>()
    }

    fun getPlaceOrderResponse(): LiveData<ResponseBody> {
        return placeOrderResponse
    }

    fun callPlaceOrderApi(params: HashMap<String, String>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiPlaceOrder3(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: ResponseBody) {
        placeOrderResponse.value = response
    }

    private fun handleRawResponse(response: Result<ResponseBody>) {

    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if (error is SocketTimeoutException) {
            AppUtils.showToast(
                getApplication(),
                getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out)
            )
        }
        //apiErrorMessage.value = error.localizedMessage
    }
    /*[END] Place order API*/

    // getting card products to send in order API request
    fun getCartProducts(): String {
        val productsArray = JSONArray()

        val cartItems = appDatabase.cartDao().getCartData()
        for (cart in cartItems) {
            val product = JSONObject()
            product.put("product_id", cart.productId)
            product.put("qty", cart.quantity)
            productsArray.put(product)
        }

        return productsArray.toString()
    }

    fun clearCartData() {
        appDatabase.cartDao().nukeTable()
        appDatabase.cartProductDao().nukeTable()
        appDatabase.cartAmountDao().nukeTable()
    }

    /*[START] Payment through the Stripe code*/

    var selectedPaymentMethod: PaymentMethod? = null

    private val paymentMethodLiveData: MutableLiveData<PaymentMethod> by lazy {
        MutableLiveData<PaymentMethod>()
    }

    fun getPaymentMethod(): LiveData<PaymentMethod> {
        return paymentMethodLiveData
    }

    val isFirstApiCall = true
    var paymentIntentId: String? = null

    var mStripe: Stripe? = null

    //
    fun initStripeSCA() {
        val uiCustomization =
            PaymentAuthConfig.Stripe3ds2UiCustomization.Builder().build()

        PaymentAuthConfig.init(
            PaymentAuthConfig.Builder()
                .set3ds2Config(
                    PaymentAuthConfig.Stripe3ds2Config.Builder()
                        // set a 5 minute timeout for challenge flow
                        .setTimeout(5)
                        // customize the UI of the challenge flow
                        .setUiCustomization(uiCustomization)
                        .build()
                )
                .build()
        )
        //AppUtils.showToast(getApplication(), prefsObj.stripePublicKey)
        PaymentConfiguration.init(context, prefsObj.stripePublicKey)
        mStripe = Stripe(
            context,
            PaymentConfiguration.getInstance(context).publishableKey
        )

    }

    // Generating ephemeral key
    fun generateEphemeralKey(stripeCustomerId: String) {
        // showing progress
        onApiStart()

        CustomerSession.initCustomerSession(context,
            object : EphemeralKeyProvider {
                @SuppressLint("CheckResult")
                override fun createEphemeralKey(apiVersion: String, keyUpdateListener: EphemeralKeyUpdateListener) {
                    val apiParamMap = java.util.HashMap<String, String>()
                    apiParamMap["api_version"] = apiVersion
                    apiParamMap["customer_id"] = stripeCustomerId

                    apiServiceObj.getEphemeralKey(apiParamMap).enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            try {
                                onApiFinishPost()
                                keyUpdateListener.onKeyUpdate(response.body()!!.string())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            onApiFinishPost()
                            Log.e("EPHEMERAL_ERROR", ""+t.message)
                        }
                    })
                }
            })
    }
    /*[END] Payment through the Stripe code*/


    // Generating payment method for first time payment
    fun generatePaymentMethod(card_widget: CardMultilineWidget) {
        onApiStart()

        if (card_widget.card == null) {
            onApiFinish()
            AppUtils.showToast(context, "Card details are not valid")
            return
        }
        val paymentMethodCreateParamsCard = card_widget.card!!.toPaymentMethodParamsCard()
        val paymentMethodCreateParams = PaymentMethodCreateParams.create(paymentMethodCreateParamsCard)

        if (mStripe == null) {
            onApiFinish()
            AppUtils.showToast(context, "Something went wrong. Please try again")
            return
        }

        mStripe!!.createPaymentMethod(
            paymentMethodCreateParams,
            UUID.randomUUID().toString(),
            object : ApiResultCallback<PaymentMethod> {
                override fun onError(e: Exception) {
                    Log.e("STRIPE_ERROR", "" + e.message)
                    AppUtils.showToast(context, e.message!!)
                    onApiFinish()
                }

                override fun onSuccess(result: PaymentMethod) {
                    onApiFinish()
                    selectedPaymentMethod = result
                    paymentMethodLiveData.value = selectedPaymentMethod
                }
            })


    }

    private val threeDAuthResponse: MutableLiveData<Stripe3DAuthResponse> by lazy {
        MutableLiveData<Stripe3DAuthResponse>()
    }

    fun get3dAuthResponse(): LiveData<Stripe3DAuthResponse> {
        return threeDAuthResponse
    }

    // starting 3D authentication flow
    fun start3DAuthenticationFlow(requestCode: Int, data: Intent) {
        onApiStart()
        mStripe?.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onError(e: Exception) {
                onApiFinish()
                AppUtils.showToast(context, ""+e.message)
            }

            override fun onSuccess(result: PaymentIntentResult) {
                onApiFinish()
                val paymentIntent = result.intent
                val paymentIntentId = paymentIntent.id
                val status = paymentIntent.status

                if(status == StripeIntent.Status.Succeeded) {
                    val stripe3dResponse = Stripe3DAuthResponse(true, paymentIntentId!!)
                    threeDAuthResponse.value = stripe3dResponse
                } else if(status == StripeIntent.Status.RequiresPaymentMethod) {

                    val stripe3dResponse = Stripe3DAuthResponse(false, "", "Authenticate again or use different Payment method")
                    threeDAuthResponse.value = stripe3dResponse
                    //AppUtils.showSimpleDialog(context, "Authenticate again or use different Payment method")

                } else if(status == StripeIntent.Status.RequiresAction) {

                    val stripe3dResponse = Stripe3DAuthResponse(false, "", "Authenticate again or use different Payment method")
                    threeDAuthResponse.value = stripe3dResponse
                    //AppUtils.showSimpleDialog(context, "Authenticate again or use different Payment method")

                } else if(status == StripeIntent.Status.RequiresConfirmation) {

                    val stripe3dResponse = Stripe3DAuthResponse(true, paymentIntentId!!)
                    threeDAuthResponse.value = stripe3dResponse
                }
            }
        })
    }
}