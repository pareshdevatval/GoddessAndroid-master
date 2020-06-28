package com.krystal.goddesslifestyle.data.response

import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

/**
 * Created by imobdev on 9/4/20
 */
data class GoddessCommunityCountResponse(
    @SerializedName("data") val countData: CountData
):BaseResponse()

data class CountData(
    val total_community_opinions_count: Int
)