package me.imoltres.bbu.utils.scoreboard

/**
 * Exception while setting up the BBUScoreboard
 */
class BBUScoreboardSetupException(message: String, e: Exception) : Exception("$message\nActual Exception: ") {
    init {
        e.printStackTrace()
    }
}