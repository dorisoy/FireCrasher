package com.osama.firecrasher

import android.app.Activity
import android.app.ActivityManager
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper

internal class CrashHandler internal constructor() : Thread.UncaughtExceptionHandler {

    var activity: Activity? = null

    val lifecycleCallbacks: ActivityLifecycleCallbacks

    private var crashListener: CrashListener? = null

    fun setCrashListener(crashListener: CrashListener?) {
        this.crashListener = crashListener
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Handler(Looper.getMainLooper()).post { crashListener?.onCrash(thread, throwable) }
    }

    fun getBackStackCount(): Int {
        activity ?: return 0
        val m = activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfoList = m.getRunningTasks(10)
        var numOfActivities = 0
        if (runningTaskInfoList.size >= 1) numOfActivities = runningTaskInfoList[0].numActivities
        return if (numOfActivities <= 0) 0 else numOfActivities - 1
    }

    init {
        lifecycleCallbacks = object : ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                this@CrashHandler.activity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                this@CrashHandler.activity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                this@CrashHandler.activity = activity
            }

            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) = Unit
            override fun onActivityDestroyed(activity: Activity) {
                if (this@CrashHandler.activity == activity)
                    this@CrashHandler.activity = null
            }
        }
    }
}