package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class AppSettings(
    @SerializedName("stripe_public_key")
    var stripePublicKey: String? = null,
    @SerializedName("stripe_secret_key")
    var stripeSecretKey: String? = null,
    @SerializedName("order_delivery_charge")
    var deliveryCharges: List<DeliveryCharge>? = null,
    @SerializedName("convert_points_to_usd")
    var pointsToUsd: String? = null

)