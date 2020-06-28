package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class GetNotificationListResponse(
    @SerializedName("pagination")
    val pagination: Pagination,
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("unread_count")
    val unreadCount: Int,
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
        @SerializedName("n_id")
        val nId: Int,
        @SerializedName("n_sender_id")
        val nSenderId: String,
        @SerializedName("n_sender_name")
        val nSenderName: String,
        @SerializedName("n_sender_image")
        val nSenderImage: String,
        @SerializedName("n_params")
        val nParams: NParams,
        @SerializedName("n_message")
        val nMessage: String,
        @SerializedName("n_notification_type")
        val nNotificationType: Int,
        @SerializedName("n_created_at")
        val nCreatedAt: String,
        @SerializedName("n_status")
        val nStatus: Int
    ) {
        data class NParams(
            @SerializedName("u_id")
            val uId: Int
        )
    }
}