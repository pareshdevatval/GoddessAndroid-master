package com.krystal.goddesslifestyle.data.response

import com.krystal.goddesslifestyle.base.BaseResponse

/**
 * Created by imobdev on 9/4/20
 */
data class CommunityOpinionCommentListResponse(
    val pagination: Pagination,
    val result: List<CommentData>
):BaseResponse()


data class CommentData(
    val added_by: CommentAddedBy,
    val gcoc_comment: String,
    val gcoc_created_at: String,
    val gcoc_deleted_at: Any,
    val gcoc_gco_id: Int,
    val gcoc_id: Int,
    val gcoc_status: Int,
    val gcoc_u_id: Int,
    val gcoc_updated_at: String
)

data class CommentAddedBy(
    val u_email: String,
    val u_id: Int,
    val u_image: String,
    val u_user_name: String
)