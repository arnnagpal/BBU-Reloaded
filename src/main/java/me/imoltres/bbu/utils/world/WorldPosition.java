package me.imoltres.bbu.utils.world;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.papermc.paper.math.FinePosition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.io.IOException;
import java.util.Set;

/**
 * Represents a location in a world. This is a generic base object that acts as
 * a bukkit-like location.
 */
public class WorldPosition extends Position {

    private static final Set<Biome> BAD_BIOMES = Set.of(
            Biome.SWAMP,
            Biome.MUSHROOM_FIELDS,
            Biome.BEACH,
            Biome.RIVER,
            Biome.FROZEN_RIVER,

            //oceans
            Biome.OCEAN,
            Biome.DEEP_OCEAN,
            Biome.COLD_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN,
            Biome.FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.WARM_OCEAN
    );

    @Getter
    @Setter
    @NonNull
    private String world;

    /**
     * Create a new WorldPosition for the specified x, y and z coordinates alongside the given
     * world name.
     *
     * @param x         x-pos
     * @param y         y-pos
     * @param z         z-pos
     * @param worldName World
     */
    public WorldPosition(double x, double y, double z, @NonNull String worldName) {
        super(x, y, z);
        this.world = worldName;
    }

    /**
     * Create a new WorldPosition for the specified x, y, z yaw and pitch coordinates alongside the given
     * world name.
     *
     * @param x         x-pos
     * @param y         y-pos
     * @param z         z-pos
     * @param yaw       yaw
     * @param pitch     pitch
     * @param worldName World
     */
    public WorldPosition(double x, double y, double z, float yaw, float pitch, @NonNull String worldName) {
        super(x, y, z, yaw, pitch);
        this.world = worldName;
    }

    public static WorldPosition fromBukkitLocation(Location location) {
        return new WorldPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.getWorld().getName());
    }

    public static WorldPosition fromFinePosition(FinePosition location, World world) {
        return new WorldPosition(location.x(), location.y(), location.z(), world.getName());
    }

    public boolean isSafe(int width, int height) {
        World w = Bukkit.getWorld(world);
        if (w == null) return false;

        int bx = (int) getX(), by = (int) getY(), bz = (int) getZ();

        // cheap check first
        if (BAD_BIOMES.contains(w.getBlockAt(bx, by, bz).getBiome())) return false;

        for (int x = bx - width; x <= bx + width; x++) {
            for (int y = by; y <= by + height; y++) {
                for (int z = bz - width; z <= bz + width; z++) {
                    Block feet = w.getBlockAt(x, y, z);
                    Block head = w.getBlockAt(x, y + 1, z);
                    Block ground = w.getBlockAt(x, y - 1, z);

                    if (head.getType().isOccluding()) return false;
                    if (ground.getType() == Material.LAVA) return false;
                    if (BAD_BIOMES.contains(feet.getBiome())) return false;
                }
            }
        }
        return true;
    }

    /**
     * Distance between two WorldPositions.
     *
     * @param other Other WorldPosition
     * @return distance
     */
    public double distance(WorldPosition other) {
        return Math.sqrt(Math.pow(getX() - other.getX(), 2) + Math.pow(getY() - other.getY(), 2) + Math.pow(getZ() - other.getZ(), 2));
    }

    /**
     * Converts a WorldPosition to a bukkit Location.
     *
     * @return bukkit location
     */
    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(world), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    /**
     * Converts a WorldPosition to a bukkit Location.
     *
     * @param worldPosition WorldPosition
     * @return bukkit location
     */
    public Location toBukkitLocation(WorldPosition worldPosition) {
        return new Location(Bukkit.getWorld(worldPosition.getWorld()), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getYaw(), worldPosition.getPitch());
    }

    public Block getBlock() {
        return toBukkitLocation().getBlock();
    }

    @Override
    public String toString() {
        return "WorldPosition{" +
                "world='" + world + '\'' +
                "x='" + getX() + '\'' +
                ", y='" + getY() + '\'' +
                ", z='" + getZ() + '\'' +
                ", pitch='" + getPitch() + '\'' +
                ", yaw='" + getYaw() + '\'' +
                '}';
    }

    public static class Serializer extends TypeAdapter<WorldPosition> {

        @Override
        public void write(JsonWriter out, WorldPosition value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginArray();
            out.value(value.getX());
            out.value(value.getY());
            out.value(value.getZ());
            out.value(value.getPitch());
            out.value(value.getYaw());
            out.value(value.getWorld());
            out.endArray();
        }

        @Override
        public WorldPosition read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            in.beginArray();

            double x = in.nextDouble();
            double y = in.nextDouble();
            double z = in.nextDouble();
            float pitch = (float) in.nextDouble();
            float yaw = (float) in.nextDouble();
            String world = in.nextString();

            in.endArray();

            return new WorldPosition(x, y, z, yaw, pitch, world);
        }
    }
}
