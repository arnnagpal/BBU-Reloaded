package me.imoltres.bbu.data.classes

import me.imoltres.bbu.data.ability.Ability
import org.bukkit.inventory.ItemStack

interface PlayerClass {
    val description: String
    val startingEquipment: List<ItemStack>
    val abilities: List<Ability>

    fun useAbility(ability: Ability)

}