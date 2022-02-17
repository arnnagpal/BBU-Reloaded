package me.imoltres.bbu.utils.general;

import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.item.ItemConstants;
import me.imoltres.bbu.utils.world.WorldPosition;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.Objects;

/**
 * Used to primarily broadcast titles to all players
 */
public class PlayerUtils {

    /**
     * Broadcast a {@link net.kyori.adventure.title.Title} to all players
     *
     * @param title    big title text
     * @param subTitle small title text
     */
    public static void broadcastTitle(String title, String subTitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(CC.translate(title), CC.translate(subTitle)));
        }
    }

    public static boolean trackPosition(Player player, WorldPosition position) {
        return trackPosition(player, position, true);
    }


    public static boolean trackPosition(Player player, WorldPosition position, boolean message) {
        ItemStack compass = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (ItemConstants.isSimilar(ItemConstants.TRACKING_COMPASS, item)) {
                    compass = item;
                    break;
                }
            }
        }

        if (compass != null) {
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            meta.setLodestoneTracked(false);
            meta.setLodestone(position.toBukkitLocation());
            compass.setItemMeta(meta);

            if (message)
                player.sendMessage(CC.translate("&aTracking position on compass! (World=" + Objects.requireNonNull(Bukkit.getWorld(position.getWorld())).getEnvironment().name() + ")"));

            return true;
        }

        return false;
    }

    public static String getLocation(Location pos) {
        return String.format("%.1f %.1f %.1f %s", pos.getX(), pos.getY(), pos.getZ(), pos.getWorld().getName());
    }

}
