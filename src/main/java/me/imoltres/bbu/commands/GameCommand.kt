package me.imoltres.bbu.commands

import me.imoltres.bbu.commands.main.*
import me.imoltres.bbu.commands.player.PlayerCommands
import me.imoltres.bbu.commands.team.TeamCommands
import me.imoltres.bbu.commands.team.TeamsCommand
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import net.kyori.adventure.text.Component

val GameCommand = command("bbu") {
    description("BBU Main help command, displays all commands with arguments")
    permission("bbu.command.help")

    defaultExecutor { sender ->
        for (component in helpMessage) {
            sender.sendMessage(component)
        }
    }

    subcommand(
        // admin commands
        BuildModeCommand,
        StartCommand,
        StopCommand,
        SetLobbySpawnCommand,
        SetDeathmatchSpawn,
        MoveToCagesCommand,

        // team commands
        TeamsCommand,
        TeamCommands,

        // player commands
        PlayerCommands,

        // debug commands
        DebugCommand
    )
}

private inline val helpMessage: MutableList<Component>
    get() =
        CC.translate(
            listOf(
                "&3&lBBU Help Menu &8- ",
                "&3MAIN: ",
                "  &b* /bbu buildmode &8- &7Enables building/interacting with the game world.",
                "  &b* /bbu start &8- &7What do you think it does?",
                "  &b* /bbu stop &8- &7What do you think it does?",
                "  &b* /bbu setlobbyspawn &8- &7What do you think it does?",

                "  &b* /bbu movetocages &8- &7Moves the players from the lobby phase to the pre-game phase (lobby --> cages)",
                "",
                "&3TEAM: ",
                "  &b* /bbu teams &8- &7Lists all the teams with players in them.",
                "  &b* /bbu teams clear &8- &7Clears all teams' players.",
                "  &b* /bbu team <team> eliminate [players] &8- &7Eliminates the team's beacon, and if specified, eliminates any players inside of that team.",
                "",
                "  &b* /teamposition &8- &7Gives your team's position to you",
                "  &bAliases: [\"teampos\", \"bbuteampos\"]",
                "",
                "&3PLAYER: ",
                "  &b* /bbu player <player> eliminate &8- &7Eliminates the player.",
                "  &b* /bbu player <player> jointeam <team> &8- &7Adds <player> to team <team>.",
                "  &b* /bbu player <player> leaveteam &8- &7Removes <player> from whatever team they were previously in.",
                "",
                "&3DEBUG: ",
                "  &b* /bbu debug givebeacon <player> &8- &7Gives a beacon to another player (Will invalidate any other beacons for that team if placed.)",
                "  &b* /bbu debug givetracker <player> &8- &7Gives a tracker to another player (only use for debug purposes/person somehow loses it.)",
                "  &b* /bbu debug setgamestate <state> &8- &7Sets the game state to <state> (only use for debug purposes.)",
                "",
                "Made by iMoltres &c\u2665"
            )
        )

