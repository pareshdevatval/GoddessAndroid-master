package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class DeliveryCharge(
    @SerializedName("min_order_value")
    var minOrderValue: Int? = null,
    @SerializedName("max_order_value")
    var maxOrderValue: Int? = null,
    @SerializedName("local_delivery_charge")
    var localDeliveryCharge: Int? = null,
    @SerializedName("international_delivery_charge")
    var internationalDeliveryCharge: Int? = null
)