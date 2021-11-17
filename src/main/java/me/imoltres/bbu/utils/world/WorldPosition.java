package me.imoltres.bbu.utils.world;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.io.IOException;

/**
 * Represents a location in a world. This is a generic base object that acts as
 * a bukkit-like location.
 */
public class WorldPosition extends Position {

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
    public WorldPosition(double x, double y, double z, String worldName) {
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
    public WorldPosition(double x, double y, double z, float yaw, float pitch, String worldName) {
        super(x, y, z, yaw, pitch);
        this.world = worldName;
    }

    /**
     * Converts a WorldPosition to a bukkit Location.
     *
     * @param worldPosition WorldPosition
     * @return bukkit location
     */
    public static Location toBukkitLocation(WorldPosition worldPosition) {
        return new Location(Bukkit.getWorld(worldPosition.getWorld()), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getYaw(), worldPosition.getPitch());
    }

    public boolean isSafe() {
        try {
            Block feet = getBlock();
            if (!feet.getType().isOccluding() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isOccluding()) {
                return false;
            }
            Block head = feet.getRelative(BlockFace.UP);
            if (!head.getType().isOccluding()) {
                return false;
            }
            Block ground = feet.getRelative(BlockFace.DOWN);

            return ground.getType().isSolid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Converts a WorldPosition to a bukkit Location.
     *
     * @return bukkit location
     */
    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(world), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    public Block getBlock() {
        return toBukkitLocation().getBlock();
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
            out.value(value.getY());
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
