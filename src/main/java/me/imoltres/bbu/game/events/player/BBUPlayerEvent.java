package me.imoltres.bbu.game.events.player;

import lombok.Getter;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Parent player event
 */
public class BBUPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final BBUPlayer player;

    public BBUPlayerEvent(BBUPlayer player) {
        this.player = player;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
