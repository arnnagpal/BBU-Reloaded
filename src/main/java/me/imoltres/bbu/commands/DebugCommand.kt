package me.imoltres.bbu.commands

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.GameStateArgumentType
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.argument
import me.imoltres.bbu.utils.command.argumentPlayer
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.item.ItemBuilder
import me.imoltres.bbu.utils.item.ItemConstants
import me.imoltres.bbu.utils.world.WorldPosition
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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

    subcommand("nextshrinkphase") {
        defaultExecutor { sender ->
            if (BBU.getInstance().game.gameState != GameState.PVP_BORDER_SHRINK) {
                sender.sendMessage(CC.translate("&cYou can only advance the shrink phase in the PVP_BORDER_SHRINK game state."))
                return@defaultExecutor
            }

            val nextPhase = BBU.getInstance().game.nextShrinkPhase
            if (nextPhase != null) {
                BBU.getInstance().game.thread.timeToNextShrink = 0
                sender.sendMessage(CC.translate("&aAdvanced to the next shrink phase with size ${nextPhase.size}, time ${nextPhase.time}, and length ${nextPhase.length}."))
            } else {
                sender.sendMessage(CC.translate("&cNo next shrink phase available."))
            }
        }
    }

    subcommand("pastecage") {
        defaultExecutor { sender ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage(CC.translate("&cOnly players can use this command."))
                return@defaultExecutor
            }

            BBU.getInstance().cageController.pasteSchematic(WorldPosition.fromBukkitLocation(player.location))
            sender.sendMessage(CC.translate("&aPasted cage at your location."))
        }
    }

    subcommand("reset") {
        defaultExecutor { sender ->
            BBU.getInstance().game.reset()
            sender.sendMessage(CC.translate("&aReset the game successfully."))
        }
    }
}

private fun updateState(sender: CommandSender, state: GameState) {
    if (state != GameState.DEATHMATCH) {
        BBU.getInstance().game.thread.tick = state.startTime * 20
    } else {
        BBU.getInstance().game.thread.enterDeathmatch()
    }
    sender.sendMessage(CC.translate("&aUpdated game state to " + state.name.uppercase()))
}

private fun giveItem(sender: CommandSender, player: Player?, item: ItemBuilder, display: String) {
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
            player.inventory.addItem(item.build())
            sender.sendMessage(CC.translate("&aGiven ${player.name} a $display. successfully."))
        }
    }
}
