package me.imoltres.bbu.controllers;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerController {
    private final BBU plugin;

    private final Set<BBUPlayer> players = new HashSet<>();

    public BBUPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(bbuPlayer -> bbuPlayer.getUniqueId() == uniqueId).findFirst().orElse(null);
    }

    public BBUPlayer getPlayer(String name) {
        return players.stream().filter(bbuPlayer -> bbuPlayer.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

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

    public boolean deletePlayer(UUID uniqueId) {
        return players.remove(getPlayer(uniqueId));
    }

    public boolean deletePlayer(String name) {
        return players.remove(getPlayer(name));
    }

}
