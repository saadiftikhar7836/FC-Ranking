package com.fcranking.android.model

import java.io.Serializable

data class User(
    var full_name: String = "",
    var email: String = "",
    var password: String = "",
    var fcm_token: String = "",
    var role: String = "user",
    var profile_image: String = "",
    var cover_photo: String = "",
    var comments_count: Long = 0,
    var favourite_characters_count: Long = 0
) : Serializable{
    var userId: String = null ?: ""
}