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
        var spectateAfterDeath = false
        var friendlyFire = false
        var lobbySpawn = ""

        init { load() }

        private fun <T> cfg(path: String): T = MainConfig<T>(path).get()

        private fun load() {
            borderSize = cfg("border.size")
            borderPhases = cfg<List<LinkedHashMap<String, Int>>>("border.shrink-phases")
                .mapTo(mutableListOf()) { ShrinkPhase(it["size"]!!, it["time"]!!, it["length"]!!) }
            deathmatchEnabled = cfg("deathmatch.enabled")
            deathmatchTime    = cfg("deathmatch.time")
            deathmatchSpawn   = cfg("deathmatch.spawn")
            spectateAfterDeath = cfg("spectate-after-death")
            friendlyFire      = cfg("friendly-fire")
            lobbySpawn        = cfg("lobby-spawn")
        }

        fun reload() {
            BBU.getInstance().mainConfig.reloadConfig()
            load()
        }
    }
}