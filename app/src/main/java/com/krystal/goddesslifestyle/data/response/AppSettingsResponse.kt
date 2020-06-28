package com.krystal.goddesslifestyle.data.response


import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse
import kotlinx.android.parcel.Parcelize

data class AppSettingsResponse(
    @SerializedName("result")
    var result: Result? = null

) : BaseResponse() {
    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Result(
        var stripe_public_key: String? = "",
        var stripe_secret_key: String? = "",
        var order_delivery_charge: List<OrderDeliveryCharge?>? = listOf(),
        var convert_points_to_usd: String? = "",
        var point_system: PointSystem? = PointSystem()
    ) : Parcelable {
        @SuppressLint("ParcelCreator")
        @Parcelize
        data class OrderDeliveryCharge(
            var min_order_value: Int? = 0,
            var max_order_value: Int? = 0,
            var local_delivery_charge: Int? = 0,
            var international_delivery_charge: Int? = 0
        ) : Parcelable

        @SuppressLint("ParcelCreator")
        @Parcelize
        data class PointSystem(
            var beginner: String? = "",
            var maiden: String? = "",
            var priestess: String? = "",
            var queen: String? = "",
            var goddess: String? = ""
        ) : Parcelable
    }
}