package me.imoltres.bbu.utils.menu.button;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@AllArgsConstructor
@Getter
@Setter
public class DisplayButton extends Button {

    private ItemStack itemStack;
    private boolean cancel;

    @Override
    public ItemStack getButtonItem(Player player) {
        return Objects.requireNonNullElseGet(this.itemStack, () -> new ItemStack(Material.AIR));
    }

    @Override
    public boolean shouldCancel(Player player, ClickType clickType) {
        return this.cancel;
    }

}
