package com.krystal.goddesslifestyle.data.response

/**
 * Created by imobdev on 9/4/20
 */
data class Pagination(
    val currentPage: Int,
    val lastPage: Int,
    val perPage: Int,
    val total: Int
)