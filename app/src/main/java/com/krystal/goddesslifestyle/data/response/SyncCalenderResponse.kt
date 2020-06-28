package com.krystal.goddesslifestyle.data.response


import android.annotation.SuppressLint
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class SyncCalenderResponse(
    var result: Result? = Result(),
    var server_date: String? = "",
    var message: String? = "",
    var status: Boolean? = false,
    var code: Int? = 0
) : Parcelable {
    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Result(
        var ta_id: Int? = 0,
        var ta_theme_id: Int? = 0,
        var ta_year: Int? = 0,
        var ta_month: Int? = 0,
        var ta_status: Int? = 0,
        var ta_created_at: String? = "",
        var ta_updated_at: String? = "",
        var ta_deleted_at: String? = "",
        var themes_data: ThemesData? = ThemesData()
    ) : Parcelable {
        @SuppressLint("ParcelCreator")
        @Parcelize
        data class ThemesData(
            var theme_id: Int? = 0,
            var theme_title: String? = "",
            var theme_image: String? = "",
            var theme_status: Int? = 0,
            var theme_created_at: String? = "",
            var theme_updated_at: String? = "",
            var theme_deleted_at: String? = "",
            var calendars_data: CalendarsData? = CalendarsData()
        ) : Parcelable {
            @SuppressLint("ParcelCreator")
            @Parcelize
            data class CalendarsData(
                var calendar_practics: List<Practice?>? = listOf(),
                var calendar_recipes: List<Recipe?>? = listOf(),
                var calendar_journal_prompts: List<Journal?>? = listOf()
            ) : Parcelable
        }
    }
}