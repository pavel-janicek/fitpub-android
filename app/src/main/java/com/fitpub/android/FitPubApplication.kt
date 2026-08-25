package com.fitpub.android

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

class FitPubApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Configure osmdroid tile cache in app storage.
        Configuration.getInstance()
            .load(context = this, sharedPreferenceName = "osmdroid_prefs")
        Configuration.getInstance().userAgentValue = "FitPub-Android/0.1.0"

        // osmdroid needs access to a writeable tile cache dir for modern scoped storage.
        Configuration.getInstance().osmdroidBasePath = filesDir
        Configuration.getInstance().osmdroidTileCache = filesDir.resolve("osmdroid")
    }

    companion object {
        fun from(context: Context): FitPubApplication = context.applicationContext as FitPubApplication

        fun container(context: Context): AppContainer = from(context).container
    }
}