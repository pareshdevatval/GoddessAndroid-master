package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

data class CalenderResponse(
    @SerializedName("result")
    var themeMaster: ThemeMaster? = ThemeMaster(),
    @SerializedName("message")
    var message: String = "",
    @SerializedName("status")
    var status: Boolean = false,
    @SerializedName("code")
    var code: Int = 0,
    @SerializedName("server_date")
    var serverDate: String = ""
)