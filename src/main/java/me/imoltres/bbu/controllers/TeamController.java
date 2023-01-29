package me.imoltres.bbu.controllers;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.world.WorldPosition;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller to manage all the team instances
 */
@RequiredArgsConstructor
public class TeamController {
    private final BBU plugin;

    private final List<BBUTeam> teams = new ArrayList<>();

    /**
     * Create a team and cache it
     *
     * @param colour colour of the team
     * @return if it was successful
     */
    public boolean createTeam(BBUTeamColour colour) {
        return teams.add(new BBUTeam(colour));
    }

    /**
     * Delete a team from cache based on colour
     *
     * @param colour colour of the team
     * @return if it was successful
     */
    public boolean deleteTeam(BBUTeamColour colour) {
        return teams.remove(getTeam(colour));
    }

    /**
     * Delete a team from cache from the instance of it
     *
     * @param team team instance
     * @return if it was successful
     */
    public boolean deleteTeam(BBUTeam team) {
        return teams.remove(team);
    }

    /**
     * Retrieve a team based on colour
     *
     * @param colour colour of the team
     * @return BBUTeam
     */
    public BBUTeam getTeam(BBUTeamColour colour) {
        return teams.stream().filter(team -> team.getColour() == colour).findFirst().orElse(null);
    }

    /**
     * Retrieve a team from a player within it
     *
     * @param bbuPlayer player inside the team
     * @return BBUTeam
     */
    public BBUTeam getTeam(BBUPlayer bbuPlayer) {
        return teams.stream().filter(team -> team.getPlayers().contains(bbuPlayer)).findFirst().orElse(null);
    }

    /**
     * Retrieve a team from a bukkit player within it
     *
     * @param player player inside the team
     * @return BBUTeam
     */
    @Nullable
    public BBUTeam getTeam(Player player) {
        return BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId()).getTeam();
    }

    /**
     * Retrieve a team from the beacon position
     *
     * @param beacon Beacon block
     * @return BBUTeam
     */
    public BBUTeam getTeam(Block beacon) {
        return teams.stream().filter(team -> Objects.equals(team.getBeacon(), WorldPosition.fromBukkitLocation(beacon.getLocation()))).findFirst().orElse(null);
    }

    /**
     * @return immutable list of teams with cages already setup
     */
    public List<BBUTeam> getTeamsWithCages() {
        List<BBUTeam> teams = new ArrayList<>();
        for (BBUTeam team : getAllTeams()) {
            if (team.getCage() != null) {
                teams.add(team);
            }
        }

        return ImmutableList.copyOf(teams);
    }

    public void clearTeams() {
        for (BBUTeam team : teams) {
            team.getPlayers().clear();
        }
    }

    /**
     * @return immutable list of all teams
     */
    public ImmutableList<BBUTeam> getAllTeams() {
        return ImmutableList.copyOf(teams);
    }

}
