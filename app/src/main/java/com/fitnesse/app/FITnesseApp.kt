package com.fitnesse.app

import android.app.Application
import com.fitnesse.app.data.local.PhotoStorage

class FITnesseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PhotoStorage.init(this)
    }
}
