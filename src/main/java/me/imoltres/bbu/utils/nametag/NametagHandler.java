package me.imoltres.bbu.utils.nametag;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class NametagHandler {

    private JavaPlugin plugin;
    private NametagAdapter adapter;
    private Map<UUID, NametagBoard> boards;
    private NametagThread thread;
    private NametagListeners listeners;
    private long ticks = 2;
    private boolean hook = false;


    public NametagHandler(JavaPlugin plugin, NametagAdapter adapter) {
        if (plugin == null) {
            throw new RuntimeException("Nametag Handler can not be instantiated without a plugin instance!");
        }

        this.plugin = plugin;
        this.adapter = adapter;
        this.boards = new ConcurrentHashMap<>();

        this.setup();
    }

    public void setup() {
        System.out.println("Setting up Nametag Handler");
        //Register Events
        this.listeners = new NametagListeners(this);
        this.plugin.getServer().getPluginManager().registerEvents(this.listeners, this.plugin);

        // Ensure that the thread has stopped running
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            //Call Event
            getBoards().putIfAbsent(player.getUniqueId(), new NametagBoard(player, this));
        }

        this.thread = new NametagThread(this);
    }

    public void cleanup() {
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }

        if (this.listeners != null) {
            HandlerList.unregisterAll(this.listeners);
            this.listeners = null;
        }

        for (UUID uuid : getBoards().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            NametagBoard board = getBoards().get(uuid);
            board.cleanup();

            getBoards().remove(uuid);
        }
    }

}
