package me.imoltres.bbu.data.team

import me.imoltres.bbu.utils.world.Cuboid
import me.imoltres.bbu.utils.world.WorldPosition

/**
 * Represents a cage for a team
 */
class BBUCage(
    val team: BBUTeam,
    val cuboid: Cuboid,
    spawnPosition: WorldPosition
) {

    val spawnPosition: WorldPosition = spawnPosition

    override fun toString(): String {
        return "BBUCage {team=" + team.colour.name + ", cuboid=" + cuboid.toString() + ", spawnPos=" + spawnPosition.toString() + "}"
    }
}