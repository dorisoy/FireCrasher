package com.osama.firecrasher

/**
 * Different kind of crash strategy levels supported
 */
enum class CrashLevel {

    /**
     * Restart the activity
     */
    LEVEL_ONE,

    /**
     * Launch the activity in background
     */
    LEVEL_TWO,

    /**
     * Restart application
     */
    LEVEL_THREE,
}