package com.krystal.goddesslifestyle.data.db.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class CartAmount(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var subTotal: Double? = null,

    var deliveryChargees: Double? = null,

    var totalAmount: Double? = null,

    var useRedeemPoint : Boolean = false,

    var redeemPoints: Double? = null,

    var newTotalAmount: Double? = null

): Parcelable