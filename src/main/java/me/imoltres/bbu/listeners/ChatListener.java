package me.imoltres.bbu.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.config.Messages;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        TextComponent msg = (TextComponent) e.message();
        e.setCancelled(true);

        BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(p.getUniqueId());
        BBUTeam team = BBU.getInstance().getTeamController().getTeam(p);
        boolean teamChat = bbuPlayer.getTeamChat();

        if (teamChat && team == null) {
            p.sendMessage(CC.translate("&cYou are not in a team, toggle team chat off to chat normally."));
            return;
        }

        TextComponent format = formatMessage(p, team, msg.content(), teamChat);
        if (teamChat) {
            for (BBUPlayer player : team.getPlayers()) {
                Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
                if (bukkitPlayer == null)
                    continue;
                bukkitPlayer.sendMessage(format);
            }

            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(format);
        }

    }

    private TextComponent formatMessage(Player p, BBUTeam team, String msg, boolean teamChat) {
        if (team == null) {
            return CC.translate(Messages.NO_TEAM_CHAT_FORMAT
                    .replace("{player_name}", p.getName())
                    .replace("{message}", msg)
            );
        }

        if (teamChat) {
            return CC.translate(Messages.TEAM_CHAT_FORMAT
                    .replace("{player_name}", p.getName())
                    .replace("{message}", msg)
                    .replace("{team_color}", "&" + team.getColour().getChatColor().getChar())
                    .replace("{team_name}", team.getName())
            );
        }

        return CC.translate(Messages.ALL_CHAT_FORMAT
                .replace("{player_name}", p.getName())
                .replace("{message}", msg)
                .replace("{team_color}", "&" + team.getColour().getChatColor().getChar())
                .replace("{team_name}", team.getName())
        );
    }

}
