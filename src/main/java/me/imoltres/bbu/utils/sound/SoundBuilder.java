package me.imoltres.bbu.utils.sound;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundBuilder {

    private Sound sound;
    private float pitch;
    private float volume;

    public SoundBuilder(Sound sound) {
        this.sound = sound;
        this.pitch = 1;
        this.volume = 1;
    }

    public SoundBuilder(Sound sound, float pitch) {
        this.sound = sound;
        this.pitch = pitch;
        this.volume = 1;
    }

    public SoundBuilder(Sound sound, float pitch, float volume) {
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    public SoundBuilder sound(Sound sound) {
        this.sound = sound;
        return this;
    }

    public SoundBuilder pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public SoundBuilder volume(float volume) {
        this.volume = volume;
        return this;
    }

    /**
     * Play this sound effect at the specified location
     *
     * @param pos Position to play the sound at
     */
    public void play(Location pos) {
        pos.getWorld().playSound(pos, sound, volume, pitch);
    }

    /**
     * Play this sound effect for the specified player
     *
     * @param players Players to play sound to
     */
    public void play(Player... players) {
        for(Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Broadcast a sound to all players
     */
    public void broadcast() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            play(player);
        }
    }
}
