package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class RecipeImage(
    @PrimaryKey
    @SerializedName("recipe_image_id")
    var recipeImageId: Int = 0,

    @SerializedName("recipe_image_recipe_id")
    var recipeImageRecipeId: Int = 0,

    @SerializedName("recipe_image_url")
    var recipeImageUrl: String = ""
): Parcelable