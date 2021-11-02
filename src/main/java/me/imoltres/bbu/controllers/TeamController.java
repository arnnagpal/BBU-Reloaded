package me.imoltres.bbu.controllers;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class TeamController {
    private final BBU plugin;

    private final Set<BBUTeam> teams = new HashSet<>();

    public boolean createTeam(BBUTeamColour colour) {
        return teams.add(new BBUTeam(colour));
    }

    public boolean deleteTeam(BBUTeamColour colour) {
        return teams.remove(getTeam(colour));
    }

    public boolean deleteTeam(BBUTeam team) {
        return teams.remove(team);
    }

    public BBUTeam getTeam(BBUTeamColour colour) {
        return teams.stream().filter(team -> team.getColour() == colour).findFirst().orElse(null);
    }

    public BBUTeam getTeam(BBUPlayer bbuPlayer) {
        return teams.stream().filter(team -> team.getPlayers().contains(bbuPlayer)).findFirst().orElse(null);
    }
}
