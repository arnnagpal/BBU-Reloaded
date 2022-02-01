package me.imoltres.bbu.data.team

import me.imoltres.bbu.utils.world.Cuboid
import me.imoltres.bbu.utils.world.WorldPosition

class BBUCage(val team: BBUTeam, val cuboid: Cuboid, val spawnPosition: WorldPosition) {
    override fun toString(): String {
        return "BBUCage {team=" + team.colour.name + ", cuboid=" + cuboid.toString() + ", spawnPos=" + spawnPosition.toString() + "}"
    }
}