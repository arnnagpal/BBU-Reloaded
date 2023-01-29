package me.imoltres.bbu.game.events.team;

import lombok.Getter;
import me.imoltres.bbu.data.team.BBUTeam;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;

/**
 * Whenever a team is modified, this event is called.
 */
public class BBUTeamModificationEvent extends BBUTeamEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final AbstractMap.SimpleEntry<ModificationType, Object> type;

    private boolean cancelled = false;

    public BBUTeamModificationEvent(BBUTeam team, AbstractMap.SimpleEntry<ModificationType, Object> type) {
        super(team);
        this.type = type;
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

    public enum ModificationType {
        ADD_PLAYER,
        REMOVE_PLAYER,

        ASSIGN_BEACON,

        ASSIGN_CAGE,
        ASSIGN_BUKKIT_TEAM
    }

}
