package me.imoltres.bbu.game.events.game;

import lombok.Getter;
import me.imoltres.bbu.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Parent player event
 */
public class BBUGameEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Game game;

    public BBUGameEvent(Game game) {
        this.game = game;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
