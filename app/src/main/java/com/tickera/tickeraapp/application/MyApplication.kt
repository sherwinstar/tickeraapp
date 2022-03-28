package com.tickera.tickeraapp.application

import android.app.Application
import com.stripe.stripeterminal.TerminalApplicationDelegate

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()

        TerminalApplicationDelegate.onCreate(this)
    }

    // Don't forget to let the observer know if your application is running low on memory
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        TerminalApplicationDelegate.onTrimMemory(this, level)
    }
}