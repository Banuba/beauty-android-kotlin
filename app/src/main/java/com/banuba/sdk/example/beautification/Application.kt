package com.banuba.sdk.example.beautification

import android.app.Application
import com.banuba.sdk.example.common.BANUBA_CLIENT_TOKEN
import com.banuba.sdk.manager.BanubaSdkManager

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        BanubaSdkManager.initialize(this, BANUBA_CLIENT_TOKEN)
    }
}