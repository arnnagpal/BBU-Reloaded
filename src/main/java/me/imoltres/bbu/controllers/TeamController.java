package me.imoltres.bbu.controllers;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TeamController {
    private final BBU plugin;

    private final List<BBUTeam> teams = new ArrayList<>();

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

    public List<BBUTeam> getTeamsWithCages() {
        List<BBUTeam> teams = new ArrayList<>();
        for (BBUTeam team : getAllTeams()) {
            if (team.getCage() != null) {
                teams.add(team);
            }
        }

        return teams;
    }

    public ImmutableList<BBUTeam> getAllTeams() {
        return ImmutableList.copyOf(teams);
    }

}
