package com.osama.firecrasher

import android.app.Activity
import android.app.Application
import android.content.Intent

/**
 * FireCrasher is responsible for handling unexpected crashes instead of making your application crash
 */
object FireCrasher {

    /**
     * Number of retries already done
     */
    var retryCount: Int = 0
        private set

    private val crashHandler: CrashHandler by lazy { CrashHandler() }

    /**
     * Install the CrashHandler for the application
     * @param application Application
     * @param crashListener CrashListener
     */
    fun install(application: Application, crashListener: CrashListener) {
        if (FireLooper.isSafe) return
        crashHandler.setCrashListener(crashListener)
        application.registerActivityLifecycleCallbacks(crashHandler.lifecycleCallbacks)
        FireLooper.install()
        FireLooper.setUncaughtExceptionHandler(crashHandler)
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
    }

    /**
     * Evaluate the strategy level to be applied
     * @return CrashLevel
     */
    fun evaluate(): CrashLevel {
        return when {
            retryCount <= 1 ->
                //try to restart the failing activity
                CrashLevel.LEVEL_ONE
            crashHandler.getBackStackCount() >= 1 ->
                //failure in restarting the activity try to go back
                CrashLevel.LEVEL_TWO
            else ->
                //no activates to go back to so just restart the app
                CrashLevel.LEVEL_THREE
        }
    }

    /**
     * Evaluate the strategy level to be applied
     * @param onEvaluate callback to return the result
     */
    fun evaluateAsync(onEvaluate: ((activity: Activity?, level: CrashLevel) -> Unit)?) {
        when {
            retryCount <= 1 ->
                //try to restart the failing activity
                onEvaluate?.invoke(crashHandler.activity, CrashLevel.LEVEL_ONE)
            crashHandler.getBackStackCount() >= 1 ->
                //failure in restarting the activity try to go back
                onEvaluate?.invoke(crashHandler.activity, CrashLevel.LEVEL_TWO)
            else ->
                //no activates to go back to so just restart the app
                onEvaluate?.invoke(crashHandler.activity, CrashLevel.LEVEL_THREE)
        }
    }

    /**
     * Recover the application with specified strategy level to apply
     * @param level CrashLevel
     * @param onRecover callback which will be called when the recover strategy has been applied
     */
    fun recover(level: CrashLevel = evaluate(), onRecover: ((activity: Activity?) -> Unit)?) {
        val activityPair = getActivityPair()
        when (level) {
            //try to restart the failing activity
            CrashLevel.LEVEL_ONE -> {
                restartActivity(activityPair)
            }
            //failure in restarting the activity finish the current one
            CrashLevel.LEVEL_TWO -> {
                retryCount = 0
                finish(activityPair)
            }
            //no activates to go back to so just restart the app
            CrashLevel.LEVEL_THREE -> {
                retryCount = 0
                restartApp(activityPair)
            }
        }
        onRecover?.invoke(crashHandler.activity)
    }


    private fun getActivityPair(): Pair<Activity?, Intent?> {
        val activity = crashHandler.activity
        val intent: Intent? = if (activity?.intent?.action == "android.intent.action.MAIN")
            Intent(activity, activity.javaClass)
        else
            activity?.intent

        intent?.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return Pair(activity, intent)
    }

    private fun restartActivity(activityPair: Pair<Activity?, Intent?>) {
        val activity = activityPair.first ?: run {
            retryCount += 1
            return
        }

        when (retryCount) {
            0 -> activity.recreate()
            else -> {
                activity.startActivity(activityPair.second)
                activity.overridePendingTransition(0, 0)
                activity.finish()
                activity.overridePendingTransition(0, 0)
            }
        }

        retryCount += 1
    }

    private fun finish(activityPair: Pair<Activity?, Intent?>) {
        activityPair.first?.finish()
    }

    private fun restartApp(activityPair: Pair<Activity?, Intent?>) {
        val activity = activityPair.first ?: return
        val packageName = activity.baseContext.packageName

        activity.baseContext.packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        }

        with(activity) {
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}
