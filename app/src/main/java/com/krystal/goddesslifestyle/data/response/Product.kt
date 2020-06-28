package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "CartProduct")
data class Product(
    @PrimaryKey
    @SerializedName("product_id")
    var productId: Int? = null,
    @SerializedName("product_title")
    var productTitle: String? = null,
    @SerializedName("product_description")
    var productDescription: String? = null,
    @SerializedName("product_price")
    var productPrice: String? = null,
    @SerializedName("product_image")
    var productImage: String? = null,
    @SerializedName("product_status")
    var productStatus: Int? = null,
    @SerializedName("product_created_at")
    var productCreatedAt: String? = null,
    @SerializedName("product_updated_at")
    var productUpdatedAt: String? = null,
    @SerializedName("product_deleted_at")
    var productDeletedAt: String? = null,
    @SerializedName("product_is_new")
    var productIsNew: Boolean = false,
    @Ignore
    @SerializedName("media")
    var media: List<Media>? = null
): Parcelable