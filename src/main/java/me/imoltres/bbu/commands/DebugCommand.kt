package me.imoltres.bbu.commands

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.GameStateArgumentType
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.argument
import me.imoltres.bbu.utils.command.argumentPlayer
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.item.ItemConstants
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val DebugCommand = command("debug") {
    permission("bbu.command.debug")
    val playerArg = argumentPlayer("player")

    subcommand("givebeacon") {
        buildSyntax(playerArg) {
            executor { sender, ctx ->
                val plr = playerArg().resolve(ctx.source).first()
                giveItem(
                    sender,
                    plr,
                    ItemConstants.TEAM_BEACON,
                    "team beacon"
                )
            }
        }
    }

    subcommand("givetracker") {
        buildSyntax(playerArg) {
            executor { sender, ctx ->
                val plr = playerArg().resolve(ctx.source).first()
                giveItem(
                    sender,
                    plr,
                    ItemConstants.TRACKING_COMPASS,
                    "tracking compass"
                )
            }
        }
    }

    subcommand("setgamestate") {
        val stateArg = argument("gameState", GameStateArgumentType())

        buildSyntax(stateArg) {
            executor { sender ->
                val state = stateArg()
                updateState(sender, state)
            }
        }
    }
}

private fun updateState(sender: CommandSender, state: GameState) {
    BBU.getInstance().game.thread.tick = state.startTime * 20
    sender.sendMessage(CC.translate("&aUpdated game state to " + state.name.uppercase()))
}

private fun giveItem(sender: CommandSender, player: Player?, item: ItemStack, display: String) {
    if (player != null) {
        if (player.inventory.firstEmpty() == -1) {
            player.showTitle(
                Title.title(
                    CC.translate("&cYou might have to empty your inventory (FULL_INV)"),
                    CC.translate("&7An admin attempted to give you a $display.")
                )
            )
            sender.sendMessage(CC.translate("&cError while giving ${player.name} a $display. (FULL_INV)"))
        } else {
            player.inventory.addItem(item)
            sender.sendMessage(CC.translate("&aGiven ${player.name} a $display. successfully."))
        }
    }
}
