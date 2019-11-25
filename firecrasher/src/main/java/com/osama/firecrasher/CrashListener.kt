package com.osama.firecrasher

/**
 * Listener for when an unexpected crash has occurred
 */
abstract class CrashListener {

    /**
     * Method called when an unexpeted crash has been captured
     * @param throwable Throwable
     */
    abstract fun onCrash(throwable: Throwable)
}
