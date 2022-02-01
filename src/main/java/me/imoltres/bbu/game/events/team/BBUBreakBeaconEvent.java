package me.imoltres.bbu.game.events.team;

import lombok.Getter;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BBUBreakBeaconEvent extends BBUTeamEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Block beacon;
    @Getter
    private final BBUPlayer breaker;
    @Getter
    private final boolean teamBreak;

    private boolean cancelled = false;

    public BBUBreakBeaconEvent(Block beacon, BBUTeam team, BBUPlayer breaker, boolean teamBreak) {
        super(team);
        this.beacon = beacon;
        this.breaker = breaker;
        this.teamBreak = teamBreak;
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
