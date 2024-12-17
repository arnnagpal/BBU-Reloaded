package me.imoltres.bbu.utils.schematic;


import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
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
    public static HashMap<Position, Material> parseSchematic(CompoundTag schematic) {
        HashMap<Integer, Material> palette = new HashMap<>();

        CompoundTag paletteTag = schematic.get("Palette");
        if (paletteTag != null) {
            parsePalette(palette, paletteTag);
        }

        ByteArrayTag blockData = schematic.get("BlockData");
        if (blockData == null) {
            return null;
        }

        HashMap<Position, Material> output = new HashMap<>();

        int width = ((ShortTag) schematic.get("Width")).getValue();
        int length = ((ShortTag) schematic.get("Length")).getValue();

        int i = 0;
        for (byte b : blockData.getValue()) {
            int id = b & 0xFF;
            Material mat = palette.get(id);

            int x = i % width;
            int y = i / (length * width);
            int z = (i / width) % length;

            System.out.println("Material: " + mat + " X: " + x + " Y: " + y + " Z: " + z);

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

            IntTag id = paletteTag.get(matStr);
            palette.put(id.getValue(), mat);

            System.out.println("Material: " + mat + " ID: " + id.getValue());
        }
    }
}
