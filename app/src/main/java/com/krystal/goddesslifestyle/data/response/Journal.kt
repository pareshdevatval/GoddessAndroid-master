package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Journal(
    @PrimaryKey
    @SerializedName("jp_id")
    var jpId: Int = 0,

    @SerializedName("jp_image")
    var jpImage: String = "",

    @SerializedName("jp_status")
    var jpStatus: Int = 0,

    @SerializedName("jp_created_at")
    var jpCreatedAt: String = "",

    @SerializedName("jp_updated_at")
    var jpUpdatedAt: String? = null,

    @SerializedName("jp_deleted_at")
    var jpDeletedAt: String? = null
): Parcelable