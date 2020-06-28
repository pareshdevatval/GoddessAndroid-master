package com.krystal.goddesslifestyle.ui.shop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.CartAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.model.CartUpdateStatus
import com.krystal.goddesslifestyle.data.response.MyAddressListResponse
import com.krystal.goddesslifestyle.databinding.ActivityOrderReviewBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.WaveActivity
import com.krystal.goddesslifestyle.ui.activity.AddAddressActivity
import com.krystal.goddesslifestyle.ui.activity.MyAddressListActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.OrderReviewViewModel
import kotlinx.android.synthetic.main.activity_wave.view.*
import javax.inject.Inject

class OrderReviewActivity : BaseActivity<OrderReviewViewModel>(), View.OnClickListener,
    BaseBindingAdapter.ItemClickListener<Cart> {

    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, OrderReviewActivity::class.java)
            return intent
        }
    }

    /*ViewModel*/
    private lateinit var vModel: OrderReviewViewModel
    /*binding variable*/
    private lateinit var binding: ActivityOrderReviewBinding

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    // boolean indicating user needs to add address
    var requestedNewAdd = false

    // selected address
    var selectedAddress: MyAddressListResponse.Result? = null

    // redeempoints to be used on order
    var redeemPoints = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectOrderReviewActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_review)
        super.onCreate(savedInstanceState)

        vModel.setInjectable(apiService, prefs)
        vModel.setAppDatabase(appDatabase)

        //vModel.getPlaceOrderResponse().observe(this, placeOrderResponseObserver)
        vModel.getMyAddressListResponse().observe(this, myAddressListObsrver)

        vModel.callMyAddressListApi()

        init()
    }

    private fun init() {
        setToolbarTitle("Review")
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

        val lLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = lLayoutManager

        // Disabling the notifyItemChange default animations
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        val adapter = CartAdapter()
        adapter.itemClickListener = this

        val cartList = appDatabase.cartDao().getCartData()

        adapter.setItem(cartList as ArrayList<Cart>)
        binding.recyclerView.adapter = adapter

        vModel.getUpdateCartAcknowledgement().observe(this, cartUpdateAckObserver)

        //setCartTotalValues()

        binding.btnPlaceOrder.setOnClickListener(this)
        binding.tvChange.setOnClickListener(this)

        val user = prefs.userDataModel?.result
        if (user == null) {
            binding.cbRedeem.visibility = View.GONE
        } else {
            val points = user.uPoints
            if (points == null || points == 0) {
                binding.cbRedeem.visibility = View.GONE
            } else {

                val settings = prefs.settings

                if(settings.result != null && settings.result!!.convert_points_to_usd != null) {
                    // getting pointsToUsd from settings prefs
                    val pointsToUsd = settings.result!!.convert_points_to_usd!!
                    // getting divison of points/pointstoUSD into form of int
                    // this will give us the number of $ to deduct from the total amount
                    // (for ex. if user has 1700 points and pointsToUsd is 500 then
                    // 1700/500 will give 3 in form of an int.)
                    val division = points/(pointsToUsd.toInt())
                    // in above example 3 *500 (taken as pointsToUsd) will be our redeemPoints
                    redeemPoints = division * pointsToUsd.toInt()
                    if(redeemPoints >= pointsToUsd.toInt()) {
                        binding.cbRedeem.text = "Use " + redeemPoints + " points to this order"
                        binding.cbRedeem.visibility = View.VISIBLE
                    } else {
                        binding.cbRedeem.visibility = View.GONE
                    }
                } else {
                    binding.cbRedeem.visibility = View.GONE
                }

            }
        }
        //redeemPoints = 1000
        val cartTotal = appDatabase.cartAmountDao().getCartData()
        //binding.cbRedeem.isChecked = cartTotal?.useRedeemPoint!!
        binding.cbRedeem.isChecked = false

        binding.cbRedeem.setOnClickListener {
            if(binding.cbRedeem.isChecked) {
                //AppUtils.showToast(this@OrderReviewActivity, "Checked")
                val settings = prefs.settings
                if(settings.result != null && settings.result!!.convert_points_to_usd != null) {
                    // getting pointsToUsd from settings prefs
                    val pointsToUsd = settings.result!!.convert_points_to_usd!!

                    val priceToDecrease = redeemPoints/pointsToUsd.toInt()
                    val cartTotal = appDatabase.cartAmountDao().getCartData()
                    if(cartTotal != null) {
                        cartTotal.useRedeemPoint = true
                        cartTotal.redeemPoints = redeemPoints.toDouble()
                        val newTotal = cartTotal.totalAmount!! - priceToDecrease
                        cartTotal.newTotalAmount = newTotal
                        cartTotal.totalAmount = newTotal

                        appDatabase.cartAmountDao().insert(cartTotal)
                        //setCartTotalValues()

                        binding.tvSubtotal.text = AppUtils.getPriceWithCurrency(this, cartTotal.subTotal.toString())

                        cartTotal.deliveryChargees?.let {
                            if (it == 0.0) {
                                binding.tvDeliveryCharges.text = "-"
                            } else {
                                binding.tvDeliveryCharges.text =
                                    AppUtils.getPriceWithCurrency(this, cartTotal.deliveryChargees.toString())
                            }
                        }
                        binding.tvTotalAmount.text = AppUtils.getPriceWithCurrency(this, cartTotal.totalAmount.toString())
                    }
                }
            } else {
                //AppUtils.showToast(this@OrderReviewActivity, "UnChecked")

                val settings = prefs.settings
                // getting pointsToUsd from settings prefs
                val pointsToUsd = settings.result!!.convert_points_to_usd!!

                val priceToDecrease = redeemPoints/pointsToUsd.toInt()

                val cartTotal = appDatabase.cartAmountDao().getCartData()
                if(cartTotal != null) {
                    cartTotal.useRedeemPoint = false
                    cartTotal.redeemPoints = 0.0
                    cartTotal.newTotalAmount = cartTotal.totalAmount!! + priceToDecrease
                    cartTotal.totalAmount = cartTotal.totalAmount!! + priceToDecrease

                    appDatabase.cartAmountDao().insert(cartTotal)
                    //setCartTotalValues()

                    binding.tvSubtotal.text = AppUtils.getPriceWithCurrency(this, cartTotal.subTotal.toString())

                    cartTotal.deliveryChargees?.let {
                        if (it == 0.0) {
                            binding.tvDeliveryCharges.text = "-"
                        } else {
                            binding.tvDeliveryCharges.text =
                                AppUtils.getPriceWithCurrency(this, cartTotal.deliveryChargees.toString())
                        }
                    }
                    binding.tvTotalAmount.text = AppUtils.getPriceWithCurrency(this, cartTotal.totalAmount.toString())
                }
            }
        }
    }

    override fun onItemClick(view: View, data: Cart, position: Int) {
        when (view.id) {
            R.id.iv_minus -> {
                vModel.changeQuantity(data, AppConstants.DECREASE_QUANTITY, position, selectedAddress)
            }
            R.id.iv_plus -> {
                vModel.changeQuantity(data, AppConstants.INCREASE_QUANTITY, position, selectedAddress)
            }
            R.id.iv_delete -> {
                vModel.deleteItemFromCart(data, position, selectedAddress)
            }
        }
    }

    // setting cart Total values based on quantity change or address change
    private fun setCartTotalValues() {
        val cartTotal = appDatabase.cartAmountDao().getCartData() ?: return

        if(selectedAddress == null) {
            binding.tvDeliveryCharges.text = "-"
        } else {
            // getting delivery charge based on selected address
            val deliveryCharge = AppUtils.getDeliveryCharges(this, cartTotal.subTotal!!,
                selectedAddress!!.uaCountry)
            val totalAmount = cartTotal.subTotal!! + deliveryCharge

            cartTotal.deliveryChargees = deliveryCharge
            cartTotal.totalAmount = totalAmount
            cartTotal.newTotalAmount = totalAmount

            appDatabase.cartAmountDao().insert(cartTotal)
        }

        binding.tvSubtotal.text = AppUtils.getPriceWithCurrency(this, cartTotal.subTotal.toString())

        cartTotal.deliveryChargees?.let {
            if (it == 0.0) {
                binding.tvDeliveryCharges.text = "-"
            } else {
                binding.tvDeliveryCharges.text =
                    AppUtils.getPriceWithCurrency(this, cartTotal.deliveryChargees.toString())
            }
        }
        binding.tvTotalAmount.text = AppUtils.getPriceWithCurrency(this, cartTotal.totalAmount.toString())
    }

    val cartUpdateAckObserver = Observer<CartUpdateStatus> {
        if (it.success) {
            if (it.updateType == AppConstants.UPDATE_TYPE_CHANGE_ITEM) {
                (binding.recyclerView.adapter as CartAdapter).updateItem(it.position, it.cartItem)
            } else if (it.updateType == AppConstants.UPDATE_TYPE_DELETE_ITEM) {
                (binding.recyclerView.adapter as CartAdapter).removeItem(it.position)

                if ((binding.recyclerView.adapter as CartAdapter).itemCount == 0) {
                    onBackPressed()
                }
            }
            setCartTotalValues()
        } else {
            AppUtils.showToast(this, "Problem occurred while updating the data!")
        }

    }

    override fun getViewModel(): OrderReviewViewModel {
        vModel = ViewModelProvider(this).get(OrderReviewViewModel::class.java)
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
                if (selectedAddress == null) {
                    AppUtils.showToast(this, "Select Address first");
                } else {
                    if (AppUtils.hasInternet(this)) {
                        startActivity(PaymentActivity.newInstance(this, selectedAddress?.uaId!!))
                        AppUtils.startFromRightToLeft(this)
                    } else {
                        AppUtils.showToast(this, "No Internet");
                    }
                }
            }
            R.id.tv_change -> {
                //AppUtils.showToast(this, "Under Development")
            }
        }
    }

    private val placeOrderResponseObserver = Observer<BaseResponse> {
        if (it.status) {

            // Clearing entire cart data from Room db
            vModel.clearCartData()

            startActivity(WaveActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
        } else {
            AppUtils.showSnackBar(binding.btnPlaceOrder.root, it.message)
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == AppConstants.REQUEST_CODE_ORDER) {
            AppUtils.finishActivity(this)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == AppConstants.SET_ADDRESS_REQUEST_CODE) {

            // If user was redirected to add new address, then we will call an API to get that address
            if (requestedNewAdd) {
                vModel.callMyAddressListApi()
            } else {
                // otherwise we will get selected address in return
                val address = data?.getParcelableExtra<MyAddressListResponse.Result>(AppConstants.SELECTED_ADDRESS)
                setAddress(address)
                setCartTotalValues()
            }
        }
    }

    private val myAddressListObsrver = Observer<MyAddressListResponse> {
        if (it.status && it.result.isNotEmpty()) {
            val addresses = it.result
            binding.tvChange.text = "CHANGE"
            val firstAaddress = addresses[0]
            setAddress(firstAaddress)


            binding.tvChange.setOnClickListener {
                requestedNewAdd = false
                startActivityForResult(
                    MyAddressListActivity.newInstance(this, AppConstants.FROM_ORDER),
                    AppConstants.SET_ADDRESS_REQUEST_CODE
                )
                AppUtils.startFromRightToLeft(this)
            }

        } else {
            binding.tvChange.text = "ADD"
            binding.tvAddress.text = ""
            binding.tvChange.setOnClickListener {
                requestedNewAdd = true
                startActivityForResult(AddAddressActivity.newInstance(this), AppConstants.SET_ADDRESS_REQUEST_CODE)
                AppUtils.startFromRightToLeft(this)
            }
        }
        setCartTotalValues()
    }

    private fun setAddress(address: MyAddressListResponse.Result?) {
        selectedAddress = address
        val address = address!!.uaHouseNo + ", " + address!!.uaRoadAreaColony + ", " +
                address.uaCity + ", " + address.uaState +  ", " + address.uaCountry + " - " + address.uaPinCode
        binding.tvAddress.text = address
    }
}
