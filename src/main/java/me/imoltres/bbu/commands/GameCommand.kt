package me.imoltres.bbu.commands

import me.imoltres.bbu.BBU
import me.imoltres.bbu.commands.main.BuildModeCommand
import me.imoltres.bbu.commands.main.StartCommand
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
                        for (team in BBU.instance.teamController.allTeams) {
                            options.add(team.colour.name)
                        }
                    }

                    "player" -> {
                        options.addAll(
                            Arrays.asList(
                                "revive",
                                "eliminate"
                            )
                        )
                        options.addAll(
                            Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet())
                        )
                    }

                    "debug" -> options.addAll(
                        Arrays.asList(
                            "invsee",
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
                            Arrays.asList(
                                "revive",
                                "eliminate"
                            )
                        )
                    }

                    "player" -> options.addAll(
                        Arrays.asList(
                            "jointeam",
                            "leaveteam"
                        )
                    )

                    "debug" -> options.addAll(
                        Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet())
                    )
                }
            }

            4 -> {
                val parentCommand = args[0]
                when (parentCommand.lowercase()) {
                    "eliminate", "invsee" -> {
                        options.add("true")
                        options.add("false")
                    }
                }
            }
        }

        StringUtil.copyPartialMatches(args[args.size - 1], options, completions)

        return completions
    }

    private fun getHelpMessage(): MutableList<Component> {
        return CC.translate(
            Arrays.asList(
                "&3&lBBU Help Menu &8- ",
                "&3MAIN: ",
                "  &b* /bbu buildmode &8- &7Enables building/interacting with the game world.",
                "  &b* /bbu start &8- &7What do you think it does?",
                "  &b* /bbu pause &8- &7What do you think it does?",
                "  &b* /bbu stop &8- &7What do you think it does?",
                "",
                "&3TEAM: ",
                "  &b* /bbu teams &8- &7Lists all the teams with players in them.",
                "  &b* /bbu teams clear &8- &7Clears all teams' players.",
                "  &b* /bbu team <team> revive &8- &7Respawns the team's beacon, and reviving any dead teammates.",
                "  &b* /bbu team <team> eliminate [players] &8- &7Eliminates the team's beacon, and if specified, eliminates any players inside of that team.",
                "",
                "&3PLAYER: ",
                "  &b* /bbu player revive &8- &7Allows the player to come back into the game *if* they got eliminated.",
                "  &b* /bbu player eliminate &8- &7Eliminates the player.",
                "  &b* /bbu player <player> jointeam <team> &8- &7Adds <player> to team <team>.",
                "  &b* /bbu player <player> leaveteam &8- &7Removes <player> from whatever team they were previously in.",
                "",
                "&3DEBUG: ",
                "  &b* /bbu debug givebeacon <player> &8- &7Gives a beacon to another player (Will invalidate any other beacons for that team if placed.)",
                "  &b* /bbu debug givetracker <player> &8- &7Gives a tracker to another player (only use for debug purposes/person somehow loses it.)",
                "",
                "Made by iUwutres &c\u2665"
            )
        )
    }

}
