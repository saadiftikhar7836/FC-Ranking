package com.codesses.fcranking.model

data class FavouriteCharacter(
    val character_id: String = "",
    val user_id: String = ""
) {
    lateinit var favouriteCharId: String
}