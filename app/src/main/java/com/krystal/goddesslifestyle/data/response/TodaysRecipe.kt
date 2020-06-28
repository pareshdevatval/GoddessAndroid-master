package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class TodaysRecipe(
    @PrimaryKey
    @SerializedName("cr_id")
    var crId: Int = 0,

    @SerializedName("cr_calendar_id")
    var crCalendarId: Int = 0,

    @SerializedName("cr_recipe_id")
    var crRecipeId: Int = 0,

    @SerializedName("cr_status")
    var crStatus: Int = 0,

    @SerializedName("cr_created_at")
    var crCreatedAt: String = "",

    @Ignore
    @SerializedName("recipes")
    var recipes: List<Recipe?>? = listOf(),

    @SerializedName("cr_updated_at")
    var crUpdatedAt: String? = null,

    @SerializedName("cr_deleted_at")
    var crDeletedAt: String? = null
):Parcelable