package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class MyProfileResponse(
    @SerializedName("result")
    val result: Result
):BaseResponse() {
    data class Result(
        @SerializedName("u_id")
        val uId: Int, // 9
        @SerializedName("u_user_name")
        val uUserName: String, // adam Gill
        @SerializedName("u_email")
        val uEmail: String, // adam@gmail.com
        @SerializedName("u_mobile_number")
        val uMobileNumber: String, // +919624649521
        @SerializedName("u_social_id")
        val uSocialId: Any, // null
        @SerializedName("u_dob")
        val uDob: Any, // null
        @SerializedName("u_gender")
        val uGender: Int, // 1
        @SerializedName("u_image")
        val uImage: String, // avatar-1.png
        @SerializedName("u_user_type")
        val uUserType: Int, // 1
        @SerializedName("u_actual_earned_points")
        val uActualEarnedPoints: Int, // 0
        @SerializedName("u_points")
        val uPoints: Int, // 0
        @SerializedName("u_referral_code")
        val uReferralCode: Any, // null
        @SerializedName("u_referred_by")
        val uReferredBy: Any, // null
        @SerializedName("u_enable_practice_notification")
        val uEnablePracticeNotification: Int, // 1
        @SerializedName("u_enable_community_notification")
        val uEnableCommunityNotification: Int, // 1
        @SerializedName("u_enable_shopping_notification")
        val uEnableShoppingNotification: Int, // 1
        @SerializedName("u_stripe_customer_id")
        val uStripeCustomerId: String, // null
         @SerializedName("current_level_points")
        val current_level_points: String // null
    )
}