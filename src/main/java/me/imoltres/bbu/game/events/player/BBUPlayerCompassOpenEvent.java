package me.imoltres.bbu.game.events.player;

import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.menus.TrackingCompassMenu;
import me.imoltres.bbu.utils.menu.Menu;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Whenever a playing player dies, this event is called.
 */
public class BBUPlayerCompassOpenEvent extends BBUPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    @Getter
    @Setter
    private Menu compassMenu;

    public BBUPlayerCompassOpenEvent(BBUPlayer player) {
        super(player);
        this.compassMenu = new TrackingCompassMenu();
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
