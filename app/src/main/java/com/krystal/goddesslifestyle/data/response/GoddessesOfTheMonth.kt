package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

//@Entity
data class GoddessesOfTheMonth(
    //@PrimaryKey
    @SerializedName("goddess_id")
    var goddessId: Int? = null,
    @SerializedName("goddess_title")
    var goddessTitle: String? = null,
    @SerializedName("goddess_description")
    var goddessDescription: String? = null,
    @SerializedName("goddess_image")
    var goddessImage: String? = null
)