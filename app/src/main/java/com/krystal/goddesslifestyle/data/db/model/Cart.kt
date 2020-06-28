package com.krystal.goddesslifestyle.data.db.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Cart(
    @PrimaryKey
    var productId: Int? = null,

    var amount: Double? = null,

    var quantity: Int= 0
): Parcelable