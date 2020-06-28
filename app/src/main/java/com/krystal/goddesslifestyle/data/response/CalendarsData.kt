package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class CalendarsData(
    @PrimaryKey
    @SerializedName("calendar_id")
    var calendarId: Int = 0,

    @SerializedName("calendar_theme_id")
    var calendarThemeId: Int = 0,

    @SerializedName("calendar_title")
    var calendarTitle: String = "",

    @SerializedName("calendar_day")
    var calendarDay: Int = 0,

    @SerializedName("calendar_status")
    var calendarStatus: Int = 0,

    @SerializedName("calendar_created_at")
    var calendarCreatedAt: String = "",

    @Ignore
    @SerializedName("calendar_practics")
    var calendarPractics: List<TodaysPractic?>? = listOf(),

    @Ignore
    @SerializedName("calendar_recipes")
    var calendarRecipes: List<TodaysRecipe?>? = listOf(),

    @Ignore
    @SerializedName("calendar_journal_prompts")
    var calendarJournalPrompts: List<TodaysJournal?>? = listOf(),

    @SerializedName("calendar_updated_at")
    var calendarUpdatedAt: String? = null,

    @SerializedName("calendar_deleted_at")
    var calendarDeletedAt: String? = null
):Parcelable