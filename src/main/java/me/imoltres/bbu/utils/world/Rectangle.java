package me.imoltres.bbu.utils.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Construct a new rectangular shape that exists of 2 positions (2d)
 */
public class Rectangle implements Iterable<Position2D> {

    private Position2D min;
    private Position2D max;

    public Rectangle(Position2D min, Position2D max) {
        this.min = min;
        this.max = max;
    }

    public Position2D getMin() {
        return min;
    }

    public void setMin(Position2D min) {
        this.min = min;
    }

    public Position2D getMax() {
        return max;
    }

    public void setMax(Position2D max) {
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
    public Rectangle grow(double x, double y, double z) {
        Position2D newMin = new Position2D(min.getX() - x, min.getY() - z);
        Position2D newMax = new Position2D(max.getX() + x, max.getY() + z);

        return new Rectangle(newMin, newMax);
    }

    /**
     * Returns a grown version of this cuboid
     *
     * @param delta Delta
     * @return New Cuboid
     */
    public Rectangle grow(double delta) {
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
    public Rectangle shrink(double x, double y, double z) {
        return grow(-x, -y, -z);
    }

    /**
     * Returns a shrunken version of this cuboid
     *
     * @param delta Delta
     * @return New Cuboid
     */
    public Rectangle shrink(double delta) {
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
        return Math.abs(min.getY() - max.getY());
    }

    /**
     * Return all positions contained within this cuboid
     *
     * @return Position list
     */
    public List<Position2D> contents() {
        List<Position2D> content = new ArrayList<>();

        for (double x = Math.min(min.getX(), max.getX()); x <= Math.max(min.getX(), max.getX()); x++) {
            for (double z = Math.min(min.getY(), max.getY()); z <= Math.max(min.getY(), max.getY()); z++) {
                content.add(new Position2D(x, z));
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
    public boolean contains(Position2D position) {
        double x = position.getX();
        double z = position.getY();

        double x1 = Math.min(min.getX(), max.getX());
        double z1 = Math.min(min.getY(), max.getY());
        double x2 = Math.max(min.getX(), max.getX());
        double z2 = Math.max(min.getY(), max.getY());

        return x >= x1 && x <= x2 && z >= z1 && z <= z2;
    }

    /**
     * Returns a random position from this Cuboid
     *
     * @return Random position
     */
    public Position2D randomPosition() {
        Random random = ThreadLocalRandom.current();

        double deltaX = max.getX() - min.getX();
        double deltaZ = max.getY() - min.getY();

        double rx = random.nextDouble() * deltaX + min.getX();
        double rz = random.nextDouble() * deltaZ + min.getY();

        return new Position2D(rx, rz);
    }

    @Override
    public String toString() {
        return "Cuboid{" +
                "min=" + min +
                ", max=" + max +
                ", widthX=" + getWidthX() +
                ", widthZ=" + getWidthZ() +
                '}';
    }

    @Override
    public Iterator<Position2D> iterator() {
        return contents().iterator();
    }
}
