package me.imoltres.bbu.utils.world;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Represents a location on a 2d plane. This is a generic base object that
 * acts as a spigot/bukkit location.
 */
public class Position2D {

    private double x;
    private double y;
    private float pitch;
    private float yaw;

    /**
     * Create a new position with the specified x, y and z units.
     *
     * @param x X position
     * @param z Z position
     */
    public Position2D(double x, double z) {
        this.x = x;
        this.y = z;
    }

    /**
     * Create a new position with the specified x, y and z units. Besides the position,
     * the pitch and yaw is also given.
     *
     * @param x X position
     * @param z Z position
     */
    public Position2D(double x, double z, float yaw, float pitch) {
        this.x = x;
        this.y = z;
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

    public double distance(Position2D p) {
        return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
    }

    @Override
    public String toString() {
        return "Position2D{" +
                "x=" + x +
                ", z=" + y +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }

    public static class Serializer extends TypeAdapter<Position2D> {

        @Override
        public void write(JsonWriter out, Position2D value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginArray();
            out.value(value.x);
            out.value(value.y);
            out.value(value.pitch);
            out.value(value.yaw);
            out.endArray();
        }

        @Override
        public Position2D read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            in.beginArray();

            double x = in.nextDouble();
            double y = in.nextDouble();
            float pitch = (float) in.nextDouble();
            float yaw = (float) in.nextDouble();

            in.endArray();

            return new Position2D(x, y, yaw, pitch);
        }

    }

}
