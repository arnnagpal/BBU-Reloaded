package me.imoltres.bbu.commands

import me.imoltres.bbu.BBU
import me.imoltres.bbu.commands.main.*
import me.imoltres.bbu.commands.player.PlayerCommands
import me.imoltres.bbu.commands.team.TeamCommands
import me.imoltres.bbu.commands.team.TeamsClearCommand
import me.imoltres.bbu.commands.team.TeamsCommand
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.*
import java.util.stream.Collectors

@CommandInfo(
    name = "bbu",
    permission = "bbu.command.help",
    desc = "BBU Main help command, displays all commands with arguments",
    usage = "&cUsage: /bbu"
)
class GameCommand : Command {

    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<CommandSender>()

        for (component in getHelpMessage()) {
            sender.sendMessage(component)
        }
    }

    override fun subCommands(): List<SubCommand> {
        return listOf(
            BuildModeCommand(),
            StartCommand(),
            StopCommand(),
            SetLobbySpawnCommand(),
            MoveToCagesCommand(),

            TeamsCommand(),
            TeamsClearCommand(),
            TeamCommands(),

            PlayerCommands(),

            DebugCommand()
        )
    }

    @Completer
    override fun tabCompleter(cmd: CommandArgs): List<String> {
        val args = cmd.arguments
        val completions = ArrayList<String>()
        val options = ArrayList<String>()

        when (args.size) {
            1 -> options.addAll(
                Arrays.asList(
                    "buildmode",
                    "start",
                    "stop",
                    "setlobbyspawn",
                    "movetocages",
                    "teams",
                    "team",
                    "player",
                    "debug"
                )
            )

            2 -> {
                val prev = args[0]
                when (prev.lowercase()) {
                    "teams" -> options.add("clear")

                    "team" -> {
                        for (team in BBU.getInstance().teamController.allTeams) {
                            options.add(team.colour.name)
                        }
                    }

                    "player" -> {
                        options.addAll(
                            Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet())
                        )
                    }

                    "debug" -> options.addAll(
                        listOf(
                            "setgamestate",
                            "givebeacon",
                            "givetracker"
                        )
                    )
                }
            }

            3 -> {
                val parentCommand = args[0]
                when (parentCommand.lowercase()) {
                    "team" -> {
                        options.addAll(
                            listOf(
                                "eliminate"
                            )
                        )
                    }

                    "player" -> options.addAll(
                        listOf(
                            "eliminate",
                            "jointeam",
                            "leaveteam"
                        )
                    )

                    "debug" -> {
                        val option = args[1]
                        when (option.lowercase()) {
                            "setgamestate" -> {
                                options.addAll(
                                    GameState.entries.map { it.name.uppercase() }
                                )
                            }
                            else -> {
                                options.addAll(
                                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet())
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                val parentCommand = args[2]
                when (parentCommand.lowercase()) {
                    "eliminate" -> {
                        options.add("true")
                        options.add("false")
                    }
                    "jointeam" -> {
                        for (team in BBU.getInstance().teamController.allTeams) {
                            options.add(team.colour.name)
                        }
                    }
                }
            }
        }

        StringUtil.copyPartialMatches(args[args.size - 1], options, completions)

        return completions
    }

    private fun getHelpMessage(): MutableList<Component> {
        return CC.translate(
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
                "Made by iUwutres &c\u2665"
            )
        )
    }

}
