package com.codesses.fcranking.model

import java.io.Serializable

data class Comments(
    var comment_id: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val date: String = "",
    val time: String = "",
    val commented_by: String = "",
    val replied_by: String = "",
    val image_url: String = "",
    val gif_url: String = "",
    val type: Int = 0,
    var likes_count: Long = 0
) : Serializable {
    var user: User = User()
    var timeAgo: String = ""
    var isLiked: Boolean = false
    var repliesCount: Int = 0
}