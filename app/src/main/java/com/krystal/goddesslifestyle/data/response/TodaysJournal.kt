package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class TodaysJournal(
    @PrimaryKey
    @SerializedName("cjp_id")
    var cjpId: Int = 0,

    @SerializedName("cjp_calendar_id")
    var cjpCalendarId: Int = 0,

    @SerializedName("cjp_jp_id")
    var cjpJpId: Int = 0,

    @SerializedName("cjp_status")
    var cjpStatus: Int = 0,

    @SerializedName("cjp_created_at")
    var cjpCreatedAt: String = "",

    @Ignore
    @SerializedName("journal_prompts")
    var journalPrompts: List<Journal?>? = listOf(),

    @SerializedName("cjp_updated_at")
    var cjpUpdatedAt: String? = null,

    @SerializedName("cjp_deleted_at")
    var cjpDeletedAt: String? = null
): Parcelable