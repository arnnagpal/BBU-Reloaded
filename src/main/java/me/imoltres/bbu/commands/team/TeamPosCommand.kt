package me.imoltres.bbu.commands.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.general.PlayerUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player


val TeamPosCommand = command(
    "teamposition",
    "teampos", "bbuteampos"
) {
    onlyPlayers()

    defaultExecutor { player ->
        player as Player

        val team = BBU.getInstance().teamController.getTeam(player)

        if (team == null) {
            player.sendMessage(CC.translate("&cYou're not in a team."))
            return@defaultExecutor
        }

        for (bbuPlayer in team.players) {
            val location = (bbuPlayer.player?.location?.let { PlayerUtils.getLocation(it) })!!
            val component = Component.text()
                .append(
                    Component.text("* ")
                        .color(NamedTextColor.AQUA)
                )
                .append(bbuPlayer.getDisplayName())
                .append(
                    Component.text(": ")
                        .color(NamedTextColor.AQUA)
                )
                .append(
                    Component.text(location)
                        .color(NamedTextColor.AQUA)
                        .clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                ClickEvent.Payload.string("/trackposition $location true")
                            )
                        )
                        .hoverEvent(
                            HoverEvent.hoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                CC.translate("&7Click me to track!")
                            )
                        )
                )
                .append(Component.text("\n"))
                .build()

            player.sendMessage(component)
        }
    }
}