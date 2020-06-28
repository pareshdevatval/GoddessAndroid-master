package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class Registration(
    @SerializedName("u_user_name")
    var uUserName: String? = null,
    @SerializedName("u_email")
    var uEmail: String? = null,
    @SerializedName("u_mobile_number")
    var uMobileNumber: String? = null,
    @SerializedName("u_user_type")
    var uUserType: Int? = null,
    @SerializedName("u_social_id")
    var uSocialId: Any? = null,
    @SerializedName("u_updated_at")
    var uUpdatedAt: String? = null,
    @SerializedName("u_created_at")
    var uCreatedAt: String? = null,
    @SerializedName("u_id")
    var uId: Int? = null,
    @SerializedName("token")
    var token: String? = null
)