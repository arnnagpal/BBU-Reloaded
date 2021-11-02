package me.imoltres.bbu.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to quickly make and modify an item in a simple and clean builder solution.
 */
public class ItemBuilder {

    //ItemStack to hold the item being created.
    private final ItemStack is;

    /**
     * Constructs an instance of ItemBuilder with a material input
     *
     * @param mat Material of the item being "built"
     */
    public ItemBuilder(Material mat) {
        is = new ItemStack(mat);
    }

    /**
     * Constructs an instance of ItemBuilder with an itemstack input
     * (Keeps any item metadata added on in the item stack previously)
     *
     * @param is ItemStack to modify.
     */
    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    /**
     * Set the amount of items that are in this specific itemstack.
     *
     * @param amount Amount of items
     * @return The current ItemBuilder instance
     */
    public ItemBuilder amount(int amount) {
        is.setAmount(amount);
        return this;
    }

    /**
     * Set the name of the itemstack, colour codes are allowed.
     *
     * @param name Name of the item (colour codes supported)
     * @return The current ItemBuilder instance
     */
    public ItemBuilder name(String name) {
        ItemMeta meta = is.getItemMeta();
        meta.displayName(CC.translate(name));
        is.setItemMeta(meta);
        return this;
    }

    /**
     * Appends to the lore of the itemstack
     *
     * @param lore Lore string to append
     * @return The current ItemBuilder instance
     */
    public ItemBuilder lore(String lore) {
        ItemMeta meta = is.getItemMeta();
        List<Component> cLore = meta.lore();

        if (cLore == null) {
            cLore = new ArrayList<>();
        }

        cLore.add(CC.translate(lore));
        meta.lore(cLore);

        is.setItemMeta(meta);

        return this;
    }

    /**
     * Set/Replace the lore of the itemstack using an array
     *
     * @param lore New lore represented as an array
     * @return The current ItemBuilder instance
     */
    public ItemBuilder lore(String... lore) {
        ItemMeta meta = is.getItemMeta();

        meta.lore(CC.translate(lore));
        is.setItemMeta(meta);

        return this;
    }

    /**
     * Sets/replaces the current lore of the itemstack using an array list
     *
     * @param lore New lore represented as an array list
     * @return The current ItemBuilder instance
     */
    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = is.getItemMeta();

        meta.lore(CC.translate(lore));
        is.setItemMeta(meta);

        return this;
    }

    /**
     * Set the durability of the itemstack
     *
     * @param damage Damage to set the durability to
     * @return The current ItemBuilder instance
     */
    public ItemBuilder durability(int damage) {
        Damageable meta = (Damageable) is.getItemMeta();
        meta.setDamage(damage);
        is.setItemMeta(meta);
        return this;
    }

    /**
     * Appends an enchantment to the itemstack (unsafely, meaning it'll add it even it's not supposed to be otherwise possible)
     *
     * @param enchantment Enchantment to add onto the itemstack
     * @param level       The level of the enchantment
     * @return The current ItemBuilder instance
     */
    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        is.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Appends an enchantment to the itemstack (unsafely, meaning it'll add it even it's not supposed to be otherwise possible) at the default level of 1
     *
     * @param enchantment Enchantment to add onto the itemstack
     * @return The current ItemBuilder instance
     */
    public ItemBuilder enchantment(Enchantment enchantment) {
        is.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    /**
     * Sets the material type of the itemstack
     *
     * @param material New material type of the itemstack
     * @return The current ItemBuilder instance
     */
    public ItemBuilder type(Material material) {
        is.setType(material);
        return this;
    }

    /**
     * Hide/Unhide the attributes of the itemstack
     *
     * @param hide Hide or unhide (true or false)
     * @return The current ItemBuilder instance
     */
    public ItemBuilder hideAttributes(boolean hide) {
        ItemMeta meta = is.getItemMeta();
        if (hide) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        is.setItemMeta(meta);
        return this;
    }

    /**
     * Sets the itemstack to be unbreakable (or not), infinite durability in a sense.
     *
     * @param unbreakable Unbreakable (or not), true or false.
     * @return The current ItemBuilder instance
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        ItemMeta toSet = is.getItemMeta();
        toSet.setUnbreakable(unbreakable);
        is.setItemMeta(toSet);
        return this;
    }

    /**
     * Clear all lore on the itemstack
     *
     * @return The current ItemBuilder instance
     */
    public ItemBuilder clearLore() {
        ItemMeta meta = is.getItemMeta();

        meta.lore(new ArrayList<>());
        is.setItemMeta(meta);

        return this;
    }

    /**
     * Clear all enchantments on the itemstack
     *
     * @return The current ItemBuilder instance
     */
    public ItemBuilder clearEnchantments() {
        for (Enchantment e : is.getEnchantments().keySet()) {
            is.removeEnchantment(e);
        }

        return this;
    }

    /**
     * Add a potion effect to the itemstack
     * Only applies to material type Material.POTION, or any other material types that extend it
     *
     * @param effect The potion effect to add onto the potion
     * @return The current ItemBuilder instance
     */
    public ItemBuilder potionEffect(PotionType effect) {
        PotionMeta meta = (PotionMeta) is.getItemMeta();
        meta.setBasePotionData(new PotionData(effect));
        is.setItemMeta(meta);

        return this;
    }

    /**
     * Sets the localized name (hidden name in the NBT) to the itemstack
     *
     * @param name The new localized name
     * @return The current ItemBuilder instance
     */
    public ItemBuilder localizedName(String name) {
        ItemMeta meta = is.getItemMeta();
        meta.setLocalizedName(name);
        is.setItemMeta(meta);

        return this;
    }

    /**
     * Returns the current itemstack with all the changes made.
     *
     * @return The final itemstack after all the changes
     */
    public ItemStack build() {
        return is;
    }

}