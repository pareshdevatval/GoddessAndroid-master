package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class LikeOpinionResponse(
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("status")
    var status: Boolean? = null,
    @SerializedName("result")
    var result: LikeUnlikeResult? = null
)