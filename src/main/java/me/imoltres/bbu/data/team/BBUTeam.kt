package me.imoltres.bbu.data.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColour
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.events.team.BBUTeamModificationEvent
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.GsonFactory
import me.imoltres.bbu.utils.world.Cuboid
import me.imoltres.bbu.utils.world.Position
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.*

class BBUTeam(val colour: BBUTeamColour) {
    var cage: BBUCage? = null
        set(value) {
            val event = BBUTeamModificationEvent(
                this,
                AbstractMap.SimpleEntry(BBUTeamModificationEvent.ModificationType.ASSIGN_CAGE, field)
            )
            Bukkit.getPluginManager().callEvent(event)

            if (event.isCancelled)
                return

            field = value
        }

    val players: MutableSet<BBUPlayer> = HashSet()

    var beacon: Position? = null
        set(value) {
            val event = BBUTeamModificationEvent(
                this,
                AbstractMap.SimpleEntry(BBUTeamModificationEvent.ModificationType.ASSIGN_BEACON, field)
            )
            Bukkit.getPluginManager().callEvent(event)

            if (event.isCancelled)
                return

            field = value
        }

    private fun setupCage() {
        val cageStr = BBU.instance.teamSpawnsConfig.getString("team." + colour.name)

        cage = if (cageStr != null && cageStr.isNotEmpty()) {
            val cuboid = GsonFactory.getCompactGson().fromJson(cageStr, Cuboid::class.java)

            BBUCage(this, cuboid, cuboid.center.toWorldPosition(Bukkit.getWorlds()[0].name))
        } else null
    }

    fun addPlayer(player: BBUPlayer): Boolean {
        val event = BBUTeamModificationEvent(
            this,
            AbstractMap.SimpleEntry(BBUTeamModificationEvent.ModificationType.ADD_PLAYER, player)
        )
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled)
            return false

        System.out.printf(
            "%s has joined team '%s'\n",
            player.name,
            CC.capitalize(colour.name.lowercase(Locale.getDefault()))
        )
        player.team = this
        return players.add(player)
    }

    fun removePlayer(player: BBUPlayer): Boolean {
        val event = BBUTeamModificationEvent(
            this,
            AbstractMap.SimpleEntry(BBUTeamModificationEvent.ModificationType.REMOVE_PLAYER, player)
        )
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled)
            return false

        System.out.printf(
            "%s has left team '%s'\n",
            player.name,
            CC.capitalize(colour.name.lowercase(Locale.getDefault()))
        )
        player.team = null
        return players.remove(player)
    }

    fun hasBeacon(): Boolean {
        return if (BBU.instance.game.gameState === GameState.PRE_GAME) true else beacon != null
    }

    fun eliminate() {
        eliminate(false)

    }

    fun eliminate(players: Boolean) {
        if (players) {
            for (player in this.players) {
                player.player?.health = 0.0

                removePlayer(player)
            }
        }

        if (beacon != null) {
            beacon!!.toWorldPosition(BBU.instance.game.overworld.name).block.type = Material.AIR
        }

        beacon = null
    }

    fun getDisplayName(): TextComponent {
        return CC.translate(getRawDisplayName())
    }

    fun getRawDisplayName(): String {
        return "&" + colour.chatColor.char + CC.capitalize(colour.name)
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other is BBUTeam && other.colour == colour)
                return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = colour.hashCode()
        result = 31 * result + (cage?.hashCode() ?: 0)
        result = 31 * result + players.hashCode()
        result = 31 * result + (beacon?.hashCode() ?: 0)
        return result
    }

    init {
        setupCage()
    }
}