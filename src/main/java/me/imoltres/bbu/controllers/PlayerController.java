package me.imoltres.bbu.controllers;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controller to manage all the player instances
 */
@RequiredArgsConstructor
public class PlayerController {
    private final BBU plugin;

    private final Set<BBUPlayer> players = new HashSet<>();

    /**
     * Retrieve a player based on their UUID
     *
     * @param uniqueId UUID of the player
     * @return BBUPlayer
     */
    public BBUPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(bbuPlayer -> bbuPlayer.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Retrieve a player based on their name
     *
     * @param name name of the player
     * @return BBUPlayer
     */
    public BBUPlayer getPlayer(String name) {
        return players.stream().filter(bbuPlayer -> bbuPlayer.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Get the username of a player from the UUID
     *
     * @param uniqueId UUID of the player
     * @return username
     */
    public String getName(UUID uniqueId) {
        //check player set first
        //then check with bukkit
        //check inside user-cache last
        String name = players.stream()
                .filter(bbuPlayer -> bbuPlayer.getUniqueId() == uniqueId)
                .map(BBUPlayer::getName)
                .findFirst()
                .orElse(null);
        if (name != null) {
            return name;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
        if (offlinePlayer.getName() != null) {
            return offlinePlayer.getName();
        }

        return null;
    }

    /**
     * Get the uuid of the player from their name
     *
     * @param name name of the player
     * @return UUID
     */
    public UUID getUniqueId(String name) {
        //check player set first
        //then check with bukkit
        //check inside user-cache last
        UUID uniqueId = players.stream()
                .filter(bbuPlayer -> bbuPlayer.getName().equalsIgnoreCase(name))
                .map(BBUPlayer::getUniqueId)
                .findFirst()
                .orElse(null);
        if (uniqueId != null) {
            return uniqueId;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
        if (offlinePlayer != null) {
            return offlinePlayer.getUniqueId();
        }

        return null;
    }

    /**
     * Create a BBUPlayer for a unique id and a name, though, if it already exists, we ignore it.
     *
     * @param uniqueId uuid of the player
     * @param name     name of the player
     * @return BBUPlayer
     */
    public BBUPlayer createPlayer(UUID uniqueId, String name) {
        BBUPlayer bbuPlayer = getPlayer(uniqueId);
        if (bbuPlayer != null) {
            System.out.printf("Player cache for '%s' already exists, ignoring...\n", name);
            return bbuPlayer;
        }

        bbuPlayer = new BBUPlayer(uniqueId, name);
        players.add(bbuPlayer);

        System.out.printf("Created new player cache for '%s'\n", name);

        return bbuPlayer;
    }

    /**
     * @return an immutable list of all the players
     */
    public List<BBUPlayer> getPlayers() {
        return ImmutableList.copyOf(players);
    }

    /**
     * Delete a BBUPlayer from cache based on the uuid
     *
     * @param uniqueId uniqueId of the player
     * @return if it was successful
     */
    public boolean deletePlayer(UUID uniqueId) {
        return players.remove(getPlayer(uniqueId));
    }

    /**
     * Delete a BBUPlayer from cache based on the name
     *
     * @param name name of the player
     * @return if it was successful
     */
    public boolean deletePlayer(String name) {
        return players.remove(getPlayer(name));
    }

}
