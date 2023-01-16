package com.fcranking.android.model

import java.io.Serializable

data class Characters(
    var character_name: String = "",
    var character_image: String = "",
    var show_name: String = "",
    var time_stamp: Long = -1,
    var time: String = "",
    var proposed_by: String = "",
    var date: String = "",
    var status: String = "",
    var last_rank: Long = 0,
    var best_rank: Long = 0,
    var worst_rank: Long = 0,
    var votes_count: Long = 0
) : Serializable {
    var user: User? = null
    var character_id = ""
}

