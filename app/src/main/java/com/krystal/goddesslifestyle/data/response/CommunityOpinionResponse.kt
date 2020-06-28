package com.krystal.goddesslifestyle.data.response

import android.os.Parcelable
import com.krystal.goddesslifestyle.base.BaseResponse
import kotlinx.android.parcel.Parcelize

/**
 * Created by imobdev on 9/4/20
 */
data class CommunityOpinionResponse(
    val pagination: Pagination,
    val result: List<CommunityOpinionResult>
) : BaseResponse()

@Parcelize
data class CommunityOpinionResult(
    val added_by: CommunityOpinionAddedBy?,
    var already_liked: Boolean,
    val comments_count: String,
    val gco_created_at: String,
    val gco_deleted_at: String,
    val gco_id: Int,
    val gco_show_identity: Int,
    val gco_status: Int,
    val gco_text: String,
    val gco_u_id: Int,
    val gco_updated_at: String,
    var likes_count: String,
    val media: List<MediaData>
) : Parcelable

@Parcelize
data class CommunityOpinionAddedBy(
    val u_email: String,
    val u_id: Int,
    val u_image: String,
    val u_user_name: String
) : Parcelable

@Parcelize
data class MediaData(
    var gcom_id: Int = 0,
    var gcom_gco_id: Int = 0,
    var gcom_media_type: Int = 0,
    var gcom_status: Int = 0,
    var gcom_media: String? = "",
    var gco_created_at: String? = ""
) : Parcelable