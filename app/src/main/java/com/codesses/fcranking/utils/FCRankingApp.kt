package com.codesses.fcranking.utils

import android.app.Application
import android.content.Context
import com.codesses.fcranking.interfaces.Bus
import com.giphy.sdk.ui.Giphy

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