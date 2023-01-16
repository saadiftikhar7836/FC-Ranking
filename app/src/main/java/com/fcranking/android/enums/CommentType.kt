package com.fcranking.android.enums

enum class CommentType(val value: Int) {
    TEXT_ONLY(0),
    IMAGE_ONLY(1),
    GIF_ONLY(2),
    TEXT_AND_IMAGE(3),
    TEXT_AND_GIF(4)
}