package com.agile.kouti

import android.app.Application
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.CrashReportingTree
import timber.log.Timber

class KoutiApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if (Const.IS_DEBUGGABLE)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(CrashReportingTree())

    }
}