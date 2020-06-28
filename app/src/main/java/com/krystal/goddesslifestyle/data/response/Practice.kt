package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Practice(
    @PrimaryKey
    @SerializedName("practice_id")
    var practiceId: Int = 0,

    @SerializedName("practice_title")
    var practiceTitle: String = "",

    @SerializedName("practice_description")
    var practiceDescription: String = "",

    @SerializedName("practice_image")
    var practiceImage: String = "",

    @SerializedName("practice_video")
    var practiceVideo: String = "",

    @SerializedName("practice_status")
    var practiceStatus: Int = 0,

    @SerializedName("practice_created_at")
    var practiceCreatedAt: String = "",

    @Ignore
    @SerializedName("practice_equipment")
    var practiceEquipments: List<PracticeEquipment?>? = listOf(),

    @SerializedName("practice_updated_at")
    var practiceUpdatedAt: String? = null,

    @SerializedName("practice_deleted_at")
    var practiceDeletedAt: String? = null
):Parcelable