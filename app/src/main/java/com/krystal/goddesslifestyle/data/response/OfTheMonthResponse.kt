package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class OfTheMonthResponse(
    @SerializedName("result")
    var result: OfTheMonth? = null
) : BaseResponse()