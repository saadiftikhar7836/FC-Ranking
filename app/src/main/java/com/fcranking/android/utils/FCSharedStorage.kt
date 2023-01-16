package com.fcranking.android.utils

import com.fcranking.android.enums.EnumIntents
import com.fcranking.android.model.User


object FCSharedStorage : SharedPrefHelper(FCSharedConfig.PREF_NAME) {
    private const val RECENT_SCREEN = "recent_screen"
    private const val SLIDER_VISITED = "on_boarding_slider_visited"
    private const val FILTERED_CITY = "filtered_city"
    private const val EVENTS_CREDITS_LEFT = "events_credits_left"
    private const val INVITATION_DAYS_LEFT = "invitation_days_left"
    private const val CURRENT_MONTH_FREE_EVENT = "current_m%s_%s_free_event"


    fun saveUserData(user: User) {
        saveObject(EnumIntents.USER.value, user)
    }

    fun getUserObject(): User {
        return super.getUserObject(EnumIntents.USER.value)
    }

}