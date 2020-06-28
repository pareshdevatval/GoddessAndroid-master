package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class RegistrationResponse(
    @SerializedName("result")
    var result: Registration? = null
) : BaseResponse()