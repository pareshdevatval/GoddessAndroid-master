package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

data class  LoginResponse(
    @SerializedName("result")
    var result: User? = null
) : BaseResponse() {
    data class User(
        @SerializedName("u_id")
        var uId: Int? = null,
        @SerializedName("u_user_name")
        var uUserName: String? = null,
        @SerializedName("u_email")
        var uEmail: String? = null,
        @SerializedName("u_mobile_number")
        var uMobileNumber: String? = null,
        @SerializedName("u_social_id")
        var uSocialId: Any? = null,
        @SerializedName("u_dob")
        var uDob: Any? = null,
        @SerializedName("u_gender")
        var uGender: Any? = null,
        @SerializedName("u_image")
        var uImage: String? = null,
        @SerializedName("u_user_type")
        var uUserType: Int? = null,
        @SerializedName("u_actual_earned_points")
        var uActualEarnedPoints: Int? = null,
        @SerializedName("u_points")
        var uPoints: Int? = null,
        @SerializedName("u_referral_code")
        var uReferralCode: Any? = null,
        @SerializedName("u_referred_by")
        var uReferredBy: Any? = null,
        @SerializedName("u_enable_practice_notification")
        var uEnablePracticeNotification: Int? = null,
        @SerializedName("u_enable_community_notification")
        var uEnableCommunityNotification: Int? = null,
        @SerializedName("u_enable_shopping_notification")
        var uEnableShoppingNotification: Int? = null,
        @SerializedName("u_created_at")
        var uCreatedAt: String? = null,
        @SerializedName("u_updated_at")
        var uUpdatedAt: String? = null,
        @SerializedName("token")
        var token: String? = null,
        @SerializedName("u_stripe_customer_id")
        var uStripeCustomerId: String? = null,
        @SerializedName("current_level_points")
        var currentLevelPoints: String? = null
    )
}