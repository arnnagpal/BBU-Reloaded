package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.GsonFactory
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.entity.Player

@CommandInfo(
    name = "bbu.setlobbyspawn",
    permission = "bbu.command.setlobbyspawn",
    desc = "Set lobby spawn",
    usage = "&c/bbu setlobbyspawn",
    senderType = CommandInfo.SenderType.PLAYER
)
class SetLobbySpawnCommand : SubCommand {
    override fun execute(cmd: CommandArgs) {
        val player = cmd.getSender<Player>()
        val worldPosition = WorldPosition.fromBukkitLocation(player.location)

        BBU.getInstance().mainConfig.configuration.set(
            "lobby-spawn",
            GsonFactory.getCompactGson().toJson(worldPosition)
        )
        player.sendMessage(CC.translate("&aSet lobby spawn to $worldPosition"))

        BBU.getInstance().mainConfig.configuration.save(BBU.getInstance().mainConfig.file)
        player.sendMessage(CC.translate("&aSaved main config."))
    }
}