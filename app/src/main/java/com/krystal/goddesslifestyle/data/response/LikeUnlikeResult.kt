package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class LikeUnlikeResult(
    @SerializedName("likes_count")
    var likesCount: Int? = null,
    @SerializedName("already_liked")
    var alreadyLiked: Boolean? = null
)