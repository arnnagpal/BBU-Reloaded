package me.imoltres.bbu.game.events.team;

import lombok.Getter;
import me.imoltres.bbu.data.team.BBUTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Parent team event
 */
public class BBUTeamEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final BBUTeam team;

    public BBUTeamEvent(BBUTeam team) {
        this.team = team;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
