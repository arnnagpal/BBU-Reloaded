package me.imoltres.bbu.utils.config

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.ShrinkPhase

class MainConfig<T>(path: String) : ConfigGetter<T>(BBU.getInstance().mainConfig, path) {

    companion object {
        var borderSize = 0
        var borderPhases = listOf<ShrinkPhase>()

        var deathmatchEnabled = false
        var deathmatchTime = 0
        var deathmatchSpawn = ""
        var deathmatchTeamLocations = listOf<String>()

        var spectateAfterDeath = false
        var friendlyFire = false

        var lobbySpawn = ""
        var lobbyYMin = 0

        var beaconYMin = 0
        var beaconYMax = 0

        init {
            load()
        }

        private fun <T> cfg(path: String): T = MainConfig<T>(path).get()

        private fun load() {
            borderSize = cfg("border.size")
            borderPhases = cfg<List<LinkedHashMap<String, Int>>>("border.shrink-phases")
                .mapTo(mutableListOf()) { ShrinkPhase(it["size"]!!, it["time"]!!, it["length"]!!) }
            deathmatchEnabled = cfg("deathmatch.enabled")
            deathmatchTime = cfg("deathmatch.time")
            deathmatchSpawn = cfg("deathmatch.spawn")
            deathmatchTeamLocations = cfg("deathmatch.team-locations")
            spectateAfterDeath = cfg("spectate-after-death")
            friendlyFire = cfg("friendly-fire")
            lobbySpawn = cfg("lobby.spawn")
            lobbyYMin = cfg("lobby.y-min")
            beaconYMin = cfg("beacon.y-min")
            beaconYMax = cfg("beacon.y-max")
        }

        fun reload() {
            BBU.getInstance().mainConfig.reloadConfig()
            load()
        }
    }
}