package com.krystal.goddesslifestyle.data.response


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class UserSubscription(
    @PrimaryKey
    @SerializedName("us_id")
    var usId: Int? = null,
    @SerializedName("us_user_id")
    var usUserId: Int? = null,
    @SerializedName("us_subscription_plan_id")
    var usSubscriptionPlanId: String? = null,
    @SerializedName("us_token")
    var usToken: String? = null,
    @SerializedName("us_price")
    var usPrice: String? = null,
    @SerializedName("us_from")
    var usFrom: String? = null,
    @SerializedName("us_to")
    var usTo: String? = null,
    @SerializedName("us_type")
    var usType: Int? = null,
    @SerializedName("us_order_id")
    var usOrderId: String? = null,
    @SerializedName("us_device_type")
    var usDeviceType: String? = null,
    @SerializedName("us_status")
    var usStatus: Int? = null,
    @SerializedName("us_created_at")
    var usCreatedAt: String? = null,
    @SerializedName("us_updated_at")
    var usUpdatedAt: String? = null,
    @SerializedName("us_deleted_at")
    var usDeletedAt: String? = null,
    @SerializedName("us_subscription_expired")
    var usSubscriptionExpired: Boolean? = null,
    @SerializedName("us_subscription_remain_days")
    var usSubscriptionRemainDays: String? = null
)