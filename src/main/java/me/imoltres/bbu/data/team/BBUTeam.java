package me.imoltres.bbu.data.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.CC;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Setter
public class BBUTeam {
    @Getter
    private final BBUTeamColour colour;

    @Getter
    private BBUCage cage;

    @Getter
    private final Set<BBUPlayer> players = new HashSet<>();

    @Setter
    private boolean beacon = true;

    public boolean addPlayer(BBUPlayer player) {
        System.out.printf("%s has joined team '%s'\n", player.getName(), CC.capitalize(colour.name().toLowerCase()));
        player.setTeam(this);
        return players.add(player);
    }

    public boolean removePlayer(BBUPlayer player) {
        System.out.printf("%s has left team '%s'\n", player.getName(), CC.capitalize(colour.name().toLowerCase()));
        player.setTeam(null);
        return players.remove(player);
    }

    public boolean hasBeacon() {
        return beacon;
    }

}
