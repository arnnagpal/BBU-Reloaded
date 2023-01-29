package me.imoltres.bbu.data.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColour
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.game.events.team.BBUTeamModificationEvent
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.item.ItemConstants
import me.imoltres.bbu.utils.world.Position
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Represents a team within the game
 */
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

    /**
     * Add a player to the team
     *
     * @param player player to add
     * @return if it was successful or not
     */
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

    /**
     * Remove a player to the team
     *
     * @param player player to add
     * @return if it was successful or not
     */
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

    /**
     * Broadcasts a message to all team members
     * @param msg message to broadcast
     */
    fun broadcastMessage(msg: String) {
        for (player in players) {
            player.player?.sendMessage(CC.translate(msg))
        }
    }

    /**
     * @return does the team have their beacon
     */
    fun hasBeacon(): Boolean {
        return if (!BBU.getInstance().game.gameState.isPvp()) true else beacon != null
    }

    /**
     * Eliminate this team, keep the players alive though.
     */
    fun eliminate() {
        eliminate(false)
    }

    /**
     * Eliminate this team
     * @param players keep the players alive or not
     */
    fun eliminate(players: Boolean) {
        if (players) {
            for (player in this.players) {
                player.player?.health = 0.0

                removePlayer(player)
            }
        }

        if (beacon != null) {
            beacon!!.toWorldPosition(BBU.getInstance().game.overworld.name).block.type = Material.AIR
            beacon = null
        }
    }

    /**
     * Distribute the beacon, tracking compass, and anything else.
     */
    fun distributeItems() {
        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            distributeBeacon()

            for (player in players) {
                player.giveItemSafely(ItemConstants.TRACKING_COMPASS)
            }
        })
    }

    /**
     * Distribute the beacon
     */
    private fun distributeBeacon() {
        if (players.isEmpty())
            return

        val players = this.players.toList()
        val random = ThreadLocalRandom.current()

        var randInt = random.nextInt(players.size)
        var player = players[randInt]
        while (player.player == null) {
            players.drop(randInt)

            randInt = random.nextInt(players.size)
            player = players[randInt]
        }

        player.giveItemSafely(ItemConstants.TEAM_BEACON)
    }

    /**
     * @return the TextComponent version of the display name
     */
    fun getDisplayName(): TextComponent {
        return CC.translate(getRawDisplayName())
    }

    /**
     * @return a raw bukkit string version of the display name
     */
    fun getRawDisplayName(): String {
        return "&" + colour.chatColor.char + CC.capitalize(getName())
    }

    fun getName(): String {
        return colour.name
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
}