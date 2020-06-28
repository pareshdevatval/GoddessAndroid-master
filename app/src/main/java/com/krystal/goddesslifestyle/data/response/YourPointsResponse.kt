package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class YourPointsResponse(
    @SerializedName("pagination")
    val pagination: Pagination,
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
) {
    data class Pagination(
        @SerializedName("total")
        val total: Int,
        @SerializedName("lastPage")
        val lastPage: Int,
        @SerializedName("perPage")
        val perPage: String,
        @SerializedName("currentPage")
        val currentPage: Int
    )

    data class Result(
        @SerializedName("ua_id")
        val uaId: Int,
        @SerializedName("ua_type")
        val uaType: String,
        @SerializedName("ua_earned_points")
        val uaEarnedPoints: Int,
        @SerializedName("ua_created_at")
        val uaCreatedAt: String
    )
}