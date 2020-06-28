package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.OrderDetailsAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.ProductModel
import com.krystal.goddesslifestyle.data.response.OrderListResponse
import com.krystal.goddesslifestyle.databinding.ActivityOrderDetailsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.viewmodels.OrderDetailModel
import java.text.SimpleDateFormat
import javax.inject.Inject

class OrderDetailActivity : BaseActivity<OrderDetailModel>() {

    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var mViewModel: OrderDetailModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, OrderDetailActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): OrderDetailModel {
        mViewModel = ViewModelProvider(this).get(OrderDetailModel::class.java)
        return mViewModel

    }

    override fun internetErrorRetryClicked() {

    }

    lateinit var orderListResponse: OrderListResponse.Result
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectOrderDetailActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_details)
        init()
    }

    fun init() {
        orderListResponse = intent.getParcelableExtra(AppConstants.ORDER_RESPONSE)

        setToolBar(getString(R.string.lbl_order_status)+orderListResponse.orderId, R.color.yellow)
        binding.tvItems.text=""+orderListResponse.orderItems.size+" ITEMS"
        binding.tvAmount.text="$"+orderListResponse.orderTotalPrice
        binding.tvDateAndPlace.text="Placed on "+getDate(orderListResponse.orderCreatedAt)
        if(orderListResponse.orderStatus==5){
            /*order delivered done ,so show status and delivered data*/
            binding.tvStatus.visibility= View.VISIBLE
            binding.carView.visibility=View.VISIBLE
            binding.tvDeliveredDate.text=getDate(orderListResponse.orderItemTracking[orderListResponse.orderItemTracking.size-1]!!.otCreatedAt,true)

        }else{
            /*doen't order delivered, so hide status and delivered data  */
            binding.carView.visibility=View.GONE
            binding.tvStatus.visibility= View.GONE
        }
        binding.rvOrderDetails.layoutManager = LinearLayoutManager(this)
        val orderStatusAdapter = OrderDetailsAdapter()
        binding.rvOrderDetails.adapter = orderStatusAdapter

        setAdepterData()
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

    }


    /*set order list data*/
    private fun setAdepterData() {

        val productModel: ArrayList<ProductModel?> = ArrayList()
        productModel.clear()
        for (i in 0 until orderListResponse.orderItems.size) {
            for (j in 0 until orderListResponse.orderItems[i]?.productData!!.size) {
                productModel.add(ProductModel(orderListResponse.orderItems[i]?.productData!![j]!!.productId,
                    orderListResponse.orderItems[i]?.productData!![j]!!.productTitle,
                    orderListResponse.orderItems[i]?.productData!![j]!!.productImage,
                    orderListResponse.orderItems[i]?.productData!![j]!!.productPrice,
                    (orderListResponse.orderItems[i]?.oiProductPrice!!.toDouble()*orderListResponse.orderItems[i]?.oiProductQty!!).toString(),
                    orderListResponse.orderItems[i]?.oiProductQty!!.toString()))
            }
        }

        (binding.rvOrderDetails.adapter as OrderDetailsAdapter).setItem(productModel)
        (binding.rvOrderDetails.adapter as OrderDetailsAdapter).notifyItemChanged(0, productModel.size - 1)
    }
    fun getDate(
        orderCreatedAt: String,isDay:Boolean=false
    ):String {

        val curFormater = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val dateObj = curFormater.parse(orderCreatedAt);

        if(!isDay){
            val date = SimpleDateFormat("dd MMM, hh:mm a")
            return  date.format(dateObj)
        }else{
            val date = SimpleDateFormat("EEE dd MMM, hh:mm a")
            return  date.format(dateObj)
        }


    }

}
