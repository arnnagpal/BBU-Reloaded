package me.imoltres.bbu.game.generator;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * An empty chunk generator. (tldr; void gen)
 */
public class EmptyChunkGenerator extends ChunkGenerator {
    @Override
    public BiomeProvider getDefaultBiomeProvider(@NonNull WorldInfo worldInfo) {
        return new VoidBiomeProvider(Biome.PLAINS);
    }

    @Override
    public Location getFixedSpawnLocation(@NonNull World world, @NonNull Random random) {
        return new Location(world, 0, 72, 0);
    }

    private static class VoidBiomeProvider extends BiomeProvider {
        private final Biome biome;

        public VoidBiomeProvider(Biome paramBiome) {
            this.biome = paramBiome;
        }

        @Override
        public @NonNull Biome getBiome(@NonNull WorldInfo worldInfo, int x, int y, int z) {
            return this.biome;
        }

        @Override
        public @NonNull List<Biome> getBiomes(@NonNull WorldInfo worldInfo) {
            return Collections.singletonList(this.biome);
        }
    }
}
