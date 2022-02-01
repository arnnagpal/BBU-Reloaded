package me.imoltres.bbu.utils.scoreboard

class BBUScoreboardSetupException(message: String, e: Exception) : Exception("$message\nActual Exception: ") {
    init {
        e.printStackTrace()
    }
}