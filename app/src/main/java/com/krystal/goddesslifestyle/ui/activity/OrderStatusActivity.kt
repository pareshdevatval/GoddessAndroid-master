package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.OrderStatusAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.TrackData
import com.krystal.goddesslifestyle.data.response.OrderListResponse
import com.krystal.goddesslifestyle.databinding.ActivityOrderStatusBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.viewmodels.OrderStatusModel
import java.text.SimpleDateFormat
import javax.inject.Inject

class OrderStatusActivity : BaseActivity<OrderStatusModel>() {

    private lateinit var binding: ActivityOrderStatusBinding
    private lateinit var mViewModel: OrderStatusModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    override fun getViewModel(): OrderStatusModel {
        mViewModel = ViewModelProvider(this).get(OrderStatusModel::class.java)
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
        requestsComponent.injectOrderStatusActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_status)
        init()
    }

    fun init() {
        orderListResponse = intent.getParcelableExtra(AppConstants.ORDER_RESPONSE)!!

        setToolBar(
            getString(R.string.lbl_order_status) + "" + orderListResponse.orderId,
            R.color.yellow
        )
        binding.rvOrderStatus.layoutManager = LinearLayoutManager(this)
        val orderStatusAdapter = OrderStatusAdapter()
        binding.rvOrderStatus.adapter = orderStatusAdapter
        setTrackData()
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


    private fun setTrackData() {
        val list: ArrayList<TrackData?> = ArrayList()
        if (orderListResponse.orderItemTracking.size == 1) {
            val orderTrackData1 = orderListResponse.orderItemTracking[0]
            list.add(
                TrackData(
                    true,
                    orderTrackData1!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        false
                    ) + " - Your order got confirmed \n" + getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        true
                    ),
                    false
                )
            )
            list.add(TrackData(false, "Preparing", "", false))
            list.add(TrackData(false, "Ready", "", false))
            list.add(TrackData(false, "Dispatch", "", false))
            list.add(TrackData(false, "Delivered!!!", "", false))
        } else if (orderListResponse.orderItemTracking.size == 2) {
            val orderTrackData1 = orderListResponse.orderItemTracking[0]
            val orderTrackData2 = orderListResponse.orderItemTracking[1]
            list.add(
                TrackData(
                    true,
                    orderTrackData1!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        false
                    ) + " - Your order got confirmed \n" + getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData2!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        false
                    ) + " - We started preparing your order\n" + getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        true
                    ),
                    false
                )
            )
            list.add(TrackData(false, "Ready", "", false))
            list.add(TrackData(false, "Dispatch", "", false))
            list.add(TrackData(false, "Delivered!!!", "", false))

        } else if (orderListResponse.orderItemTracking.size == 3) {
            val orderTrackData1 = orderListResponse.orderItemTracking[0]
            val orderTrackData2 = orderListResponse.orderItemTracking[1]
            val orderTrackData3 = orderListResponse.orderItemTracking[2]
            list.add(
                TrackData(
                    true,
                    orderTrackData1!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        false
                    ) + " - Your order got confirmed \n" + getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData2!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        false
                    ) + " - We started preparing your order\n" + getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData3!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[2]!!.otCreatedAt,
                        false
                    ) + " -Your order is ready\n" + getDate(
                        orderListResponse.orderItemTracking[2]!!.otCreatedAt,
                        true
                    ),
                    false
                )
            )
            list.add(TrackData(false, "Dispatch", "", false))
            list.add(TrackData(false, "Delivered!!!", "", false))

        } else if (orderListResponse.orderItemTracking.size == 4) {
            val orderTrackData1 = orderListResponse.orderItemTracking[0]
            val orderTrackData2 = orderListResponse.orderItemTracking[1]
            val orderTrackData3 = orderListResponse.orderItemTracking[2]
            val orderTrackData4 = orderListResponse.orderItemTracking[3]
            list.add(
                TrackData(
                    true,
                    orderTrackData1!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        false
                    ) + " - Your order got confirmed \n" + getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData2!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        false
                    ) + " - We started preparing your order\n" + getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData3!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[2]!!.otCreatedAt,
                        false
                    ) + " -Your order is ready\n" + getDate(
                        orderListResponse.orderItemTracking[2]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData4!!.otStateName,
                    "" + getDate(orderListResponse.orderItemTracking[3]!!.otCreatedAt, true),
                    false
                )
            )
            list.add(TrackData(false, "Delivered!!!", "", false))

        } else if (orderListResponse.orderItemTracking.size == 5) {
            val orderTrackData1 = orderListResponse.orderItemTracking[0]
            val orderTrackData2 = orderListResponse.orderItemTracking[1]
            val orderTrackData3 = orderListResponse.orderItemTracking[2]
            val orderTrackData4 = orderListResponse.orderItemTracking[3]
            val orderTrackData5 = orderListResponse.orderItemTracking[4]
            list.add(
                TrackData(
                    true,
                    orderTrackData1!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        false
                    ) + " - Your order got confirmed \n" + getDate(
                        orderListResponse.orderItemTracking[0]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData2!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        false
                    ) + " - We started preparing your order\n" + getDate(
                        orderListResponse.orderItemTracking[1]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData3!!.otStateName,
                    getDate(
                        orderListResponse.orderItemTracking[2]!!.otCreatedAt,
                        false
                    ) + " -Your order is ready\n" + getDate(
                        orderListResponse.orderItemTracking[2]!!.otCreatedAt,
                        true
                    ),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData4!!.otStateName,
                    "" + getDate(orderListResponse.orderItemTracking[3]!!.otCreatedAt, true),
                    true
                )
            )
            list.add(
                TrackData(
                    true,
                    orderTrackData5!!.otStateName,
                    "To be delivered on " + getDate(
                        orderListResponse.orderItemTracking[4]!!.otCreatedAt,
                        true
                    ),
                    false
                )
            )

        }
        (binding.rvOrderStatus.adapter as OrderStatusAdapter).setItem(list)
        (binding.rvOrderStatus.adapter as OrderStatusAdapter).notifyItemChanged(0, list.size - 1)
    }

    fun getDate(
        orderCreatedAt: String, date: Boolean = true
    ): String {

        val curFormater = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val dateObj = curFormater.parse(orderCreatedAt);

        if (date) {
            val date = SimpleDateFormat("dd MMM yyyy")
            return date.format(dateObj)
        } else {
            val date = SimpleDateFormat("hh:mm a")
            return date.format(dateObj)
        }


    }
}
