package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

//@Entity
data class OfTheMonth(
    //@Ignore
    @SerializedName("goddesses")
    var goddesses: GoddessesOfTheMonth? = null,

    //@Ignore
    @SerializedName("recipes")
    var recipes: RecipeOfTheMonth? = null,

    //@Ignore
    @SerializedName("teachers")
    var teachers: TeacherOfTheMonth? = null,

    //@Ignore
    @SerializedName("video")
    var video: Video? = null,

    @SerializedName("month")
    var month: String? = null,
    @SerializedName("year")
    var year: String? = null
)