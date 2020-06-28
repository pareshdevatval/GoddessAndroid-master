package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class PracticeEquipment(
    @PrimaryKey
    @SerializedName("pe_id")
    var peId: Int = 0,

    @SerializedName("pe_equipment_id")
    var peEquipmentId: Int = 0,

    @SerializedName("pe_practice_id")
    var pePracticeId: Int = 0,

    @Ignore
    @SerializedName("equipments")
    var equipments: List<Equipment?>? = listOf()
):Parcelable