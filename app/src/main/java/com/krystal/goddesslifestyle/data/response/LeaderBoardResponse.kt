package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class LeaderBoardResponse(
    @SerializedName("result")
    val result: List<Result>
):BaseResponse() {
    data class Result(
        @SerializedName("u_id")
        val uId: Int,
        @SerializedName("u_user_name")
        val uUserName: String,
        @SerializedName("u_image")
        val uImage: String,
        @SerializedName("total_point")
        val totalPoint: String
    )
}