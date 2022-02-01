package me.imoltres.bbu.game.events.team;

import lombok.Getter;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.world.WorldPosition;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BBUPlaceBeaconEvent extends BBUTeamEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final WorldPosition position;

    @Getter
    private final BBUPlayer placer;

    private boolean cancelled = false;

    public BBUPlaceBeaconEvent(BBUTeam team, BBUPlayer placer, WorldPosition position) {
        super(team);
        this.position = position;
        this.placer = placer;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
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
}
