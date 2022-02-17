package me.imoltres.bbu.listeners;

import me.imoltres.bbu.utils.config.Nerfs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class NerfsListener implements Listener {

    private final List<EntityType> entities = Nerfs.REMOVE_MOBS_SPAWN
            .stream()
            .map(s -> EntityType.valueOf(s.toUpperCase()))
            .toList();

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (!entities.contains(e.getEntityType()))
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player damager) {
            //if they have strength
            if (damager.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                e.setDamage(Nerfs.POTION_STRENGTH * e.getDamage());
            }

            if (damager.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                e.setDamage(Nerfs.POTION_WEAKNESS * e.getDamage());
            }
        }

        if (e.getEntity() instanceof LivingEntity entity) {
            if (entity.hasPotionEffect(PotionEffectType.HARM)) {
                e.setDamage(Nerfs.POTION_DAMAGE * e.getDamage());
            }
        }
    }

}
