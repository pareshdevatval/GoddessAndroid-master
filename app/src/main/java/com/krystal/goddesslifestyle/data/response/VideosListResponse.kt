package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class VideosListResponse(
    @SerializedName("result")
    var videos: List<Video?>? = null) : BaseResponse()