package com.banuba.sdk.example.beautification

import android.app.Application
import com.banuba.sdk.manager.BanubaSdkManager

class Application : Application() {

    private val BANUBA_CLIENT_TOKEN: String = <Place your token here>

    override fun onCreate() {
        super.onCreate()
        BanubaSdkManager.initialize(this, BANUBA_CLIENT_TOKEN)
    }
}