package com.krystal.goddesslifestyle.data.model

/**
 * Created by Bhargav Thanki on 27 April,2020.
 */
data class Stripe3DAuthResponse(val status: Boolean,
                                val paymentIntentId: String,
                                val message: String = "") {
}