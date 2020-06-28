package com.krystal.goddesslifestyle.ui.shop

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ShopAdapter
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.CartProduct
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.data.response.ShopResponse
import com.krystal.goddesslifestyle.databinding.FragmentRecyclerviewBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.AppUtilsJava
import com.krystal.goddesslifestyle.viewmodels.ShopViewModel
import javax.inject.Inject

/**
 * Created by Bhargav Thanki on 06 April,2020.
 */
class ShopFragment : BaseFragment<ShopViewModel>(),
    BaseBindingAdapter.ItemClickListener<Product> {

    private lateinit var viewModel: ShopViewModel
    private lateinit var binding: FragmentRecyclerviewBinding

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var appDatabase: AppDatabase

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): ShopFragment {
            val bundle = Bundle()
            val fragment = ShopFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectShopFragment(this)

        binding = FragmentRecyclerviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        viewModel.setInjectable(apiService, prefs)
        viewModel.setAppDatabase(appDatabase)
        viewModel.getShopResponse().observe(viewLifecycleOwner, shopResponseObserver)
        viewModel.getAddToCartAcknowledgement().observe(viewLifecycleOwner, addProductAckObserver)
        viewModel.getDeleteCartItemAcknowledgement().observe(viewLifecycleOwner, removeProductAckObserver)
        viewModel.callShopApi()
    }

    fun init() {
        (activity as MainActivity).changeBgColor(R.color.yellow)

        // toolbar title
        //setToolbarTitle(R.string.title_shop)
        binding.toolbarLayout.tvToolbarTitle.text = getString(R.string.title_shop)

        setUpSearch()

        //setCartItemCount(getCartItemsCount())

        binding.toolbarLayout.llToolbarCart.setOnClickListener {

            context?.let {
                val subscriptionStatus = AppUtils.getUserSubscription(it)
                if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                    AppUtils.startSubscriptionActivity(context)
                } else {
                    startActivityForResult(CartActivity.newInstance(it), AppConstants.REQUEST_CODE_ORDER)
                    AppUtils.startFromRightToLeft(it)
                }
            }
        }

        /*Searchview*/
        binding.toolbarLayout.llToolbarRight.setOnClickListener {
            binding.toolbarLayout.searchView.visibility = View.VISIBLE
            binding.toolbarLayout.mainView.visibility = View.GONE

            /*Inflating searchview with animation of Right to Left*/
            context?.let {
                val shake = AnimationUtils.loadAnimation(it, R.anim.shake)
                shake.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        binding.toolbarLayout.etSearch.requestFocus()
                        val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.toolbarLayout.etSearch, InputMethodManager.SHOW_IMPLICIT)
                    }

                    override fun onAnimationStart(p0: Animation?) {

                    }

                })

                binding.toolbarLayout.searchView.startAnimation(shake)
            }
        }

        /*Closing searchview*/
        binding.toolbarLayout.ivCloseSearch.setOnClickListener {
            binding.toolbarLayout.etSearch.setText("")

            activity?.let {
                AppUtils.hideKeyboard(it)
            }

            context?.let {
                val shake = AnimationUtils.loadAnimation(it, R.anim.return_shake)

                shake.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        binding.toolbarLayout.searchView.visibility = View.GONE
                        binding.toolbarLayout.mainView.visibility = View.VISIBLE
                    }

                    override fun onAnimationStart(p0: Animation?) {
                    }
                })

                binding.toolbarLayout.searchView.startAnimation(shake)
            }
        }

        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context!!, R.color.yellow)
        }

        val staggeredLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredLayoutManager

        // Disabling the notifyItemChange default animations
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        val adapter = ShopAdapter()
        adapter.filterable = true

        binding.recyclerView.adapter = adapter

        adapter.itemClickListener = this
    }

    // setting an item count on the cart
    private fun setCartItemCount(count: Int) {
        if (count > 0) {
            // If count >0 , then show the count otherwise hide it
            binding.toolbarLayout.tvCartCount.text = count.toString()
            binding.toolbarLayout.tvCartCount.visibility = View.VISIBLE
            binding.toolbarLayout.llToolbarCart.layoutParams.width = AppUtilsJava.getPixelsFromDp(context!!, 35f).toInt()
            //binding.toolbarLayout.llToolbarCart.layoutParams.width = context!!.resources.getDimensionPixelSize(R.dimen._30sdp)
            binding.toolbarLayout.llToolbarCart.requestLayout()
        } else {
            binding.toolbarLayout.tvCartCount.text = "0"
            binding.toolbarLayout.tvCartCount.visibility = View.GONE
            binding.toolbarLayout.llToolbarCart.layoutParams.width = AppUtilsJava.getPixelsFromDp(context!!, 30f).toInt()
            //binding.toolbarLayout.llToolbarCart.layoutParams.width = context!!.resources.getDimensionPixelSize(R.dimen._25sdp)
            binding.toolbarLayout.llToolbarCart.requestLayout()
        }
    }

    var mLastClickTime = 0L
    override fun onItemClick(view: View, data: Product, position: Int) {
        /*if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()*/
        context?.let {
            val subscriptionStatus = AppUtils.getUserSubscription(it)
            if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                AppUtils.startSubscriptionActivity(context)
            } else {
                when (view.id) {
                    R.id.iv_cart -> {
                        if(viewModel.isItemAlreadyInCart(data)) {
                            viewModel.deleteItemFromCart(data, position)
                            setCartItemCount(getCartItemsCount())
                        } else {
                            viewModel.addProductToCart("1", data, position)
                            setCartItemCount(getCartItemsCount())
                        }
                    }
                    else -> {
                        context?.let {
                            startActivityForResult(
                                ProductDetailsActivity.newInstance(it, data),
                                AppConstants.REQUEST_CODE_ORDER
                            )
                            AppUtils.startFromRightToLeft(it)
                        }
                    }
                }
            }
        }
    }

    override fun getViewModel(): ShopViewModel {
        viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private val shopResponseObserver = Observer<ShopResponse>() {
        if (it.status) {
            val products = it.products
            (binding.recyclerView.adapter as ShopAdapter).setItem(products as ArrayList<Product>)
        }
    }

    private fun getCartItemsCount(): Int {
        return appDatabase.cartDao().getCartData().size
    }

    override fun onResume() {
        super.onResume()
        if(binding.recyclerView.adapter != null) {
            (binding.recyclerView.adapter as ShopAdapter).notifyDataSetChanged()
        }
        setCartItemCount(getCartItemsCount())
    }

    private fun setUpSearch() {
        binding.toolbarLayout.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                (binding.recyclerView.adapter as ShopAdapter).filter(p0.toString())

                if(p0.toString().isEmpty()) {
                    (binding.recyclerView.adapter as ShopAdapter).notifyDataSetChanged()
                }
            }
        })
    }

    private val addProductAckObserver = Observer<CartProduct> {
        if(it.success) {
            //val items = (binding.recyclerView.adapter as ShopAdapter).items
            (binding.recyclerView.adapter as ShopAdapter).updateItem(it.position, it.product!!)
            context?.let {
                AppUtils.showToast(it, "Added to the Cart Successfully")
            }
        } else {
            context?.let {
                AppUtils.showToast(it, "Problem occurred while adding the product!")
            }
        }
    }

    private val removeProductAckObserver = Observer<CartProduct> {
        if(it.success) {
            (binding.recyclerView.adapter as ShopAdapter).updateItem(it.position, it.product!!)
            context?.let {
                AppUtils.showToast(it, "Removed from Cart Successfully")
            }
        } else {
            context?.let {
                AppUtils.showToast(it, "Problem occurred while removing the product!")
            }
        }
    }
}