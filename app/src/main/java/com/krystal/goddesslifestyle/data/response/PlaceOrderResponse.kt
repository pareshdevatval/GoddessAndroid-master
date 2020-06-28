package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class PlaceOrderResponse(
    @SerializedName("result")
    var paymentAction: PaymentAction? = null
) : BaseResponse()