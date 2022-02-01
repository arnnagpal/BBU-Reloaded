package me.imoltres.bbu.game.events.player;

import lombok.Getter;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BBUPlayerDeathEvent extends BBUPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final BBUPlayer killer;

    @Getter
    private final boolean finalDeath;

    public BBUPlayerDeathEvent(BBUPlayer player, BBUPlayer killer, boolean finalDeath) {
        super(player);
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
