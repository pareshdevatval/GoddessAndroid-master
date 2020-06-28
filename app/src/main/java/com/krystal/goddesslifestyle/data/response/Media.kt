package com.krystal.goddesslifestyle.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Bhargav Thanki on 04 May,2020.
 */
@Parcelize
data class Media(
    @SerializedName("pi_id")
    var piId: Int? = null,
    @SerializedName("pi_product_id")
    var piProductId: Int? = null,
    @SerializedName("pi_media")
    var piMedia: String? = null
): Parcelable