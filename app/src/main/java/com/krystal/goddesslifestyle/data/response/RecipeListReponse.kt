package com.krystal.goddesslifestyle.data.response

import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse

/**
 * Created by imobdev on 23/3/20
 */
data class RecipeListReponse(
    @SerializedName("pagination")val pagination: Pagination,
    @SerializedName("result")val result: ArrayList<BreakfastData?>
):BaseResponse()

data class BreakfastData(
    val recipe_calories: String,
    val recipe_duration: String,
    val recipe_id: Int,
    val recipe_images: String,
    var recipe_is_liked: Int,
    val recipe_title: String
)