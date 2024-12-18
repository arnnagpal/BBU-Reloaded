package me.imoltres.bbu.utils.menu.button;

import lombok.AllArgsConstructor;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.callback.TypeCallback;
import me.imoltres.bbu.utils.item.ItemBuilder;
import me.imoltres.bbu.utils.menu.Button;
import me.imoltres.bbu.utils.menu.Menu;
import me.imoltres.bbu.utils.sound.SoundBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class ConfirmationButton extends Button {

    private final boolean confirm;
    private final TypeCallback<Boolean> callback;
    private final boolean closeAfterResponse;

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemBuilder itemBuilder = new ItemBuilder(this.confirm ? Material.GREEN_WOOL : Material.RED_WOOL);

        itemBuilder.name(this.confirm ? CC.GREEN + "Confirm" : CC.RED + "Cancel");

        return itemBuilder.build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        new SoundBuilder(this.confirm ? Sound.BLOCK_NOTE_BLOCK_HARP : Sound.BLOCK_GRAVEL_BREAK, 0.1f, 1f).play(player);

        if (this.closeAfterResponse) {
            Menu menu = Menu.currentlyOpenedMenus.get(player.getName());

            if (menu != null) {
                menu.setClosedByMenu(true);
            }

            player.closeInventory();
        }

        this.callback.callback(this.confirm);
    }

}
