package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Theme(
    @PrimaryKey
    @SerializedName("theme_id")
    var themeId: Int = 0,

    @SerializedName("theme_title")
    var themeTitle: String = "",

    @SerializedName("theme_image")
    var themeImage: String = "",

    @SerializedName("theme_status")
    var themeStatus: Int = 0,

    @SerializedName("theme_created_at")
    var themeCreatedAt: String = "",

    @SerializedName("theme_updated_at")
    var themeUpdatedAt: String = "",

    @SerializedName("theme_deleted_at")
    var themeDeletedAt: String? = null,

    @Ignore
    @SerializedName("calendars_data")
    var calendarsData: List<CalendarsData?>? = listOf()
):Parcelable