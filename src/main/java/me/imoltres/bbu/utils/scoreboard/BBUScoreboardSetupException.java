package me.imoltres.bbu.utils.scoreboard;

public class BBUScoreboardSetupException extends Exception {

    public BBUScoreboardSetupException(String message, Exception e) {
        super(message + "\nActual Exception: ");
        e.printStackTrace();
    }
}
