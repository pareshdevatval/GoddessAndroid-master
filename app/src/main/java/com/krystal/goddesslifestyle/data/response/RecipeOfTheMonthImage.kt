package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

//@Entity
data class RecipeOfTheMonthImage(
    //@PrimaryKey
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("image")
    var image: String? = null
)