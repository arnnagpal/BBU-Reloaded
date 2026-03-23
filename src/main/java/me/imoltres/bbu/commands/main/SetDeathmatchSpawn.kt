package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.json.GsonFactory
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.entity.Player

val SetDeathmatchSpawn = command(
    "setdeathmatchspawn",
    "setdmspawn",
) {
    permission("bbu.command.setdeathmatchspawn")
    onlyPlayers()

    subcommand("spectator") {
        defaultExecutor { player ->
            player as Player
            val worldPosition = WorldPosition.fromBukkitLocation(player.location)

            BBU.getInstance().mainConfig.configuration.set(
                "deathmatch.spawn",
                GsonFactory.getCompactGson().toJson(worldPosition)
            )
            player.sendMessage(CC.translate("&aSet deathmatch spectator spawn to $worldPosition"))

            BBU.getInstance().mainConfig.configuration.save(BBU.getInstance().mainConfig.file)
            MainConfig.reload()

            player.sendMessage(CC.translate("&aSaved main config."))
        }
    }

    subcommand("insert") {
        defaultExecutor { player ->
            player as Player
            val worldPosition = WorldPosition.fromBukkitLocation(player.location)

            val locations = MainConfig.deathmatchTeamLocations.toMutableList()
            locations.add(GsonFactory.getCompactGson().toJson(worldPosition))

            BBU.getInstance().mainConfig.configuration.set(
                "deathmatch.team-locations",
                locations
            )
            player.sendMessage(CC.translate("&aAdd deathmatch team spawn: $worldPosition"))

            BBU.getInstance().mainConfig.configuration.save(BBU.getInstance().mainConfig.file)
            MainConfig.reload()

            player.sendMessage(CC.translate("&aSaved main config."))
        }
    }

    subcommand("pop") {
        defaultExecutor { player ->
            player as Player
            val locations = MainConfig.deathmatchTeamLocations.toMutableList()
            val removed = locations.removeLastOrNull()

            BBU.getInstance().mainConfig.configuration.set(
                "deathmatch.team-locations",
                locations
            )
            if (removed == null) {
                player.sendMessage(CC.translate("&cNo deathmatch team spawns to remove."))
            } else player.sendMessage(CC.translate("&aRemoved last deathmatch team spawn: $removed"))

            BBU.getInstance().mainConfig.configuration.save(BBU.getInstance().mainConfig.file)
            MainConfig.reload()

            player.sendMessage(CC.translate("&aSaved main config."))
        }
    }
}