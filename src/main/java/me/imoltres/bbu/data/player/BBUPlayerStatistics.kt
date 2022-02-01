package me.imoltres.bbu.data.player

import me.imoltres.bbu.data.BBUTeamColour
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

class BBUPlayerStatistics(@Transient val player: BBUPlayer) {
    val kills: Set<UUID> = HashSet()
    val deaths: Set<BBUPlayerDeath> = HashSet()
    val beaconsBroken: Set<BBUTeamColour> = HashSet()

    inner class BBUPlayerDeath(val reason: EntityDamageEvent.DamageCause)
}