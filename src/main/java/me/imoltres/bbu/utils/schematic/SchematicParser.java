package me.imoltres.bbu.utils.schematic;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.world.Position;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;

public class SchematicParser {

    /**
     * Very barebones schematic parser, only parses the blocks and returns their positions and materials.
     *
     * @param schematicHead the compound tag that relates to to the schematic file's root
     * @return HashMap of positions (relative to the origin) and materials
     */
    public static HashMap<Position, BlockData> parseSchematic(com.viaversion.nbt.tag.CompoundTag schematicHead) {
        CompoundTag schematic = schematicHead.getCompoundTag("Schematic");
        if (schematic == null) {
            // invalid format
            BBU.getInstance().getLogger().severe("Invalid schematic format: Root tag is not a compound tag");
            return null;
        }

        // get version int
        IntTag version = schematic.getIntTag("Version");
        if (version == null || version.asInt() != 3) {
            BBU.getInstance().getLogger().severe("Unsupported schematic version: " + (version != null ? version.asInt() : "null"));
            return null;
        }

        CompoundTag blocks = schematic.getCompoundTag("Blocks");
        if (blocks == null) {
            // invalid format
            BBU.getInstance().getLogger().severe("Invalid schematic format: Blocks tag not found");
            return null;
        }

        HashMap<Integer, BlockData> palette = new HashMap<>();
        CompoundTag paletteTag = blocks.getCompoundTag("Palette");
        if (paletteTag != null) {
            parsePalette(palette, paletteTag);
        }

        ByteArrayTag blockData = blocks.getByteArrayTag("Data");
        if (blockData == null) {
            System.out.println("Invalid schematic format: Data tag not found");
            return null;
        }

        HashMap<Position, BlockData> output = new HashMap<>();

        int width = schematic.getShortTag("Width").asShort();
        int length = schematic.getShortTag("Length").asShort();

        int i = 0;
        for (byte b : blockData.getValue()) {
            int id = b & 0xFF;
            var data = palette.get(id);

            int x = i % width;
            int y = i / (length * width);
            int z = (i / width) % length;

            output.put(new Position(x, y, z), data);

            i++;
        }

        return output;
    }

    private static void parsePalette(HashMap<Integer, BlockData> palette, CompoundTag paletteTag) {
        for (String matStr : paletteTag.keySet()) {
            BlockData data;
            try {
                data = Bukkit.getServer().createBlockData(matStr);
            } catch (IllegalArgumentException e) {
                BBU.getInstance().getLogger().severe("Invalid material in palette: " + matStr);
                continue;
            }

            IntTag id = paletteTag.getIntTag(matStr);
            if (id == null) {
                BBU.getInstance().getLogger().severe("ID tag not found for material: " + matStr);
                continue;
            }

            palette.put(id.asInt(), data);
        }
    }
}
