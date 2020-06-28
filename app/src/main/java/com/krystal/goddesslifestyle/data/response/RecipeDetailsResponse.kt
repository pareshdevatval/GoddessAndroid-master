package com.krystal.goddesslifestyle.data.response

import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

/**
 * Created by imobdev on 24/3/20
 */
data class RecipeDetailsResponse(
    @SerializedName("result") var result: RecipeData
):BaseResponse()

data class RecipeData(
    val recipe_calories: String,
    val recipe_created_at: String,
    val recipe_deleted_at: Any,
    val recipe_description: String,
    val recipe_duration: String,
    val recipe_id: Int,
    val recipe_images: ArrayList<RImage>,
    val recipe_ingredients: String,
    val recipe_number_of_serving: String,
    val recipe_status: Int,
    val recipe_steps: ArrayList<RStep>,
    val recipe_title: String,
    val recipe_updated_at: Any
)

data class RImage(
    val recipe_image_id: Int,
    val recipe_image_recipe_id: Int,
    val recipe_image_url: String
)

data class RStep(
    val recipe_step_created_at: String,
    val recipe_step_deleted_at: Any,
    val recipe_step_description: String,
    val recipe_step_id: Int,
    val recipe_step_recipe_id: Int,
    val recipe_step_status: Int,
    val recipe_step_title: String,
    val recipe_step_updated_at: Any
)