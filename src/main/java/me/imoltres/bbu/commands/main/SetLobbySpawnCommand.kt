package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.json.GsonFactory
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.entity.Player

val SetLobbySpawnCommand = command(
    "setlobbyspawn",
    "setspawn",
) {
    permission("bbu.command.setlobbyspawn")
    onlyPlayers()

    defaultExecutor { player ->
        player as Player
        val worldPosition = WorldPosition.fromBukkitLocation(player.location)

        BBU.getInstance().mainConfig.configuration.set(
            "lobby.spawn",
            GsonFactory.getCompactGson().toJson(worldPosition)
        )
        player.sendMessage(CC.translate("&aSet lobby spawn to $worldPosition"))

        BBU.getInstance().mainConfig.configuration.save(BBU.getInstance().mainConfig.file)
        MainConfig.reload()

        player.sendMessage(CC.translate("&aSaved main config."))
    }
}