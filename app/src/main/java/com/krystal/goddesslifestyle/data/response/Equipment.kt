package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Equipment(
    @PrimaryKey()
    @SerializedName("equipment_id")
    var equipmentId: Int = 0,

    @SerializedName("equipment_title")
    var equipmentTitle: String = "",

    @SerializedName("equipment_image")
    var equipmentImage: String = "",

    @SerializedName("equipment_status")
    var equipmentStatus: Int = 0,

    @SerializedName("equipment_created_at")
    var equipmentCreatedAt: String = "",

    @SerializedName("equipment_updated_at")
    var equipmentUpdatedAt: String? = null,

    @SerializedName("equipment_deleted_at")
    var equipmentDeletedAt: String? = null
) : Parcelable