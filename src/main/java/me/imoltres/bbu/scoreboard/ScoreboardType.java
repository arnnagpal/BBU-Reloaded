package me.imoltres.bbu.scoreboard;

import me.imoltres.bbu.scoreboard.impl.LobbyScoreboard;

public record ScoreboardType(String name, Class<? extends BBUScoreboard> scoreboard) {
    public static ScoreboardType DEFAULT = new ScoreboardType("DEFAULT", null);
    public static ScoreboardType LOBBY = new ScoreboardType("LOBBY", LobbyScoreboard.class);
    public static ScoreboardType GAME = new ScoreboardType("GAME", null);

}
