package com.fcranking.android.utils

import android.app.Application
import android.content.Context
import com.fcranking.android.interfaces.Bus

class FCRankingApp : Application() {

    private lateinit var mContext: Context


    override fun onCreate() {
        super.onCreate()

        mContext = applicationContext

        SharedPrefConfig.initSharedConfig(configBus)

    }

    private val configBus = object : Bus {
        override fun getAppContext(): Context {
            return this@FCRankingApp
        }
    }


}