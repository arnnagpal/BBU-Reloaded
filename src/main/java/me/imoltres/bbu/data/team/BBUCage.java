package me.imoltres.bbu.data.team;

import me.imoltres.bbu.utils.world.Cuboid;
import me.imoltres.bbu.utils.world.WorldPosition;

public record BBUCage(BBUTeam team, Cuboid cuboid, WorldPosition spawnPosition) {

    @Override
    public String toString() {
        return "BBUCage {team=" + team.getColour().name() + ", cuboid=" + cuboid.toString() + ", spawnPos=" + spawnPosition.toString() + "}";
    }
}
