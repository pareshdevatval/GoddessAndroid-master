package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class SettingResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
) {
    data class Result(
        @SerializedName("u_enable_practice_notification")
        val uEnablePracticeNotification: Int,
        @SerializedName("u_enable_community_notification")
        val uEnableCommunityNotification: Int,
        @SerializedName("u_enable_shopping_notification")
        val uEnableShoppingNotification: Int
    )
}