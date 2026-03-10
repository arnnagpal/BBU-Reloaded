package me.imoltres.bbu.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import kotlin.Deprecated;
import kotlin.ReplaceWith;
import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.config.Messages;
import me.imoltres.bbu.utils.scoreboard.BBUScoreboardSetupException;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The display adapter for the scoreboards.
 * Uses Paper's modern Score Component API — each line is a score entry
 * with a custom display name, eliminating the old Team prefix/suffix approach
 * and the invisible-character entry trick entirely.
 */
public abstract class BBUScoreboardAdapter {

    @Getter
    private final transient BBUPlayer parentPlayer;
    @Getter
    private final transient Player parentBukkitPlayer;

    @Getter
    private final Scoreboard scoreboard;
    @Getter
    private String title;
    private Objective objective;

    // unique entry keys per line since the score's customName is what actually renders,
    // these never need to be visible or contain special characters anymore
    private static final String[] LINE_KEYS = new String[16];

    static {
        for (int i = 0; i < 16; i++) {
            LINE_KEYS[i] = "bbu_line_" + i;
        }
    }

    /**
     * Parent constructor for any implementations
     *
     * @param player      player to apply the scoreboard to
     */
    protected BBUScoreboardAdapter(Player player) {
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
            if (p == null) {
                BBU.getInstance().getLogger().severe("[UNEXPECTED] Tried to display a scoreboard to a null player, skipping...");
                continue;
            }
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

        objective = scoreboard.registerNewObjective("bbuscoreboard", Criteria.DUMMY, CC.translate(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateClearBoard(processLinesComponent(getLines(parentPlayer)));
    }

    /**
     * Update the scoreboard
     */
    public void update() {
        List<TextComponent> lines = processLinesComponent(getLines(parentPlayer));
        List<TextComponent> oldLines = getScoreboardLines();

        if (oldLines == null)
            return;

        updateClearBoard(lines);
    }

    /**
     * Update only the lines that have been changed
     *
     * @param lines    new lines
     * @param oldLines old lines
     */
    @Deprecated(
            message = "wonky method, replaced by updateClearBoard for now, will be reworked in the future",
            replaceWith = @ReplaceWith(expression = "updateClearBoard(lines)", imports = {})
    )
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
            if (!a.equals(b)) {
                setLine(i + 1, line);
            }
        }
    }

    /**
     * Clear every line and update it
     *
     * @param lines new lines
     */
    private void updateClearBoard(List<TextComponent> lines) {
        Collections.reverse(lines);

        int newSize = lines.size();

        // Update or create all lines that should exist — existing lines just
        // get their customName swapped in place, no removal needed
        for (int i = 0; i < newSize; i++) {
            int lineNum = i + 1;
            Score score = objective.getScore(LINE_KEYS[lineNum]);
            score.setScore(lineNum);
            score.customName(lines.get(i));
            score.numberFormat(NumberFormat.blank());
        }

        // Only after new content is visible, clean up lines that no longer exist
        for (int i = newSize + 1; i <= 15; i++) {
            if (objective.getScore(LINE_KEYS[i]).isScoreSet()) {
                scoreboard.resetScores(LINE_KEYS[i]);
            }
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
     * Create a line on the scoreboard with initial content
     *
     * @param lineNumber line number (determines score / vertical position)
     * @param content    content to display
     */
    private void createLine(int lineNumber, TextComponent content) {
        scoreboard.resetScores(LINE_KEYS[lineNumber]);

        Score score = objective.getScore(LINE_KEYS[lineNumber]);
        score.setScore(lineNumber);
        score.customName(content);
        score.numberFormat(NumberFormat.blank());
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
        setLine(number, CC.translate(line));
    }

    /**
     * Set a specific line on the scoreboard directly from a component
     *
     * @param number line number
     * @param line   new line as a component
     */
    public void setLine(int number, TextComponent line) {
        if (number < 1 || number > 15) {
            throw new IllegalArgumentException("The specified line number cannot be less than 1 or bigger than 15");
        }

        Score score = objective.getScore(LINE_KEYS[number]);
        if (!score.isScoreSet()) {
            // line doesn't exist yet so we need to create it first
            score.setScore(number);
            score.numberFormat(NumberFormat.blank());
        }

        score.customName(line);
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

        Score score = objective.getScore(LINE_KEYS[number]);
        if (!score.isScoreSet()) {
            return null;
        }

        return (TextComponent) score.customName();
    }

    /**
     * Get all current displayed lines
     *
     * @return list of current displayed lines
     */
    public List<TextComponent> getScoreboardLines() {
        // Count active lines by checking which keys are set
        int count = 0;
        for (int i = 1; i <= 15; i++) {
            if (objective.getScore(LINE_KEYS[i]).isScoreSet()) {
                count++;
            }
        }

        List<TextComponent> l = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            TextComponent line = getLine(i);
            if (line == null)
                return null;

            l.add(line);
        }

        Collections.reverse(l);

        return l;
    }

    /**
     * Process the list into a 15-element list
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
     * Upgrade from legacy colours to adventure colours
     * and process the list into a 15-element list
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