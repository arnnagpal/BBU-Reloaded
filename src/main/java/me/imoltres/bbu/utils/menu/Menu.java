package me.imoltres.bbu.utils.menu;

import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class Menu {

    public static Map<String, Menu> currentlyOpenedMenus = new HashMap<>();

    @Getter
    protected BBU core = BBU.getInstance();
    private Map<Integer, Button> buttons = new HashMap<>();
    private boolean autoUpdate = false;
    private boolean updateAfterClick = true;
    private boolean closedByMenu = false;
    private boolean placeholder = false;
    private Button placeholderButton = Button.placeholder(Material.BLACK_STAINED_GLASS_PANE, " ");

    private ItemStack createItemStack(Player player, Button button) {
        return button.getButtonItem(player);
    }

    public void openMenu(Player player) {
        this.buttons = this.getButtons(player);

        Menu previousMenu = Menu.currentlyOpenedMenus.get(player.getName());
        Inventory inventory = null;
        int size = this.getSize() == -1 ? this.size(this.buttons) : this.getSize();
        boolean update = false;
        String title = (this.getTitle(player));

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        if (previousMenu == null) {
            player.closeInventory();
        } else {
            int previousSize = player.getOpenInventory().getTopInventory().getSize();

            if (previousSize == size && player.getOpenInventory().title().equals(CC.translate(title))) {
                inventory = player.getOpenInventory().getTopInventory();
                update = true;
            } else {
                previousMenu.setClosedByMenu(true);
                player.closeInventory();
            }
        }

        if (inventory == null) {
            inventory = Bukkit.createInventory(player, size, CC.translate(title));
        }

        inventory.setContents(new ItemStack[inventory.getSize()]);

        currentlyOpenedMenus.put(player.getName(), this);

        for (Map.Entry<Integer, Button> buttonEntry : this.buttons.entrySet()) {
            inventory.setItem(buttonEntry.getKey(), createItemStack(player, buttonEntry.getValue()));
        }

        if (this.isPlaceholder()) {
            for (int index = 0; index < size; index++) {
                if (this.buttons.get(index) == null) {
                    this.buttons.put(index, this.placeholderButton);
                    inventory.setItem(index, this.placeholderButton.getButtonItem(player));
                }
            }
        }

        if (update) {
            player.updateInventory();
        } else {
            player.openInventory(inventory);
        }

        this.onOpen(player);
        this.setClosedByMenu(false);
    }

    public int size(Map<Integer, Button> buttons) {
        int highest = 0;

        for (int buttonValue : buttons.keySet()) {
            if (buttonValue > highest) {
                highest = buttonValue;
            }
        }

        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    public int getSize() {
        return -1;
    }

    public int getSlot(int x, int y) {
        return ((9 * y) + x);
    }

    public abstract String getTitle(Player player);

    public abstract Map<Integer, Button> getButtons(Player player);

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
    }

}
