package me.imoltres.bbu.game.events.player;

import lombok.Getter;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.world.WorldPosition;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Whenever a playing player dies, this event is called.
 */
public class BBUPlayerDeathEvent extends BBUPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final WorldPosition position;

    @Getter
    private final BBUPlayer killer;

    @Getter
    private final boolean finalDeath;

    public BBUPlayerDeathEvent(WorldPosition position, BBUPlayer player, BBUPlayer killer, boolean finalDeath) {
        super(player);
        this.position = position;
        this.killer = killer;
        this.finalDeath = finalDeath;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
