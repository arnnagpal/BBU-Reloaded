package me.imoltres.bbu.utils.menu;

import me.imoltres.bbu.utils.item.ItemBuilder;
import me.imoltres.bbu.utils.sound.SoundBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Button {

    public static Button placeholder(Material material, String... title) {
        return (new Button() {
            public ItemStack getButtonItem(Player player) {
                ItemBuilder builder = new ItemBuilder(material);
                builder.name(StringUtils.join(title));

                return builder.build();
            }
        });
    }

    public static void playFail(Player player) {
        new SoundBuilder(Sound.BLOCK_GRASS_BREAK, 0.1f, 1f).play(player);
    }

    public static void playSuccess(Player player) {
        new SoundBuilder(Sound.BLOCK_NOTE_BLOCK_HARP, 0.1f, 1f).play(player);
    }

    public static void playNeutral(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1F, 1F);
    }

    public abstract ItemStack getButtonItem(Player player);

    public void clicked(Player player, ClickType clickType) {
    }

    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
    }

    public boolean shouldCancel(Player player, ClickType clickType) {
        return true;
    }

    public boolean shouldUpdate(Player player, ClickType clickType) {
        return false;
    }

}