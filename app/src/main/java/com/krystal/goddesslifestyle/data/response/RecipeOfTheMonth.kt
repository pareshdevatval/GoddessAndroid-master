package com.krystal.goddesslifestyle.data.response


import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

//@Entity
data class RecipeOfTheMonth(
    @PrimaryKey
    @SerializedName("recipe_id")
    var recipeId: Int? = null,
    @SerializedName("recipe_title")
    var recipeTitle: String? = null,
    @SerializedName("recipe_description")
    var recipeDescription: String? = null,
    @SerializedName("recipe_calories")
    var recipeCalories: String? = null,
    @SerializedName("recipe_duration")
    var recipeDuration: String? = null,
    @SerializedName("recipe_number_of_serving")
    var recipeNumberOfServing: Int? = null,
    @SerializedName("recipe_ingredients")
    var recipeIngredients: String? = null,

    //@Ignore
    @SerializedName("recipe_images")
    var recipeImages: List<RecipeOfTheMonthImage?>? = null
)