package me.imoltres.bbu.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.imoltres.bbu.utils.world.ChunkCuboid;
import me.imoltres.bbu.utils.world.Cuboid;
import me.imoltres.bbu.utils.world.Position;
import me.imoltres.bbu.utils.world.WorldPosition;

/**
 * Create a (de)serialiser in two formats,
 * Pretty & Compact
 */
public class GsonFactory {
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
            prettyGson = getBuilder()
                    .setPrettyPrinting()
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
            compactGson = getBuilder()
                    .create();
        return compactGson;
    }

    /**
     * Returns a GSON builder that can modified
     *
     * @return a {@link com.google.gson.GsonBuilder} instance
     */
    private static GsonBuilder getBuilder() {
        return new GsonBuilder().enableComplexMapKeySerialization()
                .registerTypeAdapter(Position.class, new Position.Serializer())
                .registerTypeAdapter(WorldPosition.class, new WorldPosition.Serializer())
                .registerTypeAdapter(Cuboid.class, new Cuboid.Serializer())
                .registerTypeAdapter(ChunkCuboid.class, new ChunkCuboid.Serializer())
                .disableHtmlEscaping();
    }

}
