package me.imoltres.bbu.game.events.player;

import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Whenever a scoreboard is about to be applied to someone, this event is called.
 */
public class BBUPlayerScoreboardApplyEvent extends BBUPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    @Getter
    @Setter
    private Class<? extends BBUScoreboardAdapter> scoreboard;

    public BBUPlayerScoreboardApplyEvent(BBUPlayer player, Class<? extends BBUScoreboardAdapter> scoreboard) {
        super(player);
        this.scoreboard = scoreboard;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
