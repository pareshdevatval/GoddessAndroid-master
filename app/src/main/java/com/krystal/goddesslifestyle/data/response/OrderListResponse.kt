package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderListResponse (
    @SerializedName("pagination")
    val pagination: Pagination,
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
) : Parcelable {
    @Parcelize
    data class Pagination(
        @SerializedName("total")
        val total: Int,
        @SerializedName("lastPage")
        val lastPage: Int,
        @SerializedName("perPage")
        val perPage: Int,
        @SerializedName("currentPage")
        val currentPage: Int
    ):Parcelable
    @Parcelize
    data class Result(
        @SerializedName("order_id")
        val orderId: Int,
        @SerializedName("order_txn_id")
        val orderTxnId: String,
        @SerializedName("order_user_id")
        val orderUserId: Int,
        @SerializedName("order_ua_id")
        val orderUaId: Int,
        @SerializedName("order_total_price")
        val orderTotalPrice: String,
        @SerializedName("order_delivery_price")
        val orderDeliveryPrice: String,
        @SerializedName("order_used_points")
        val orderUsedPoints: Int,
        @SerializedName("order_payment_status")
        val orderPaymentStatus: String,
        @SerializedName("order_status")
        val orderStatus: Int,
        @SerializedName("order_created_at")
        val orderCreatedAt: String,
        @SerializedName("order_updated_at")
        val orderUpdatedAt: String,
        @SerializedName("order_items")
        val orderItems: ArrayList<OrderItem?>,
        @SerializedName("order_item_tracking")
        val orderItemTracking: ArrayList<OrderItemTracking?>

    ) : Parcelable{
        @Parcelize
        data class OrderItem(
            @SerializedName("oi_id")
            val oiId: Int,
            @SerializedName("oi_order_id")
            val oiOrderId: Int,
            @SerializedName("oi_product_id")
            val oiProductId: Int,
            @SerializedName("oi_product_qty")
            val oiProductQty: Int,
            @SerializedName("oi_product_price")
            val oiProductPrice: String,
            @SerializedName("oi_status")
            val oiStatus: Int,
            @SerializedName("oi_created_at")
            val oiCreatedAt: String,
            @SerializedName("oi_updated_at")
            val oiUpdatedAt: String,
            @SerializedName("product_data")
            val productData: ArrayList<ProductData?>

        ) : Parcelable{
            @Parcelize
            data class ProductData(
                @SerializedName("product_id")
                val productId: Int,
                @SerializedName("product_title")
                val productTitle: String,
                @SerializedName("product_description")
                val productDescription: String,
                @SerializedName("product_price")
                val productPrice: String,
                @SerializedName("product_image")
                val productImage: String,
                @SerializedName("product_status")
                val productStatus: Int,
                @SerializedName("product_created_at")
                val productCreatedAt: String,
                @SerializedName("product_updated_at")
                val productUpdatedAt: String
            ): Parcelable
        }
        @Parcelize
        data class OrderItemTracking(
            @SerializedName("ot_id")
            val otId: Int,
            @SerializedName("ot_order_id")
            val otOrderId: Int,
            @SerializedName("ot_order_state")
            val otOrderState: Int,
            @SerializedName("ot_status")
            val otStatus: Int,
            @SerializedName("ot_created_at")
            val otCreatedAt: String,
            @SerializedName("ot_updated_at")
            val otUpdatedAt: String,
            @SerializedName("ot_state_name")
            val otStateName: String

        ): Parcelable
    }
}