package com.sherdle.universal.util;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class Log {
    //To print log's and exceptions
    public static final boolean LOG = true;

    public static void i(String tag, String string) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.i(tag, string);
    }
    public static void e(String tag, String string) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.e(tag, string);
    }
    public static void e(String tag, String string, Exception e) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.e(tag, string, e);
    }

    public static void d(String tag, String string) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.d(tag, string);
    }
    public static void v(String tag, String string) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.v(tag, string);
    }
    public static void w(String tag, String string) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.w(tag, string);
    }
    public static void w(String tag, String string, Exception e) {
        FirebaseCrashlytics.getInstance().log(string);
        if (LOG) android.util.Log.w(tag, string, e);
    }

    public static void printStackTrace(Exception e) {
        FirebaseCrashlytics.getInstance().recordException(e);
        if (LOG) e.printStackTrace();
    }
}