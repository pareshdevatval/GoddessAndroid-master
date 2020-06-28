package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class ShopResponse(
    @SerializedName("result")
    var products: List<Product?>? = null
) : BaseResponse()