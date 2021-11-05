package me.imoltres.bbu.utils.world;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.imoltres.bbu.utils.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Construct a new cuboid that exists of 2 positions
 */
public class Cuboid implements Iterable<Position> {

    private Position min;
    private Position max;

    public Cuboid(Position min, Position max) {
        this.min = min;
        this.max = max;
    }

    public Position getMin() {
        return min;
    }

    public void setMin(Position min) {
        this.min = min;
    }

    public Position getMax() {
        return max;
    }

    public void setMax(Position max) {
        this.max = max;
    }

    /**
     * Return a grown version of this cuboid
     *
     * @param x X delta
     * @param y Y delta
     * @param z Z delta
     * @return New Cuboid
     */
    public Cuboid grow(double x, double y, double z) {
        Position newMin = new Position(min.getX() - x, min.getY() - y, min.getZ() - z);
        Position newMax = new Position(max.getX() + x, max.getY() + y, max.getZ() + z);

        return new Cuboid(newMin, newMax);
    }

    /**
     * Returns a grown version of this cuboid
     *
     * @param delta Delta
     * @return New Cuboid
     */
    public Cuboid grow(double delta) {
        return grow(delta, delta, delta);
    }

    /**
     * Return a shrunken version of this cuboid
     *
     * @param x X delta
     * @param y Y delta
     * @param z Z delta
     * @return New Cuboid
     */
    public Cuboid shrink(double x, double y, double z) {
        return grow(-x, -y, -z);
    }

    /**
     * Returns a shrunken version of this cuboid
     *
     * @param delta Delta
     * @return New Cuboid
     */
    public Cuboid shrink(double delta) {
        return shrink(delta, delta, delta);
    }

    /**
     * Returns the cuboid width on the X axis
     *
     * @return double
     */
    public double getWidthX() {
        return Math.abs(min.getX() - max.getX());
    }

    /**
     * Returns the cuboid width on the Z axis
     *
     * @return double
     */
    public double getWidthZ() {
        return Math.abs(min.getZ() - max.getZ());
    }

    /**
     * Returns the cuboid height on the Y axis
     *
     * @return double
     */
    public double getHeight() {
        return Math.abs(min.getY() - max.getY());
    }

    /**
     * Return all positions contained within this cuboid
     *
     * @return Position list
     */
    public List<Position> contents() {
        List<Position> content = new ArrayList<>();

        for (double x = Math.min(min.getX(), max.getX()); x <= Math.max(min.getX(), max.getX()); x++) {
            for (double y = Math.min(min.getY(), max.getY()); y <= Math.max(min.getY(), max.getY()); y++) {
                for (double z = Math.min(min.getZ(), max.getZ()); z <= Math.max(min.getZ(), max.getZ()); z++) {
                    content.add(new Position(x, y, z));
                }
            }
        }

        return content;
    }

    /**
     * Returns true if the given Position is located within the bounds
     * of this cuboid.
     *
     * @param position Position
     * @return boolean
     */
    public boolean contains(Position position) {
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        double x1 = Math.min(min.getX(), max.getX());
        double y1 = Math.min(min.getY(), max.getY());
        double z1 = Math.min(min.getZ(), max.getZ());
        double x2 = Math.max(min.getX(), max.getX());
        double y2 = Math.max(min.getY(), max.getY());
        double z2 = Math.max(min.getZ(), max.getZ());

        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    /**
     * Returns a random position from this Cuboid
     *
     * @return Random position
     */
    public Position randomPosition() {
        Random random = ThreadLocalRandom.current();

        double deltaX = max.getX() - min.getX();
        double deltaY = max.getY() - min.getY();
        double deltaZ = max.getZ() - min.getZ();

        double rx = random.nextDouble() * deltaX + min.getX();
        double ry = random.nextDouble() * deltaY + min.getY();
        double rz = random.nextDouble() * deltaZ + min.getZ();

        return new Position(rx, ry, rz);
    }

    @Override
    public String toString() {
        return "Cuboid{" +
                "min=" + min +
                ", max=" + max +
                ", widthX=" + getWidthX() +
                ", widthZ=" + getWidthZ() +
                ", height=" + getHeight() +
                '}';
    }

    @Override
    public Iterator<Position> iterator() {
        return contents().iterator();
    }

    public static class Serializer extends TypeAdapter<Cuboid> {

        @Override
        public void write(JsonWriter out, Cuboid value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginObject();

            out.name("min");
            out.value(GsonFactory.getCompactGson().toJson(value.min));

            out.name("max");
            out.value(GsonFactory.getCompactGson().toJson(value.max));

            out.endObject();
        }

        @Override
        public Cuboid read(JsonReader reader) throws IOException {
            reader.beginObject();
            Position min = null;
            Position max = null;
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("min")) {
                    min = GsonFactory.getCompactGson().fromJson(reader.nextString(), Position.class);
                } else if (name.equals("max")) {
                    max = GsonFactory.getCompactGson().fromJson(reader.nextString(), Position.class);
                }

            }
            reader.endObject();

            if (min == null || max == null)
                return null;

            return new Cuboid(min, max);
        }

    }
}
