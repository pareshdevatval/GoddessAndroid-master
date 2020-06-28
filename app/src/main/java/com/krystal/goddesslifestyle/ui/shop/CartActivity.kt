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
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.model.CartUpdateStatus
import com.krystal.goddesslifestyle.databinding.ActivityCartBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.CartViewModel
import javax.inject.Inject

class CartActivity  : BaseActivity<CartViewModel>(), View.OnClickListener, BaseBindingAdapter.ItemClickListener<Cart> {


    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, CartActivity::class.java)
            return intent
        }


    }

    /*ViewModel*/
    private lateinit var vModel: CartViewModel
    /*binding variable*/
    private lateinit var binding: ActivityCartBinding

    @Inject
    lateinit var appDatabase: AppDatabase
    @Inject
    lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectCartActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        super.onCreate(savedInstanceState)

        vModel.setAppDatabase(appDatabase)

    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        setToolbarTitle("")
        //val
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

        if(cartList.isEmpty()) {
            // show No data view
            showEmptyCartView()
        } else {
            binding.cartLayout.visibility = View.VISIBLE
            binding.noDataLayout.root.visibility = View.GONE
        }

        vModel.getUpdateCartAcknowledgement().observe(this, cartUpdateAckObserver)

        setCartTotalValues()

        binding.btnCheckout.setOnClickListener(this)

    }

    /*Empty cart view*/
    private fun showEmptyCartView() {
        binding.cartLayout.visibility = View.GONE
        binding.noDataLayout.root.visibility = View.VISIBLE

        binding.noDataLayout.ivImage.setImageResource(R.drawable.ic_empty_cart)
        binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
        binding.noDataLayout.tvTitle.setTextColor(ContextCompat.getColor(this, R.color.yellow))
        binding.noDataLayout.tvTitle.text = getString(R.string.empty_cart_title)
        binding.noDataLayout.tvDesc.text = getString(R.string.empty_cart_msg)
        binding.noDataLayout.btnAction.text = getString(R.string.continue_shopping)
        binding.noDataLayout.btnAction.setOnClickListener(this)
        binding.noDataLayout.btnAction.setBackgroundResource(R.drawable.yellow_button)
        AppUtils.startWaveAnimation(this, binding.noDataLayout.wave, R.color.yellow)
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        when(v?.id) {
            R.id.btn_checkout -> {
                if(prefs.isLoggedIn) {
                    startActivityForResult(OrderReviewActivity.newInstance(this), AppConstants.REQUEST_CODE_ORDER)
                    AppUtils.startFromRightToLeft(this)
                } else {
                    startActivity(LoginActivity.newInstance(this, AppConstants.FROM_CART))
                    AppUtils.startFromRightToLeft(this)
                }
            }
            R.id.btn_action -> {
                startActivity(MainActivity.newInstance(this, getString(R.string.title_shopping), true))
                GoddessAnimations.finishFromLeftToRight(this)
            }
        }
    }

    override fun getViewModel(): CartViewModel {
        vModel = ViewModelProvider(this).get(CartViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == AppConstants.REQUEST_CODE_ORDER) {
            AppUtils.finishActivity(this)
        }
    }

    override fun onItemClick(view: View, data: Cart, position: Int) {
        when(view.id) {
            R.id.iv_minus -> {
                vModel.changeQuantity(data, AppConstants.DECREASE_QUANTITY, position)
            }
            R.id.iv_plus -> {
                vModel.changeQuantity(data, AppConstants.INCREASE_QUANTITY, position)
            }
            R.id.iv_delete -> {
                vModel.deleteItemFromCart(data, position)
            }
        }
    }

    private fun setCartTotalValues() {
        val cartItems = appDatabase.cartDao().getCartData()

        // setting title based on product count in the cart
        setToolbarTitle(String.format(getString(R.string.cart_title), cartItems.size))
        if(cartItems.isEmpty()) {
            showEmptyCartView()
        }

        val cartTotal = appDatabase.cartAmountDao().getCartData() ?: return

        binding.tvSubtotal.text = AppUtils.getPriceWithCurrency(this, cartTotal.subTotal.toString())
        cartTotal.deliveryChargees?.let {
            // If delivery charge is 0 then show FREE
            if(it == 0.0) {
                binding.tvDeliveryCharges.text = "FREE"
            } else {
                binding.tvDeliveryCharges.text = AppUtils.getPriceWithCurrency(this, cartTotal.deliveryChargees.toString())
            }
        }
        //binding.tvTotalAmount.text = AppUtils.getPriceWithCurrency(this, cartTotal.totalAmount.toString())
        binding.tvTotalAmount.text = AppUtils.getPriceWithCurrency(this, cartTotal.subTotal.toString())
    }

    val cartUpdateAckObserver = Observer<CartUpdateStatus> {
        if(it.success) {
            if(it.updateType == AppConstants.UPDATE_TYPE_CHANGE_ITEM) {
                (binding.recyclerView.adapter as CartAdapter).updateItem(it.position, it.cartItem)
            } else if(it.updateType == AppConstants.UPDATE_TYPE_DELETE_ITEM) {
                (binding.recyclerView.adapter as CartAdapter).removeItem(it.position)

            }
            setCartTotalValues()
        } else {
            AppUtils.showToast(this, "Problem occurred while updating the data!")
        }

    }
}
