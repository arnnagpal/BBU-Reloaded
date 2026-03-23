package me.imoltres.bbu.game.events.game;

import lombok.Getter;
import me.imoltres.bbu.game.Game;
import me.imoltres.bbu.game.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BBUGameStateChangeEvent extends BBUGameEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final GameState newState;
    private boolean cancelled = false;


    public BBUGameStateChangeEvent(Game game, GameState newState) {
        super(game);
        this.newState = newState;
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
