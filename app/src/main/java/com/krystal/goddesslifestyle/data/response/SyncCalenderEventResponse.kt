package com.krystal.goddesslifestyle.data.response


import android.annotation.SuppressLint
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class SyncCalenderEventResponse(
    var result: List<CalendarsData?>? = listOf(),
    var server_date: String? = "",
    var message: String? = "",
    var status: Boolean? = false,
    var code: Int? = 0
) : Parcelable