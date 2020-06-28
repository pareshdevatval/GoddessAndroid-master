package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class EditProfileResponse(
    @SerializedName("result")
    val result: Result
):BaseResponse() {
    data class Result(
        @SerializedName("u_id")
        val uId: Int,
        @SerializedName("u_user_name")
        val uUserName: String,
        @SerializedName("u_email")
        val uEmail: String,
        @SerializedName("u_mobile_number")
        val uMobileNumber: String,
        @SerializedName("u_social_id")
        val uSocialId: Any,
        @SerializedName("u_dob")
        val uDob: Any,
        @SerializedName("u_gender")
        val uGender: String,
        @SerializedName("u_password")
        val uPassword: String,
        @SerializedName("u_image")
        val uImage: String,
        @SerializedName("u_user_type")
        val uUserType: Int,
        @SerializedName("u_last_login")
        val uLastLogin: Any,
        @SerializedName("u_actual_earned_points")
        val uActualEarnedPoints: Int,
        @SerializedName("u_points")
        val uPoints: Int,
        @SerializedName("u_referral_code")
        val uReferralCode: Any,
        @SerializedName("u_referred_by")
        val uReferredBy: Any,
        @SerializedName("u_enable_practice_notification")
        val uEnablePracticeNotification: Int,
        @SerializedName("u_enable_community_notification")
        val uEnableCommunityNotification: Int,
        @SerializedName("u_enable_shopping_notification")
        val uEnableShoppingNotification: Int,
        @SerializedName("u_stripe_customer_id")
        val uStripeCustomerId: Any,
        @SerializedName("u_status")
        val uStatus: Int,
        @SerializedName("u_created_at")
        val uCreatedAt: String,
        @SerializedName("u_updated_at")
        val uUpdatedAt: String,
        @SerializedName("u_deleted_at")
        val uDeletedAt: Any
    )
}