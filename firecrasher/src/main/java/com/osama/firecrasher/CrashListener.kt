package com.osama.firecrasher

/**
 * Created by Osama Raddad.
 */
abstract class CrashListener {
    abstract fun onCrash(throwable: Throwable)
}
