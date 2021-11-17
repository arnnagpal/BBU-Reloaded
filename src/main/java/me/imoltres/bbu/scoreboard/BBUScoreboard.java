package me.imoltres.bbu.scoreboard;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardSetupException;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public abstract class BBUScoreboard {

    @Getter
    private final transient BBUPlayer parentPlayer;
    @Getter
    private final transient Player parentBukkitPlayer;

    @Getter
    private final Scoreboard scoreboard;
    @Getter
    private final ScoreboardType scoreboardType;
    private final ChatColor[] chatColourCache = ChatColor.values();
    protected BasicConfigurationFile messages = BBU.getInstance().getMessagesConfig();
    @Getter
    private String title;
    private Objective objective;

    protected BBUScoreboard(int startNumber, Player player, ScoreboardType scoreboardType) {
        this.title = messages.getString("scoreboard-title");
        this.scoreboard = player.getScoreboard();
        this.scoreboardType = scoreboardType;
        this.parentPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
        this.parentBukkitPlayer = player;

        parentPlayer.setScoreboardUsed(this);
        parentBukkitPlayer.setScoreboard(scoreboard);

        if (scoreboard.getObjective("bbuscoreboard") != null) {
            scoreboard.getObjective("bbuscoreboard").unregister();
        }

        scoreboard.getTeams().stream().filter(team -> CC.isInteger(team.getName(), 10)).forEach(Team::unregister);

        List<String> lines = getLines(parentPlayer);
        if (lines.size() > 15) {
            throw new IllegalArgumentException("You cannot display more than 15 lines at once");
        }

        init(startNumber, lines);
    }

    public static boolean display(Class<? extends BBUScoreboard> scoreboard, Player... players) {
        try {
            displayUnsafe(scoreboard, players);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void displayUnsafe(Class<? extends BBUScoreboard> scoreboard, Player... players) throws BBUScoreboardSetupException {
        for (Player p : players) {
            try {
                scoreboard.getConstructor(Player.class).newInstance(p);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException)
                    throw new BBUScoreboardSetupException(scoreboard.getSimpleName() + " doesn't have the right constructor. Should have " + BBUScoreboard.class.getSimpleName() + "(Player.class)", e);
                e.printStackTrace();
            }
        }
    }

    private void init(int startNumber, List<String> lines) {
        objective = scoreboard.registerNewObjective("bbuscoreboard", "dummy", CC.translate("dummy"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.displayName(CC.translate(title));

        int cache = startNumber;
        for (int i = 0; i < lines.size(); i++) {
            createLine(cache++);
        }

        cache = startNumber;
        for (String line : lines) {
            setLine(cache++, line);
        }
    }

    public abstract ArrayList<String> getLines(BBUPlayer player);

    private Team createLine(int lineNumber) {
        try {
            Team team = scoreboard.registerNewTeam(String.valueOf(lineNumber));

            String pl = chatColourCache[lineNumber].toString() + ChatColor.WHITE;
            team.addEntry(pl);
            objective.getScore(pl).setScore(lineNumber);

            return team;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setTitle(String displayName) {
        this.title = displayName;
        objective.displayName(CC.translate(displayName));
    }

    public boolean isVisible() {
        return this.objective.getDisplaySlot() == DisplaySlot.SIDEBAR;
    }

    public void setVisible(boolean visible) {
        this.objective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    public void setLine(int number, String line) {
        setLine(number, line, true);
    }

    public void setLine(int number, String line, boolean autoSplit) {
        if (number < 1 || number > 15) {
            throw new IllegalArgumentException("The specified line number cannot be less than 1 or bigger than 15");
        }

        Team team = scoreboard.getTeam(String.valueOf(number));

        if (team == null) {
            team = createLine(number);
        }

        String[] split;
        if (line.lastIndexOf('~') == -1 || autoSplit) {
            split = BBUScoreboardUtils.splitTeamText(line);
        } else {
            split = line.split("~");
        }

        String prefix = split[0];
        String suffix = (split.length == 1) ? "" : split[1];

        if (prefix.length() > 16) {
            throw new IllegalArgumentException("Prefix is longer than 16 characters: length=" + prefix.length() + ",line=" + prefix);
        }

        if (suffix != null && suffix.length() > 16) {
            throw new IllegalArgumentException("Suffix is longer than 16 characters: length=" + suffix.length() + ",line=" + suffix);
        }

        team.prefix(CC.translate(prefix));

        if (suffix != null) {
            team.suffix(CC.translate(suffix));
        }
    }

    public TextComponent getLine(int number) {
        if (number < 1 || number > 15) {
            throw new IllegalArgumentException("The specified line number cannot be less than 1 or bigger than 15");
        }

        Team team = scoreboard.getTeam(String.valueOf(number));

        TextComponent ret = (TextComponent) team.prefix();
        TextComponent suffix = (TextComponent) team.suffix();

        if (!suffix.content().isEmpty()) {
            ret = ret.append(CC.translate("~" + CC.translateLegacy(suffix)));
        }

        return ret;
    }

    public void remove() {
        scoreboard.getObjective("bbuscoreboard").unregister();
    }

}
