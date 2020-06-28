package com.krystal.goddesslifestyle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.response.OrderListResponse
import com.krystal.goddesslifestyle.databinding.ItemOrderListBinding
import com.krystal.goddesslifestyle.databinding.LayoutImagesAndTextviewThreeBinding
import com.krystal.goddesslifestyle.databinding.LoadMoreProgressBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import java.text.SimpleDateFormat

/**
 * Created by imobdev on 23/3/20
 */
class OrderListAdapter : BaseBindingAdapter<OrderListResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemOrderListBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding = holder.binding as ItemOrderListBinding
                val item = items[position]
                item?.let {

                    binding.tvItems.text = "" + item.orderItems.size + " items"
                    binding.tvPrice.text = "$" + item.orderTotalPrice
                    binding.tvOrderId.text = "#" + item.orderId
                    if (item.orderDeliveryPrice == "0.00" || item.orderDeliveryPrice == "0" || item.orderDeliveryPrice.isEmpty()) {
                        binding.tvFree.text = "FREE"
                    } else {
                        binding.tvFree.text = item.orderDeliveryPrice
                    }
                    if(item.orderItemTracking[item.orderItemTracking.size-1]?.otOrderState==1){
                        binding.viewTrackThisOrder.view.visibility= View.VISIBLE
                        binding.tvDeliveryStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.gray_1))
                        binding.tvDeliveryStatus.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black_1))
                        binding.tvDeliveryStatus.text=AppConstants.ORDER_RECEIVED+getDate(item.orderCreatedAt, binding,false)
                    }else if(item.orderItemTracking[item.orderItemTracking.size-1]?.otOrderState==2){
                        binding.viewTrackThisOrder.view.visibility= View.VISIBLE
                        binding.tvDeliveryStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.gray_1))
                        binding.tvDeliveryStatus.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black_1))
                        binding.tvDeliveryStatus.text=AppConstants.ORDER_PREPARING+getDate(item.orderCreatedAt, binding,false)
                    }else if(item.orderItemTracking[item.orderItemTracking.size-1]?.otOrderState==3){
                        binding.viewTrackThisOrder.view.visibility= View.VISIBLE
                        binding.tvDeliveryStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.gray_1))
                        binding.tvDeliveryStatus.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black_1))
                        binding.tvDeliveryStatus.text=AppConstants.ORDER_READY+getDate(item.orderCreatedAt, binding,false)
                    }else if(item.orderItemTracking[item.orderItemTracking.size-1]?.otOrderState==4){
                        binding.viewTrackThisOrder.view.visibility= View.VISIBLE
                        binding.tvDeliveryStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.gray_1))
                        binding.tvDeliveryStatus.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black_1))
                        binding.tvDeliveryStatus.text=AppConstants.ORDER_DISPATCH+getDate(item.orderCreatedAt, binding,false)
                    }else if(item.orderItemTracking[item.orderItemTracking.size-1]?.otOrderState ==5){
                         binding.tvDeliveryStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.green))
                         binding.tvDeliveryStatus.setTextColor(ContextCompat.getColor(binding.root.context,R.color.white))
                        binding.tvDeliveryStatus.text=AppConstants.ORDER_DELIVERED+getDate(item.orderCreatedAt, binding,false)
                        binding.viewTrackThisOrder.view.visibility= View.GONE
                    }

                    val imagesList: ArrayList<String?> = ArrayList()
                    imagesList.clear()
                    getDate(item.orderCreatedAt, binding,true)
                    for (i in 0 until item.orderItems.size) {
                        for (j in 0 until item.orderItems[i]?.productData!!.size) {
                            imagesList.add(item.orderItems[i]?.productData!![j]!!.productImage)
                        }
                    }
                    setAdepterData(binding, imagesList)
                    setLink(
                        binding.viewBuyItAgain,
                        binding.root.context.getString(R.string.lbl_by_item_again)
                    )
                    setLink(
                        binding.viewViewOrderDetails,
                        binding.root.context.getString(R.string.lbl_view_order_details)
                    )
                    setLink(
                        binding.viewTrackThisOrder,
                        binding.root.context.getString(R.string.lbl_tack_this_order)
                    )
                    setLink(
                        binding.viewEmailThisInvoice,
                        binding.root.context.getString(R.string.lbl_email_this_invoice)
                    )
                }
            }
            LOADING -> {

            }
        }

    }

    private fun setLink(binding: LayoutImagesAndTextviewThreeBinding, text: String) {
        binding.tvName.text = text

    }

    override
    fun getItemViewType(position: Int): Int {
        return if (position == items.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    fun removeLoadingFooter(isLast: Boolean = false) {
        isLoadingAdded = false
    }

    private fun setAdepterData(
        binding: ItemOrderListBinding,
        imagesList: ArrayList<String?>
    ) {
        val orderListImagesAdapter = OrderListImagesAdapter()
        binding.rvImages.adapter = orderListImagesAdapter


        (binding.rvImages.adapter as OrderListImagesAdapter).setItem(imagesList)
        (binding.rvImages.adapter as OrderListImagesAdapter).notifyDataSetChanged()
    }

    fun getDate(
        orderCreatedAt: String,
        binding: ItemOrderListBinding,isSetorderDate:Boolean
    ):String {

        val curFormater = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val dateObj = curFormater.parse(orderCreatedAt);

        if(isSetorderDate){
            val month = SimpleDateFormat("MMM")
            val day = SimpleDateFormat("dd")
            val year = SimpleDateFormat("yyyy")

            binding.tvMonth.text = month.format(dateObj)
            binding.tvDate.text = day.format(dateObj)
            binding.tvYear.text = year.format(dateObj)
            return ""
        }else{
            val date = SimpleDateFormat("dd MMM yyyy")
             return  date.format(dateObj)
        }
        return ""

    }
}