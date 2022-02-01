package me.imoltres.bbu.scoreboard;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardSetupException;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardUtils;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BBUScoreboardAdapter {

    @Getter
    private final transient BBUPlayer parentPlayer;
    @Getter
    private final transient Player parentBukkitPlayer;

    @Getter
    private final Scoreboard scoreboard;
    private final ChatColor[] chatColourCache = ChatColor.values();
    private final int startNumber;
    protected BasicConfigurationFile messages = BBU.getInstance().getMessagesConfig();
    @Getter
    private String title;
    private Objective objective;

    protected BBUScoreboardAdapter(int startNumber, Player player) {
        this.startNumber = startNumber;
        this.title = messages.getString("scoreboard-title");
        this.scoreboard = player.getScoreboard();
        this.parentPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
        this.parentBukkitPlayer = player;

        parentPlayer.setScoreboard(this);
        parentBukkitPlayer.setScoreboard(scoreboard);

        init();
    }

    public static boolean display(Class<? extends BBUScoreboardAdapter> scoreboard, Player... players) {
        try {
            displayUnsafe(scoreboard, players);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void displayUnsafe(Class<? extends BBUScoreboardAdapter> scoreboard, Player... players) throws BBUScoreboardSetupException {
        for (Player p : players) {
            try {
                scoreboard.getConstructor(Player.class).newInstance(p);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException)
                    throw new BBUScoreboardSetupException(scoreboard.getSimpleName() + " doesn't have the right constructor. Should have " + BBUScoreboardAdapter.class.getSimpleName() + "(Player.class)", e);
                e.printStackTrace();
            }
        }
    }

    private void init() {
        if (scoreboard.getObjective("bbuscoreboard") != null) {
            remove();
        }

        objective = scoreboard.registerNewObjective("bbuscoreboard", "dummy", CC.translate("dummy"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.displayName(CC.translate(title));

        updateClearBoard(CC.translate(getLines(parentPlayer)).stream().map(component -> (TextComponent) component).collect(Collectors.toList()));
    }

    public void update() {
        List<TextComponent> lines = processLinesComponent(getLines(parentPlayer));
        List<TextComponent> oldLines = getScoreboardLines();

        if (oldLines == null)
            return;

        if (lines.size() > oldLines.size())
            updateClearBoard(lines);
        else
            updateNewLines(lines, oldLines);
    }

    private void updateNewLines(List<TextComponent> lines, List<TextComponent> oldLines) {
        Collections.reverse(lines);
        Collections.reverse(oldLines);

        for (int i = 0; i < Math.max(lines.size(), oldLines.size()); i++) {
            if (i >= (lines.size() - 1) || i >= (oldLines.size() - 1)) {
                continue;
            }

            TextComponent line = lines.get(i);
            TextComponent oldLine = oldLines.get(i);

            String a = GsonComponentSerializer.colorDownsamplingGson().serialize(line);
            String b = GsonComponentSerializer.colorDownsamplingGson().serialize(oldLine);

            //TODO: fix updating lines that don't need to be updated, it's a bit wonky atm, but it gets the job done
            if (!StringUtils.difference(a, b).isEmpty()) {
                setLine(i + 1, CC.translateLegacy(line));
            }
        }
    }

    private void updateClearBoard(List<TextComponent> lines) {
        scoreboard.getTeams().stream().filter(team -> CC.isInteger(team.getName(), 10)).forEach(Team::unregister);

        Collections.reverse(lines);

        int cache = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            createLine(cache--);
        }

        cache = startNumber;
        for (TextComponent line : lines) {
            setLine(cache++, CC.translateLegacy(line));
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

        assert team != null;

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

        if (suffix != null)
            team.suffix(CC.translate(suffix));
    }

    public TextComponent getLine(int number) {
        if (number < 1 || number > 15) {
            throw new IllegalArgumentException("The specified line number cannot be less than 1 or bigger than 15");
        }

        Team team = scoreboard.getTeam(String.valueOf(number));
        if (team == null) {
            return null;
        }

        TextComponent ret = (TextComponent) team.prefix();
        TextComponent suffix = (TextComponent) team.suffix();

        if (!suffix.content().isEmpty()) {
            ret = ret.append(suffix);
        }

        return ret;
    }

    public List<TextComponent> getScoreboardLines() {
        long linesSize = new ArrayList<>(scoreboard.getTeams()).stream().filter(team -> CC.isInteger(team.getName(), 10)).count();

        List<TextComponent> l = new ArrayList<>();

        for (int i = 1; i <= linesSize; i++) {
            TextComponent line = getLine(i);
            if (line == null)
                return null;

            l.add(getLine(i));
        }

        Collections.reverse(l);

        return l;
    }

    private List<String> processLines(List<String> lines) {
        if (lines.size() > 15) {
            lines = lines.subList(0, 15);
        }

        return lines;
    }

    private List<TextComponent> processLinesComponent(List<String> lines) {
        return CC.translate(processLines(lines)).stream().map(component -> (TextComponent) component).collect(Collectors.toList());
    }

    public void remove() {
        scoreboard.getObjective("bbuscoreboard").unregister();
    }

}
