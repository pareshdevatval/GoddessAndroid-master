package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class TodaysPractic(
    @PrimaryKey
    @SerializedName("cp_id")
    var cpId: Int = 0,

    @SerializedName("cp_calendar_id")
    var cpCalendarId: Int = 0,

    @SerializedName("cp_practice_id")
    var cpPracticeId: Int = 0,

    @SerializedName("cp_status")
    var cpStatus: Int = 0,

    @SerializedName("cp_created_at")
    var cpCreatedAt: String = "",

    @Ignore
    @SerializedName("practics")
    var practics: List<Practice?>? = listOf(),

    @SerializedName("cp_updated_at")
    var cpUpdatedAt: String? = null,

    @SerializedName("cp_deleted_at")
    var cpDeletedAt: String? = null
):Parcelable