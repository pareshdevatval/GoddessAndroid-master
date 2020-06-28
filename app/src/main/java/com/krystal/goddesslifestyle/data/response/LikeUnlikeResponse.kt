package com.krystal.goddesslifestyle.data.response

import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

/**
 * Created by imobdev on 26/3/20
 */
data class LikeUnlikeResponse(
    @SerializedName("favourite") val favourite: Int
):BaseResponse()