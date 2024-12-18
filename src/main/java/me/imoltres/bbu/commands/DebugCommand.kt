package me.imoltres.bbu.commands

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import me.imoltres.bbu.utils.item.ItemConstants
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandInfo(
    name = "bbu.debug",
    permission = "bbu.command.debug",
    desc = "Debug commands related to BBU",
    usage = "&c/bbu debug [option]"
)
class DebugCommand : SubCommand {
    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<Player>()
        val args = cmd.arguments

        if (args.size == 2) {
            when (args[0].lowercase()) {
                "givebeacon" -> giveItem(
                    sender,
                    Bukkit.getPlayer(args[1]),
                    ItemConstants.TEAM_BEACON,
                    "team beacon"
                )

                "givetracker" -> giveItem(
                    sender,
                    Bukkit.getPlayer(args[1]),
                    ItemConstants.TRACKING_COMPASS,
                    "tracking compass"
                )

                "setgamestate" -> updateState(
                    sender,
                    args[1]
                )
            }
        }
    }

    private fun updateState(sender: Player, input: String) {
        try {
            val state = GameState.valueOf(input.uppercase())
            BBU.getInstance().game.thread.tick = state.startTime * 20
            sender.sendMessage(CC.translate("&aUpdated game state to " + input.uppercase()))
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage(CC.translate("&cError while trying to update game state to " + input.uppercase() + ". Check console."))
        }
    }

    private fun giveItem(sender: Player, player: Player?, item: ItemStack, display: String) {
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
}
