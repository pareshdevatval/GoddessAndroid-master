package com.krystal.goddesslifestyle.data.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.krystal.goddesslifestyle.base.BaseResponse
import kotlinx.android.parcel.Parcelize

data class VideoCategoriesResponse(
    @SerializedName("result")
    var videoCategories: List<VideoCategory?>? = null
): BaseResponse() {
    data class VideoCategory(
        @SerializedName("vlc_id")
        var id: Int? = null,
        @SerializedName("vlc_title")
        var title: String? = null,
        @SerializedName("vlc_image")
        var image: String? = null,
        @SerializedName("childrens")
        var subCategories: List<VideoSubCategory?>? = null
    ) {
        @Parcelize
        data class VideoSubCategory(
            @SerializedName("vlc_id")
            var id: Int? = null,
            @SerializedName("vlc_title")
            var title: String? = null,
            @SerializedName("vlc_image")
            var image: String? = null,
            @SerializedName("vlc_parent_id")
            var parentId: Int? = null,
            @SerializedName("vlc_status")
            var status: Int? = null,
            @SerializedName("vlc_created_at")
            var createdAt: String? = null,
            @SerializedName("vlc_updated_at")
            var updatedAt: String? = null,
            @SerializedName("vlc_deleted_at")
            var deletedAt: String? = null
        ) : Parcelable
    }
}