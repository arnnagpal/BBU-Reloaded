package me.imoltres.bbu.utils.world;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@Getter
@AllArgsConstructor
public class ChunkCuboid {

    private int x;
    private int z;
    private String world;

    /**
     * Decodes the specified string representation of a chunk into a ChunkCuboid
     *
     * @param id String representation of a chunk
     * @return ChunkCuboid
     * @throws IllegalArgumentException When the string does not follow the format "X;Z"
     */
    public static ChunkCuboid valueOf(String id) {
        String[] split = id.split(";");

        if (split.length != 3)
            throw new IllegalArgumentException("Invalid chunk format");

        int x;
        int z;
        String world;

        try {
            x = Integer.parseInt(split[0]);
            z = Integer.parseInt(split[1]);
            world = split[2];
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid chunk format", ex);
        }

        return new ChunkCuboid(x, z, world);
    }

    /**
     * Returns the string representation of a chunk. This is the combination of
     * the X and Z position separated by a colon.
     *
     * @return String
     */
    @Override
    public String toString() {
        return x + ";" + z + ";" + world;
    }

    /**
     * Convert this chunk into a cuboid
     *
     * @return Cuboid
     */
    public Cuboid asCuboid() {
        return new Cuboid(
                new Position(x * 16, 0, z * 16),
                new Position((x * 16) + 16, 255, (z * 16) + 16)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChunkCuboid that = (ChunkCuboid) o;

        return x == that.x && z == that.z && (world != null ? world.equals(that.world) : that.world == null);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + z;
        result = 31 * result + (world != null ? world.hashCode() : 0);
        return result;
    }

    public static class Serializer extends TypeAdapter<ChunkCuboid> {

        @Override
        public void write(JsonWriter writer, ChunkCuboid o) throws IOException {
            writer.value(o.toString());
        }

        @Override
        public ChunkCuboid read(JsonReader reader) throws IOException {
            return ChunkCuboid.valueOf(reader.nextString());
        }

    }
}
