package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Recipe(
    @PrimaryKey
    @SerializedName("recipe_id")
    var recipeId: Int = 0,

    @SerializedName("recipe_title")
    var recipeTitle: String = "",

    @SerializedName("recipe_description")
    var recipeDescription: String = "",

    @SerializedName("recipe_calories")
    var recipeCalories: String = "",

    @SerializedName("recipe_duration")
    var recipeDuration: String = "",

    @SerializedName("recipe_number_of_serving")
    var recipeNumberOfServing: Int = 0,

    @SerializedName("recipe_ingredients")
    var recipeIngredients: String = "",

    @SerializedName("recipe_status")
    var recipeStatus: Int = 0,

    @SerializedName("recipe_created_at")
    var recipeCreatedAt: String = "",

    @Ignore
    @SerializedName("recipe_images")
    var recipeImages: List<RecipeImage?>? = listOf(),

    @Ignore
    @SerializedName("recipe_steps")
    var recipeSteps: List<RecipeStep?>? = listOf(),

    @SerializedName("recipe_updated_at")
    var recipeUpdatedAt: String? = null,

    @SerializedName("recipe_deleted_at")
    var recipeDeletedAt: String? = null
):Parcelable