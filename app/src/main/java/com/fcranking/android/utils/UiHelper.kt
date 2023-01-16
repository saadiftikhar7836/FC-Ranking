package com.fcranking.android.utils

import java.util.regex.Pattern

object UiHelper {
    fun isValidPassword(password: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}")
        return pattern.matcher(password)
            .find()
    }

    fun isValidEmail(emailStr: String): Boolean {
        val pattern = Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        val matcher = pattern.matcher(emailStr)
        return matcher.find()
    }

}