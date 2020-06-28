package com.krystal.goddesslifestyle.ui.shop

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.viewpager.widget.ViewPager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewpagerImagesAdapter
import com.krystal.goddesslifestyle.adapter.ViewpagerImagesAdapterWrapContent
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.databinding.ActivityProductDetailsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.ProductDetailsViewModel
import javax.inject.Inject

class ProductDetailsActivity : BaseActivity<ProductDetailsViewModel>(), View.OnClickListener {

    companion object {
        /*Here, product is the selected product nad its pacelable object*/
        fun newInstance(context: Context, product: Product): Intent {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_PRODUCT, product)
            return intent
        }

        /*variables to decice increment/decrement of Quantity*/
        const val INCREASE_QUANTITY = 1
        const val DECREASE_QUANTITY = -1

        /*These 2 variables are used to detect the click,
        * If Buy Now is clicked then we will open a Review screen
        * and if ADD_TO_CART is clicked then will open cart screen*/
        const val BUY_NOW_CLICKED = "buyNowClicked"
        const val ADD_TO_CART_CLICKED = "addToCartClicked"
    }

    /*ViewModel*/
    private lateinit var vModel: ProductDetailsViewModel
    /*binding variable*/
    private lateinit var binding: ActivityProductDetailsBinding

    var product: Product? = null

    var clicked: String = ""

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
        requestsComponent.injectProductDetailsActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_details)
        super.onCreate(savedInstanceState)

        binding.activity = this
        vModel.setAppDatabase(appDatabase)
        init()
    }

    private fun init() {
        setToolbarTitle("")
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
        setData()
        vModel.getAddToCartAcknowledgement().observe(this, addProductAckObserver)
    }

    private fun setData() {
        intent?.let {
            product = it.getParcelableExtra(AppConstants.EXTRA_PRODUCT)

            product?.let {
                it.productTitle?.let {
                    setToolbarTitle(""+it)
                }
                binding.tvProductName.text = it.productTitle
                binding.tvProductDescription.text = it.productDescription
                binding.tvPrice.text = AppUtils.getPriceWithCurrency(this, it.productPrice)

                setUpViewPager()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        product?.let {
            val cartItem = appDatabase.cartDao().getCartItem(it.productId!!)
            // setting quantity
            cartItem?.let {
                binding.tvQuantity.text = it.quantity.toString()
            }
        }
    }

    // viewpager for images
    private fun setUpViewPager() {
        if(product == null) {
            return
        }
        val imagesList: ArrayList<String> = ArrayList()
        product?.media?.let {
            if (it.isNotEmpty()) {
                for(media in it) {
                    media.let { med ->
                        imagesList.add(med.piMedia!!)
                    }
                }
            }
        }
        val adapter = ViewpagerImagesAdapterWrapContent(this, imagesList)
        binding.viewPager.adapter = adapter
        binding.vpIndicator.setViewPager(binding.viewPager)
    }

    override fun getViewModel(): ProductDetailsViewModel {
        vModel = ViewModelProvider(this).get(ProductDetailsViewModel::class.java)
        return vModel
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        when(v?.id) {
            R.id.btn_buy_now -> {
                clicked = BUY_NOW_CLICKED
                vModel.addProductToCart(binding.tvQuantity.text.toString(), product)
            }
            R.id.btn_add_to_cart -> {
                clicked = ADD_TO_CART_CLICKED
                vModel.addProductToCart(binding.tvQuantity.text.toString(), product)
            }
            R.id.iv_plus -> {
                changeQuantity(INCREASE_QUANTITY)
            }
            R.id.iv_minus -> {
                changeQuantity(DECREASE_QUANTITY)
            }
        }
    }

    // change the quantity
    private fun changeQuantity(increseDecrease: Int) {
        // get current quantity
        var currentQuantity = binding.tvQuantity.text.toString().toInt()
        /*increase or decrease based on current qty*/
        if(increseDecrease == INCREASE_QUANTITY) {
            currentQuantity += 1
        } else if(increseDecrease == DECREASE_QUANTITY) {
            if(currentQuantity != 1) {
                currentQuantity -= 1
            }
        }
        binding.tvQuantity.text = currentQuantity.toString()
    }

    private val addProductAckObserver = Observer<Boolean> {
        if(it) {
            if(clicked.isNotBlank()) {
                if(clicked.equals(ADD_TO_CART_CLICKED, true)) {
                    startActivity(CartActivity.newInstance(this))
                    AppUtils.startFromRightToLeft(this)
                } else if(clicked.equals(BUY_NOW_CLICKED, true)) {
                    if(prefs.isLoggedIn) {
                        startActivity(OrderReviewActivity.newInstance(this))
                        AppUtils.startFromRightToLeft(this)
                    } else {
                        startActivity(LoginActivity.newInstance(this, AppConstants.FROM_CART))
                        AppUtils.startFromRightToLeft(this)
                    }
                }
            } else {
                AppUtils.showToast(this, "Problem occurred!")
            }

        } else {
            AppUtils.showToast(this, "Problem occurred while adding the product!")
        }
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
}
