package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class ForgetPasswordResponse(
    @SerializedName("result")
    var result: Result? = null
): BaseResponse()