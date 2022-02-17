package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (BBU.getInstance().getGame().getGameState().isSpawn()) {
            e.getWorld().setThundering(false);
            e.getWorld().setStorm(false);
        }
    }

}
