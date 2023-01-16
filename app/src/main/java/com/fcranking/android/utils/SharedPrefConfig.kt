package com.fcranking.android.utils

import android.content.Context
import com.fcranking.android.interfaces.Bus


object SharedPrefConfig {
    private lateinit var mBus: Bus

    fun initSharedConfig(bus: Bus) {
        synchronized(this) {
            mBus = bus
        }
    }

    fun getAppContext(): Context {
        return mBus.getAppContext()
    }
}