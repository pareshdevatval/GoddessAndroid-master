package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class RecipeStep(
    @PrimaryKey
    @SerializedName("recipe_step_id")
    var recipeStepId: Int = 0,

    @SerializedName("recipe_step_recipe_id")
    var recipeStepRecipeId: Int = 0,

    @SerializedName("recipe_step_title")
    var recipeStepTitle: String = "",

    @SerializedName("recipe_step_description")
    var recipeStepDescription: String = "",

    @SerializedName("recipe_step_status")
    var recipeStepStatus: Int = 0,

    @SerializedName("recipe_step_created_at")
    var recipeStepCreatedAt: String = "",

    @SerializedName("recipe_step_updated_at")
    var recipeStepUpdatedAt: String? = null,

    @SerializedName("recipe_step_deleted_at")
    var recipeStepDeletedAt: String? = null
): Parcelable