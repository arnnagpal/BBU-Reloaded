package me.imoltres.bbu.data.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.data.BBUTeamColour;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BBUPlayerStatistics {

    private final transient BBUPlayer player;

    private final Set<UUID> kills = new HashSet<>();
    private final Set<BBUPlayerDeath> deaths = new HashSet<>();
    private final Set<BBUTeamColour> beaconsBroken = new HashSet<>();

    private record BBUPlayerDeath(EntityDamageEvent.DamageCause reason) {
    }

}
