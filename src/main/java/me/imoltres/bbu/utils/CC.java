package me.imoltres.bbu.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class CC {
    public static final String BLUE = ChatColor.BLUE.toString();
    public static final String AQUA = ChatColor.AQUA.toString();
    public static final String YELLOW = ChatColor.YELLOW.toString();
    public static final String RED = ChatColor.RED.toString();
    public static final String GRAY = ChatColor.GRAY.toString();
    public static final String GOLD = ChatColor.GOLD.toString();
    public static final String GREEN = ChatColor.GREEN.toString();
    public static final String WHITE = ChatColor.WHITE.toString();
    public static final String BLACK = ChatColor.BLACK.toString();
    public static final String BOLD = ChatColor.BOLD.toString();
    public static final String ITALIC = ChatColor.ITALIC.toString();
    public static final String UNDER_LINE = ChatColor.UNDERLINE.toString();
    public static final String STRIKE_THROUGH = ChatColor.STRIKETHROUGH.toString();
    public static final String RESET = ChatColor.RESET.toString();
    public static final String MAGIC = ChatColor.MAGIC.toString();
    public static final String DARK_BLUE = ChatColor.DARK_BLUE.toString();
    public static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
    public static final String DARK_GRAY = ChatColor.DARK_GRAY.toString();
    public static final String DARK_GREEN = ChatColor.DARK_GREEN.toString();
    public static final String DARK_PURPLE = ChatColor.DARK_PURPLE.toString();
    public static final String DARK_RED = ChatColor.DARK_RED.toString();
    public static final String PINK = ChatColor.LIGHT_PURPLE.toString();

    /**
     * Converts a textcomponent to the legacy colour coded string
     *
     * @param in TextComponent to be converted
     * @return A legacy colour coded String
     */
    public static String translateLegacy(TextComponent in) {
        return LegacyComponentSerializer.legacy('&').serialize(in);
    }

    /**
     * Converts a legacy colour coded string to the new kyori adventure system and returns a converted component (TextComponent)
     *
     * @param in Original legacy colour coded string
     * @return A text component using the new kyori adventure text library
     */
    public static TextComponent translate(String in) {
        return LegacyComponentSerializer.legacy('&').deserialize(in);
    }

    /**
     * Converts a legacy colour coded string array list to the new kyori adventure system and returns a list of components (TextComponent)
     *
     * @param lines Original legacy colour coded string array list
     * @return List of text components using the new kyori adventure text library
     */
    public static List<TextComponent> translate(List<String> lines) {
        List<TextComponent> toReturn = new ArrayList<>();
        for (String line : lines) {
            toReturn.add(translate(line));
        }
        return toReturn;
    }

    /**
     * Converts a legacy colour coded string array to the new kyori adventure system and returns a list of components (TextComponent)
     *
     * @param lines Original legacy colour coded string array
     * @return List of text components using the new kyori adventure text library
     */
    public static List<TextComponent> translate(String[] lines) {
        final List<TextComponent> toReturn = new ArrayList<>();
        for (String line : lines) {
            if (line != null) {
                toReturn.add(translate(line));
            }
        }
        return toReturn;
    }

    /**
     * Capitalizes the first character in the string
     *
     * @param msg Message
     * @return String
     */
    public static String capitalize(String msg) {
        return msg.substring(0, 1).toUpperCase() + msg.substring(1);
    }

    /**
     * Keeps splitting the given string after the given length.
     *
     * @param str        String
     * @param lineLength Length
     * @return String[]
     */
    public static String[] wrapString(String str, int lineLength) {
        List<String> list = new ArrayList<>();
        int idx = 0;
        StringBuilder sb = null;

        for (int i = 0; i < str.length(); i++) {
            if (idx == 0) {
                sb = new StringBuilder();
            }

            String let = str.substring(i, i + 1);

            if (let.equals("|") || (idx >= lineLength && let.equals(" ")) || i + 1 == str.length()) {
                if (i + 1 == str.length()) {
                    sb.append(let);
                }

                list.add(sb.toString());
                idx = 0;
            } else {
                sb.append(let);
                idx++;
            }
        }

        return list.toArray(new String[0]);
    }

}
