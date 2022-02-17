package me.imoltres.bbu.utils.menu.button;

import me.imoltres.bbu.utils.item.ItemBuilder;
import me.imoltres.bbu.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Filler extends Button {

    private final Material filler;

    public Filler(Material filler) {
        this.filler = filler;
    }

    public Filler(boolean dark) {
        this(dark ? Material.BLACK_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE);
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(filler).name("").build();
    }
}
