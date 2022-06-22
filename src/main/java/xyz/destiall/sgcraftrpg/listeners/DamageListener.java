package xyz.destiall.sgcraftrpg.listeners;

import com.sucy.skill.api.event.SkillDamageEvent;
import com.zenya.damageindicator.event.HologramSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Pair;

import java.util.concurrent.ConcurrentHashMap;

public class DamageListener implements Listener {
    private final ConcurrentHashMap<LivingEntity, Pair<String, LivingEntity>> cache = new ConcurrentHashMap<>();
    private final SGCraftRPG plugin;

    public DamageListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageSkill(SkillDamageEvent event) {
        cache.put(event.getTarget(), new Pair<>(event.getClassification(), event.getDamager()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHologram(HologramSpawnEvent e) {
        if (e.getEvent() instanceof EntityRegainHealthEvent) return;

        Pair<String, LivingEntity> classification = cache.remove(e.getEntity());
        if (classification == null) {
            if (Math.abs(e.getAmount()) <= 0.02) {
                e.setCancelled(true);
            }
            return;
        }
        String newFormat = plugin.getConfig().getString("damage-indicator." + classification.getKey());
        if (newFormat == null) return;
        String format = e.getFormat();
        e.setFormat(newFormat.replace("%original%", format));
    }
}
