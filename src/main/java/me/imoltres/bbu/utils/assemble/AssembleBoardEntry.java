package me.imoltres.bbu.utils.assemble;

import lombok.Setter;
import me.imoltres.bbu.utils.CC;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class AssembleBoardEntry {

	private final AssembleBoard board;
	private final int position;
	@Setter
	private TextComponent text;
	private Team team;
	@Setter
	private String identifier;

	/**
	 * Assemble Board Entry
	 *
	 * @param board    that entry belongs to.
	 * @param text     of entry.
	 * @param position of entry.
	 */
	public AssembleBoardEntry(AssembleBoard board, TextComponent text, int position) {
		this.board = board;
		this.text = text;
		this.position = position;
		this.identifier = this.board.getUniqueIdentifier(position);

		this.setup();
	}

	/**
	 * Setup Board Entry.
	 */
	public void setup() {
		Scoreboard scoreboard = this.board.getScoreboard();

		if (scoreboard == null) {
			return;
		}

		String teamName = this.identifier;

		// This shouldn't happen, but just in case.
		if (teamName.length() > 16) {
			teamName = teamName.substring(0, 16);
		}

		Team team = scoreboard.getTeam(teamName);

		// Register the team if it does not exist.
		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
		}

		// Add the entry to the team.
		if (team.getEntries() == null || team.getEntries().isEmpty() || !team.getEntries().contains(this.identifier)) {
			team.addEntry(this.identifier);
		}

		// Add the entry if it does not exist.
		if (!this.board.getEntries().contains(this)) {
			this.board.getEntries().add(this);
		}

		this.team = team;
	}

	/**
	 * Send Board Entry Update.
	 *
	 * @param position of entry.
	 */
	public void send(int position) {
		String translatedText = ChatColor.translateAlternateColorCodes('&', CC.translateLegacy(this.text));
		if (translatedText.length() > 16) {
			String prefix = translatedText.substring(0, 16);
			String suffix;

			if (prefix.charAt(15) == ChatColor.COLOR_CHAR) {
				prefix = prefix.substring(0, 15);
				suffix = translatedText.substring(15);
			} else if (prefix.charAt(14) == ChatColor.COLOR_CHAR) {
				prefix = prefix.substring(0, 14);
				suffix = translatedText.substring(14);
			} else {
				if (ChatColor.getLastColors(prefix).equalsIgnoreCase(ChatColor.getLastColors(this.identifier))) {
					suffix = translatedText.substring(16);
				} else {
					suffix = ChatColor.getLastColors(prefix) + translatedText.substring(16);
				}
			}

			if (suffix.length() > 16) {
				suffix = suffix.substring(0, 16);
			}

			this.team.prefix(CC.translate(prefix.replace(ChatColor.COLOR_CHAR, '&')));
			this.team.suffix(CC.translate(suffix.replace(ChatColor.COLOR_CHAR, '&')));
		} else {
			this.team.prefix(this.text);
			this.team.suffix(CC.translate(""));
		}

		Score score = this.board.getObjective().getScore(this.identifier);
		score.setScore(position);
	}

	/**
	 * Remove Board Entry from Board.
	 */
	public void remove() {
		this.board.getIdentifiers().remove(this.identifier);
		this.board.getScoreboard().resetScores(this.identifier);
	}

}
