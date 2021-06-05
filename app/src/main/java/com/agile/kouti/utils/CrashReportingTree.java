package com.agile.kouti.utils;

import android.util.Log;
import timber.log.Timber;

public class CrashReportingTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
        //logging only exceptions
        if (priority >= Log.ERROR && throwable != null) {
        }
    }
} 