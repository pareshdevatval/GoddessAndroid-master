package com.krystal.goddesslifestyle.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.OrderListAdapter

import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.CartProduct
import com.krystal.goddesslifestyle.data.response.OrderListResponse
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.data.response.ShopResponse
import com.krystal.goddesslifestyle.databinding.ActivityOrderListBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.shop.CartActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.OrderListModel
import javax.inject.Inject

class OrderListActivity : BaseActivity<OrderListModel>(),
    BaseBindingAdapter.ItemClickListener<OrderListResponse.Result?> {


    private lateinit var binding: ActivityOrderListBinding
    private lateinit var mViewModel: OrderListModel

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    val orderProductsMap: HashMap<Int, Int> = HashMap()

    var mTimeFilter = 0
    var mOrderFilter = 0

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, OrderListActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): OrderListModel {
        mViewModel = ViewModelProvider(this).get(OrderListModel::class.java)
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
        requestsComponent.injectOrderListActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_list)
        mViewModel.setInjectable(apiService, prefs)
        init()
    }

    fun init() {
        binding.rvOrderList.layoutManager = LinearLayoutManager(this)
        val orderListAdapter = OrderListAdapter()
        binding.rvOrderList.adapter = orderListAdapter
        orderListAdapter.itemClickListener = this

        mViewModel.setAppDatabase(appDatabase)

        mViewModel.getOrderListResponse().observe(this, orderListObserve)

        mViewModel.getShopResponse().observe(this, shopResponseObserver)
        mViewModel.getAddToCartAcknowledgement().observe(this, addProductAckObserver)

        mViewModel.callOrderListApi()
        setToolBar(getString(R.string.lbl_order_list), R.color.yellow)
        loadMore()

        binding.viewNoNotification.btnNoOrder.setOnClickListener {
            startActivity(MainActivity.newInstance(this, getString(R.string.title_shopping), false))
            GoddessAnimations.finishFromLeftToRight(this)
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

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_filter,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    startActivityForResult(
                        FilterActivity.newInstance(
                            this@OrderListActivity,
                            mTimeFilter,
                            mOrderFilter
                        ), 202
                    )
                }
            })

    }


    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvOrderList.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvOrderList.layoutManager as LinearLayoutManager,
                true
            ) {
            override fun loadMoreItems() {

                /*If we have load all total pages then no need to go for next*/
                if (TOTAL_PAGE != CURRENT_PAGE) {
                    /*checking for loading flag to prevent too many calls at same time*/
                    if (isLoading) {
                        /*switching the value of loading flag*/
                        isLoading = false
                        /*increment the current page count*/
                        CURRENT_PAGE += 1

                        /*Adding a dummy(null) item to the list to show a footer progress bar*/
                        (binding.rvOrderList.adapter as OrderListAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvOrderList.adapter as OrderListAdapter).itemCount
                        (binding.rvOrderList.adapter as OrderListAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvOrderList.adapter as OrderListAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callOrderListApi(CURRENT_PAGE, false)
                    }
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGE
            }

            override fun isLastPage(): Boolean {
                return false
            }

            override fun isLoading(): Boolean {
                return false
            }

        })

    }

    var orderListResponse: OrderListResponse? = null
    private val orderListObserve = Observer<OrderListResponse> { response: OrderListResponse? ->
        if (response!!.status) {
            orderListResponse = response
            if (CURRENT_PAGE == 1) {
                TOTAL_PAGE = response.pagination.lastPage

                if (response.result.isNotEmpty()) {
                    binding.rvOrderList.visibility = View.VISIBLE
                    binding.llNoData.visibility = View.GONE
                    (binding.rvOrderList.adapter as OrderListAdapter).setItem(response.result)
                } else {
                    binding.rvOrderList.visibility = View.GONE
                    binding.llNoData.visibility = View.VISIBLE
                    AppUtils.startNotificationWaveAnimation(
                        this,
                        binding.viewNoNotification.wave,
                        R.color.yellow
                    )
                }
            } else {
                (binding.rvOrderList.adapter as OrderListAdapter).removeLoadingFooter(true)
                val adapterCount =
                    (binding.rvOrderList.adapter as OrderListAdapter).itemCount
                (binding.rvOrderList.adapter as OrderListAdapter).removeItem(
                    adapterCount - 1
                )


                val sizeList =
                    (binding.rvOrderList.adapter as OrderListAdapter).itemCount
                (binding.rvOrderList.adapter as OrderListAdapter).addItems(response.result)
                (binding.rvOrderList.adapter as OrderListAdapter).notifyItemRangeInserted(
                    sizeList,
                    response.result.size
                )
                isLoading = true
            }

        } else {
            AppUtils.showSnackBar(binding.root, response.message)
        }
        binding.tvLastOrder.text =
            "Last Order (" + (binding.rvOrderList.adapter as OrderListAdapter).items.size + ")"
    }


    override fun onItemClick(view: View, data: OrderListResponse.Result?, position: Int) {

        when (view.id) {
            R.id.viewViewOrderDetails -> {
                val intent = Intent(this@OrderListActivity, OrderDetailActivity::class.java)
                intent.putExtra(AppConstants.ORDER_RESPONSE, data)
                startActivity(intent)
            }
            R.id.viewTrackThisOrder -> {
                val intent = Intent(this@OrderListActivity, OrderStatusActivity::class.java)
                intent.putExtra(AppConstants.ORDER_RESPONSE, data)
                startActivity(intent)
            }
            R.id.viewBuyItAgain -> {
                buyItAgain(data)
            }
            R.id.viewEmailThisInvoice -> {
                ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setChooserTitle("Chooser title")
                    .setText("http://play.google.com/store/apps/details?id=$packageName")
                    .startChooser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 202) {
            if (resultCode == Activity.RESULT_OK) {
                mTimeFilter = data!!.getIntExtra("time", 0)
                mOrderFilter = data.getIntExtra("order", 0)

                Log.e("SELECTED_ORDER", "-->" + mOrderFilter)
                Log.e("SELECTED_ORDER", "-->" + mOrderFilter)
                Log.e("SELECTED_TIME", "-->" + mTimeFilter)
                (binding.rvOrderList.adapter as OrderListAdapter).clear()
                CURRENT_PAGE = 1
                mViewModel.callOrderListApi(1, true, mTimeFilter, mOrderFilter)
            }

        }
    }

    private fun buyItAgain(order: OrderListResponse.Result?) {
        order?.let {

            orderProductsMap.clear()
            mViewModel.clearCartData()

            val orderItems = order.orderItems
            for (orderItem in orderItems) {
                orderItem?.let {
                    val qty = orderItem.oiProductQty
                    val product = orderItem.productData

                    orderProductsMap.put(product[0]!!.productId, qty)
                }
            }

            mViewModel.callShopApi()

        }
    }


    private val shopResponseObserver = Observer<ShopResponse>() {
        if (it.status) {
            val products = it.products
            products?.let {
                for (product in products) {
                    product?.let {
                        if (orderProductsMap.containsKey(product.productId)) {
                            val productQty = orderProductsMap.get(product.productId)
                            mViewModel.addProductToCart("" + productQty, product, 0)
                        }
                    }
                }
            }

            startActivity(CartActivity.newInstance(this))
        }
    }

    private val addProductAckObserver = Observer<CartProduct> {
        if (!it.success) {
            AppUtils.showToast(this, "Problem occurred while adding the product!")
        }
    }
}
