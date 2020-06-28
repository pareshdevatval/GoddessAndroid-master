package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FavouritesResponse(
    @SerializedName("pagination")
    val pagination: Pagination,
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
) {
    data class Pagination(
        @SerializedName("total")
        val total: Int,
        @SerializedName("lastPage")
        val lastPage: Int,
        @SerializedName("perPage")
        val perPage: Int,
        @SerializedName("currentPage")
        val currentPage: Int
    )

    data class Result(
        @SerializedName("recipe_id")
        val recipeId: Int,
        @SerializedName("recipe_title")
        val recipeTitle: String,
        @SerializedName("recipe_calories")
        val recipeCalories: String,
        @SerializedName("recipe_duration")
        val recipeDuration: String,
        @SerializedName("recipe_images")
        val recipeImages: String,
        @Expose
        @SerializedName("recipe_is_liked")
        var recipeIsLiked: Int
    )
}