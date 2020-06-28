package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class PaymentAction(
    @SerializedName("requires_action")
    var requiresAction: Boolean? = null,
    @SerializedName("payment_intent_client_secret")
    var paymentIntentClientSecret: String? = null
)