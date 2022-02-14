package me.imoltres.bbu.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Contains the most globally used items
 */
public class ItemConstants {

    /**
     * Tracking compass item represented as a constant
     * <br>
     * Allows for identification and separation of a normal compass and a tracking compass.
     * <p>
     * Used to track players or any team.
     * Can also be used to track any coordinates that a teammate or ally provides. (command undecided, but likely `/track x y z`)
     */
    public static final ItemStack TRACKING_COMPASS =
            new ItemBuilder(Material.COMPASS)
                    .name("&cTracking Compass")
                    .lore("&7Track nearest player / any team.")
                    .localizedName("tracking-compass")
                    .build();

    /**
     * Team Beacon item represented as a constant
     * <br>
     * Allows for identification and separation of a beacon and a "team beacon".
     * <p>
     * Used for pre-pvp events when the player wants to place their team's beacon down to
     * allow their team to respawn.
     */
    public static final ItemStack TEAM_BEACON =
            new ItemBuilder(Material.BEACON)
                    .name("&cBeacon")
                    .lore(
                            "&7Your entire life support bro.",
                            "&cKeep it safe."
                    )
                    .localizedName("bbu-beacon")
                    .build();

}
