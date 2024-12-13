package me.imoltres.bbu.scoreboard;

import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.config.Messages;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardSetupException;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardUtils;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
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

/**
 * The display adapter for the scoreboards
 */
public abstract class BBUScoreboardAdapter {

    @Getter
    private final transient BBUPlayer parentPlayer;
    @Getter
    private final transient Player parentBukkitPlayer;

    @Getter
    private final Scoreboard scoreboard;
    private final ChatColor[] chatColourCache = ChatColor.values();
    private final int startNumber;
    @Getter
    private String title;
    private Objective objective;

    /**
     * Parent constructor for any implementations
     *
     * @param startNumber Number to start the scoreboard lines at
     * @param player      player to apply the scoreboard to
     */
    protected BBUScoreboardAdapter(int startNumber, Player player) {
        this.startNumber = startNumber;
        this.title = Messages.SCOREBOARD_TITLE;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.parentPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
        this.parentBukkitPlayer = player;

        parentPlayer.setScoreboard(this);
        parentBukkitPlayer.setScoreboard(scoreboard);

        init();
    }

    /**
     * Display multiple scoreboards at once to a set of players
     *
     * @param scoreboard Scoreboard class
     * @param players    list of players
     * @return if it was successful
     */
    public static boolean display(Class<? extends BBUScoreboardAdapter> scoreboard, Player... players) {
        try {
            displayUnsafe(scoreboard, players);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Display multiple scoreboards at once to a set of players
     *
     * @param scoreboard scoreboard class
     * @param players    list of players
     * @throws BBUScoreboardSetupException throws when the constructor is off
     */
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

    /**
     * Setup the scoreboard
     */
    private void init() {
        if (scoreboard.getObjective("bbuscoreboard") != null) {
            remove();
        }

        objective = scoreboard.registerNewObjective("bbuscoreboard", "dummy", CC.translate("dummy"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.displayName(CC.translate(title));

        updateClearBoard(CC.translate(getLines(parentPlayer)).stream().map(component -> (TextComponent) component).collect(Collectors.toList()));
    }

    /**
     * Update the scoreboard
     */
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

    /**
     * Update only the lines that have been changed
     *
     * @param lines    new lines
     * @param oldLines old lines
     */
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

    /**
     * Clear every line and update it
     *
     * @param lines new lines
     */
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

    /**
     * The lines on the scoreboard to get
     *
     * @param player player the scoreboard is applied to
     * @return an ordered list of lines
     */
    public abstract ArrayList<String> getLines(BBUPlayer player);

    /**
     * Create a line on the scoreboard
     *
     * @param lineNumber line number to create
     * @return team that the line is on
     */
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

    /**
     * Set the title of the scoreboard on the fly
     *
     * @param displayName title
     */
    public void setTitle(String displayName) {
        this.title = displayName;
        objective.displayName(CC.translate(displayName));
    }

    /**
     * @return if the scoreboard is visible or not
     */
    public boolean isVisible() {
        return this.objective.getDisplaySlot() == DisplaySlot.SIDEBAR;
    }

    /**
     * Sets the scoreboard visibility
     *
     * @param visible scoreboard visibility
     */
    public void setVisible(boolean visible) {
        this.objective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    /**
     * Set a specific line on the scoreboard
     *
     * @param number line number
     * @param line   new line
     */
    public void setLine(int number, String line) {
        setLine(number, line, true);
    }

    /**
     * Set a specific line on the scoreboard,
     * specify if the method should automatically
     * split the lines into a prefix/suffix pair or
     * if the scoreboard already accounts for that with a '~'
     *
     * @param number    line number
     * @param line      new line
     * @param autoSplit should auto split?
     */
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

    /**
     * Get a line from the scoreboard
     *
     * @param number line number
     * @return line
     */
    public TextComponent getLine(int number) {
        if (number < 1 || number > 15) {
            throw new IllegalArgumentException("The specified line number cannot be less than 1 or bigger than 15");
        }

        Team team = scoreboard.getTeam(String.valueOf(number));
        if (team == null) {
            return null;
        }

        try {
            TextComponent ret = (TextComponent) team.prefix();
            TextComponent suffix = (TextComponent) team.suffix();

            if (!suffix.content().isEmpty()) {
                ret = ret.append(suffix);
            }

            return ret;
        } catch (Exception e) {
            System.out.println("Error while grabbing line " + number);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all current displayed lines
     *
     * @return list of current displayed lines
     */
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

    /**
     * Process the list into a 16-element list
     *
     * @param lines list of lines
     * @return processed list of lines
     */
    private List<String> processLines(List<String> lines) {
        if (lines.size() > 15) {
            lines = lines.subList(0, 15);
        }

        return lines;
    }

    /**
     * Process the list into a 16-element list
     *
     * @param lines list of lines
     * @return processed list of lines
     */
    private List<TextComponent> processLinesComponent(List<String> lines) {
        return CC.translate(processLines(lines)).stream().map(component -> (TextComponent) component).collect(Collectors.toList());
    }

    /**
     * Cleanup this instance of the scoreboard
     */
    public void remove() {
        Objective objective = scoreboard.getObjective("bbuscoreboard");

        if (objective != null)
            objective.unregister();
    }

}
