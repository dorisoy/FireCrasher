package com.osama.firecrasher

/**
 * Listener for when an unexpected crash has occurred
 */
abstract class CrashListener {

    /**
     * Method called when an unexpected crash has been captured
     * @param throwable Throwable
     */
    abstract fun onCrash(throwable: Throwable)

    /**
     * Method called when an unexpected crash has been captured
     * @param thread the current thread
     * @param throwable Throwable
     * @since v1.0.2
     */
    fun onCrash(thread: Thread, throwable: Throwable) {
        onCrash(throwable)
    }
}
