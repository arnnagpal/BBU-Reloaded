package me.imoltres.bbu.utils.nametag;

import me.imoltres.bbu.utils.CC;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class NametagThread extends Thread {

    private final NametagHandler handler;

    public NametagThread(NametagHandler handler) {
        this.handler = handler;
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                try {
                    tick();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                sleep(50 * handler.getTicks());
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void tick() {
        if (this.handler.getAdapter() == null) {
            return;
        }

        Bukkit.getScheduler().runTask(this.handler.getPlugin(), () -> {
            List<BufferedNametag> nametags = this.handler.getAdapter().getPlate();

            for (Player player : this.handler.getPlugin().getServer().getOnlinePlayers()) {
                NametagBoard board = this.handler.getBoards().get(player.getUniqueId());
                // This shouldn't happen, but just in case
                if (board == null) {
                    System.out.println("board is null for " + player.getName());
                    continue;
                }

                Scoreboard scoreboard = board.getScoreboard();


                if (nametags == null) {
                    continue;
                }

                if (this.handler.getAdapter().showHealthBelowName()) {
                    if (scoreboard.getObjective(DisplaySlot.BELOW_NAME) == null) {
                        //stringescapeutils was moved to commons-text, fix later
                        Objective objective = scoreboard.registerNewObjective("showhealth", Criteria.HEALTH, CC.translate("&c" + StringEscapeUtils.unescapeJava("\u2764")));
                        Objective listObjective = scoreboard.registerNewObjective("listhealth", Criteria.HEALTH, CC.translate("Health"));

                        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
                        listObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                        listObjective.setRenderType(RenderType.HEARTS);
                    }
                } else {
                    if (scoreboard.getObjective(DisplaySlot.BELOW_NAME) != null) {
                        Objective objective = scoreboard.getObjective(DisplaySlot.BELOW_NAME);
                        Objective listObjective = scoreboard.getObjective(DisplaySlot.PLAYER_LIST);
                        assert objective != null;
                        assert listObjective != null;

                        objective.unregister();
                        listObjective.unregister();
                    }
                }

                Set<String> toReturn = new HashSet<>();
                Map<String, List<String>> strings = new HashMap<>();

                for (BufferedNametag bufferedNametag : nametags) {
                    //Get Team
                    Team team = scoreboard.getTeam(bufferedNametag.getGroupName());

                    if (team == null) {
                        team = scoreboard.registerNewTeam(bufferedNametag.getGroupName());
                        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                    }

                    toReturn.add(team.getName());
                    board.getBufferedTeams().remove(team.getName());

                    //Set Prefix
                    if (bufferedNametag.getPrefix() != null) {
                        if (!bufferedNametag.getPrefix().content().equals(((TextComponent) team.prefix()).content())) {
                            team.prefix(bufferedNametag.getPrefix());
                        }
                    } else {
                        team.prefix(CC.translate("&f"));
                    }
                    //Set Suffix
                    if (bufferedNametag.getSuffix() != null) {
                        if (!bufferedNametag.getSuffix().content().equals(((TextComponent) team.suffix()).content())) {
                            team.suffix(bufferedNametag.getSuffix());
                        }
                    } else {
                        team.suffix(bufferedNametag.getPrefix());
                    }

                    if (bufferedNametag.getPlayer() != null && bufferedNametag.getPlayer().isOnline()) {
                        if (!team.hasEntry(bufferedNametag.getPlayer().getName())) {
                            team.addEntry(bufferedNametag.getPlayer().getName());
                        }
                        if (strings.containsKey(team.getName())) {
                            List<String> lol = strings.get(team.getName());
                            lol.add(bufferedNametag.getPlayer().getName());
                            strings.put(team.getName(), lol);
                        } else {
                            List<String> lol = new ArrayList<>();
                            lol.add(bufferedNametag.getPlayer().getName());
                            strings.put(team.getName(), lol);
                        }
                    }

                    //Friendly Invis
                    team.setCanSeeFriendlyInvisibles(bufferedNametag.isFriendlyInvis());
                }

                for (String newGroupName : board.getBufferedTeams()) {
                    Team team = scoreboard.getTeam(newGroupName);

                    if (team == null) {
                        continue;
                    }

                    team.unregister();
                }

                board.getBufferedTeams().clear();
                board.getBufferedTeams().addAll(toReturn);

                for (String teamName : board.getBufferedTeams()) {
                    List<String> members = strings.get(teamName);
                    Team team = scoreboard.getTeam(teamName);

                    if (team == null) {
                        continue;
                    }

                    for (String entry : team.getEntries()) {
                        if (members.contains(entry)) {
                            continue;
                        }
                        team.removeEntry(entry);
                    }
                }
            }
        });
    }
}
