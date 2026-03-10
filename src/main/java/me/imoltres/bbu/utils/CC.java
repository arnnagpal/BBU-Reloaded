package me.imoltres.bbu.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A short form for {@link LegacyColor}
 * contains some useful string utilities as well.
 */
public class CC {
    public static final String BLUE = LegacyColor.BLUE.toString();
    public static final String AQUA = LegacyColor.AQUA.toString();
    public static final String YELLOW = LegacyColor.YELLOW.toString();
    public static final String RED = LegacyColor.RED.toString();
    public static final String GRAY = LegacyColor.GRAY.toString();
    public static final String GOLD = LegacyColor.GOLD.toString();
    public static final String GREEN = LegacyColor.GREEN.toString();
    public static final String WHITE = LegacyColor.WHITE.toString();
    public static final String BLACK = LegacyColor.BLACK.toString();
    public static final String BOLD = LegacyColor.BOLD.toString();
    public static final String ITALIC = LegacyColor.ITALIC.toString();
    public static final String UNDER_LINE = LegacyColor.UNDERLINE.toString();
    public static final String STRIKE_THROUGH = LegacyColor.STRIKETHROUGH.toString();
    public static final String RESET = LegacyColor.RESET.toString();
    public static final String MAGIC = LegacyColor.MAGIC.toString();
    public static final String DARK_BLUE = LegacyColor.DARK_BLUE.toString();
    public static final String DARK_AQUA = LegacyColor.DARK_AQUA.toString();
    public static final String DARK_GRAY = LegacyColor.DARK_GRAY.toString();
    public static final String DARK_GREEN = LegacyColor.DARK_GREEN.toString();
    public static final String DARK_PURPLE = LegacyColor.DARK_PURPLE.toString();
    public static final String DARK_RED = LegacyColor.DARK_RED.toString();
    public static final String PINK = LegacyColor.LIGHT_PURPLE.toString();

    public static final String SB_DIV = STRIKE_THROUGH + "----------------------";

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
    @NotNull
    public static TextComponent translate(String in) {
        return LegacyComponentSerializer.legacy('&').deserialize(in);
    }

    /**
     * Converts a legacy colour coded string array list to the new kyori adventure system and returns a list of components (TextComponent)
     *
     * @param lines Original legacy colour coded string array list
     * @return List of text components using the new kyori adventure text library
     */
    public static List<Component> translate(List<String> lines) {
        List<Component> toReturn = new ArrayList<>();
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
    public static List<Component> translate(String[] lines) {
        final List<Component> toReturn = new ArrayList<>();
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
        return msg.substring(0, 1).toUpperCase() + msg.substring(1).toLowerCase();
    }

    /**
     * Returns true if the given string is an integer in the given radix
     *
     * @param s     str
     * @param radix radix
     * @return Boolean
     */
    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }

            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
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
