package me.imoltres.bbu.data.team;

import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.game.GameState;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.GsonFactory;
import me.imoltres.bbu.utils.world.Cuboid;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

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

    public BBUTeam(BBUTeamColour colour) {
        this.colour = colour;

        setupCage();
    }

    private void setupCage() {
        String cageStr = BBU.getInstance().getTeamSpawnsConfig().getString("team." + colour.name());
        if (cageStr != null && !cageStr.isEmpty()) {
            Cuboid cuboid = GsonFactory.getCompactGson().fromJson(cageStr, Cuboid.class);
            this.cage = new BBUCage(this, cuboid, cuboid.getCenter().toWorldPosition(Bukkit.getWorlds().get(0).getName()));
        } else {
            this.cage = null;
        }
    }

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
        if (BBU.getInstance().getGame().getGameState() == GameState.PRE_GAME)
            return true;
        return beacon;
    }

}
