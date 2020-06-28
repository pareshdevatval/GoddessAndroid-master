package com.krystal.goddesslifestyle.data.response


import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ThemeMaster(
    @PrimaryKey
    @SerializedName("ta_id")
    var taId: Int,

    @SerializedName("ta_theme_id")
    var taThemeId: Int,

    @SerializedName("ta_year")
    var taYear: Int,

    @SerializedName("ta_month")
    var taMonth: Int,

    @SerializedName("ta_status")
    var taStatus: Int,

    @SerializedName("ta_created_at")
    var taCreatedAt: String?,

    @SerializedName("ta_updated_at")
    var taUpdatedAt: String?,

    @SerializedName("ta_deleted_at")
    var taDeletedAt: String?,

    @Ignore
    @SerializedName("themes_data")
    var themesData: Theme? = Theme()
) {
    constructor() : this(0, 0, 0, 0, 0, "", "", "", null)
}