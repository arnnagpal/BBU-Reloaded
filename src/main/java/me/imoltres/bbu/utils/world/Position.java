package me.imoltres.bbu.utils.world;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Represents a location in a world. This is a generic base object that
 * acts as a spigot/bukkit location.
 */
public class Position {

    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    /**
     * Create a new position with the specified x, y and z units.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Create a new position with the specified x, y and z units. Besides the position,
     * the pitch and yaw is also given.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    public Position(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public Position add(Position position) {
        this.x += position.getX();
        this.y += position.getY();
        this.z += position.getZ();

        return this;
    }

    public Position add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    public Position subtract(Position position) {
        this.x -= position.getX();
        this.y -= position.getY();
        this.z -= position.getZ();

        return this;
    }

    public Position subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    public WorldPosition toWorldPosition(String worldName) {
        return new WorldPosition(x, y, z, worldName);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Position && ((Position) o).x == x && ((Position) o).y == y && ((Position) o).z == z;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }

    public static class Serializer extends TypeAdapter<Position> {

        @Override
        public void write(JsonWriter out, Position value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginArray();
            out.value(value.x);
            out.value(value.y);
            out.value(value.z);
            out.value(value.pitch);
            out.value(value.yaw);
            out.endArray();
        }

        @Override
        public Position read(JsonReader in) throws IOException {
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

            in.endArray();

            return new Position(x, y, z, yaw, pitch);
        }

    }

}
