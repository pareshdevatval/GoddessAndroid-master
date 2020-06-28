package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class UserSubscriptionResponse(
    @SerializedName("result")
    var result: UserSubscription? = null
) : BaseResponse()