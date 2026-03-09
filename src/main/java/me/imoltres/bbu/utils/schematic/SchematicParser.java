package me.imoltres.bbu.utils.schematic;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import me.imoltres.bbu.utils.world.Position;
import org.bukkit.Material;

import java.util.HashMap;

public class SchematicParser {

    /**
     * Very barebones schematic parser, only parses the blocks and returns their positions and materials.
     *
     * @param schematic
     * @return HashMap of positions (relative to the origin) and materials
     */
    public static HashMap<Position, Material> parseSchematic(com.viaversion.nbt.tag.CompoundTag schematic) {
        HashMap<Integer, Material> palette = new HashMap<>();

        CompoundTag paletteTag = schematic.getCompoundTag("Palette");
        if (paletteTag != null) {
            parsePalette(palette, paletteTag);
        }

        ByteArrayTag blockData = schematic.getByteArrayTag("BlockData");
        if (blockData == null) {
            System.out.println("BlockData tag not found in schematic");
            return null;
        }

        HashMap<Position, Material> output = new HashMap<>();

        int width = schematic.getShortTag("Width").getValue();
        int length = schematic.getShortTag("Length").getValue();

        int i = 0;
        for (byte b : blockData.getValue()) {
            int id = b & 0xFF;
            Material mat = palette.get(id);

            int x = i % width;
            int y = i / (length * width);
            int z = (i / width) % length;

            output.put(new Position(x, y, z), mat);

            i++;
        }

        return output;
    }

    private static void parsePalette(HashMap<Integer, Material> palette, CompoundTag paletteTag) {
        for (String matStr : paletteTag.keySet()) {
            Material mat = Material.matchMaterial(matStr);
            if (mat == null) {
                continue;
            }

            IntTag id = paletteTag.getIntTag(matStr);
            palette.put(id.getValue(), mat);
        }
    }
}
