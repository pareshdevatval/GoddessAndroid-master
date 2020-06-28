package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("vl_id")
    var vlId: Int? = null,
    @SerializedName("vl_title")
    var vlTitle: String? = null,
    @SerializedName("vl_image")
    var vlImage: String? = null,
    @SerializedName("vl_video")
    var vlVideo: String? = null
)