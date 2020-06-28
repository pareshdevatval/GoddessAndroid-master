package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class MyAddressListResponse(
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
) {
    @Parcelize
    data class Result(
        @SerializedName("ua_id")
        val uaId: Int?,
        @SerializedName("ua_user_id")
        val uaUserId: Int?,
        @SerializedName("ua_address_title")
        val uaAddressTitle: String?,
        @SerializedName("ua_pin_code")
        val uaPinCode: String?,
        @SerializedName("ua_house_no")
        val uaHouseNo: String?,
        @SerializedName("ua_road_area_colony")
        val uaRoadAreaColony: String?,
        @SerializedName("ua_city")
        val uaCity: String?,
        @SerializedName("ua_state")
        val uaState: String?,
        @SerializedName("ua_landmark")
        val uaLandmark: String?="",
        @SerializedName("ua_country")
        val uaCountry: String?,
        @SerializedName("ua_status")
        val uaStatus: Int?,
        @SerializedName("ua_created_at")
        val uaCreatedAt: String?,
        @SerializedName("ua_updated_at")
        val uaUpdatedAt: String?
    ) : Parcelable
}