package me.imoltres.bbu.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

@Getter
@RequiredArgsConstructor
public enum BBUTeamColour {
    RED(ChatColor.RED),
    GREEN(ChatColor.GREEN),
    BLUE(ChatColor.BLUE),
    YELLOW(ChatColor.YELLOW),
    ORANGE(ChatColor.GOLD),
    PURPLE(ChatColor.DARK_PURPLE),
    PINK(ChatColor.LIGHT_PURPLE),
    GRAY(ChatColor.GRAY);

    private final ChatColor chatColor;

}
