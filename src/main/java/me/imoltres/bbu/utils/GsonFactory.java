package me.imoltres.bbu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.imoltres.bbu.utils.world.ChunkCuboid;
import me.imoltres.bbu.utils.world.Cuboid;
import me.imoltres.bbu.utils.world.Position;
import me.imoltres.bbu.utils.world.WorldPosition;

public class GsonFactory {
    private static final Gson g = new Gson();

    private final static String CLASS_KEY = "SERIAL-ADAPTER-CLASS-KEY";

    private static Gson prettyGson;
    private static Gson compactGson;

    /**
     * Returns a Gson instance for use anywhere with new line pretty printing
     * <p>
     * Use @GsonIgnore in order to skip serialization and deserialization
     * </p>
     *
     * @return a Gson instance
     */
    public static Gson getPrettyGson() {
        if (prettyGson == null)
            prettyGson = new GsonBuilder().enableComplexMapKeySerialization()
                    .registerTypeAdapter(Position.class, new Position.Serializer())
                    .registerTypeAdapter(WorldPosition.class, new WorldPosition.Serializer())
                    .registerTypeAdapter(Cuboid.class, new Cuboid.Serializer())
                    .registerTypeAdapter(ChunkCuboid.class, new ChunkCuboid.Serializer())
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
        return prettyGson;
    }

    /**
     * Returns a Gson instance for use anywhere with one line strings
     * <p>
     * Use @GsonIgnore in order to skip serialization and deserialization
     * </p>
     *
     * @return a Gson instance
     */
    public static Gson getCompactGson() {
        if (compactGson == null)
            compactGson = new GsonBuilder().enableComplexMapKeySerialization()
                    .registerTypeAdapter(Position.class, new Position.Serializer())
                    .registerTypeAdapter(WorldPosition.class, new WorldPosition.Serializer())
                    .registerTypeAdapter(Cuboid.class, new Cuboid.Serializer())
                    .registerTypeAdapter(ChunkCuboid.class, new ChunkCuboid.Serializer())
                    .disableHtmlEscaping()
                    .create();
        return compactGson;
    }

}
