package xyz.destiall.sgcraftrpg.listeners;

import com.sucy.skill.api.event.SkillDamageEvent;
import com.zenya.damageindicator.event.HologramSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DamageListener implements Listener {
    private final Map<LivingEntity, String> cache = new ConcurrentHashMap<>();
    private final SGCraftRPG plugin;

    public DamageListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(SkillDamageEvent event) {
        if (event.getDamager() instanceof Player) {
            cache.put(event.getTarget(), event.getClassification());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHologram(HologramSpawnEvent e) {
        if (e.getEvent() instanceof EntityRegainHealthEvent) return;
        String classification = cache.remove(e.getEntity());
        if (classification == null) return;
        String newFormat = plugin.getConfig().getString("damage-indicator." + classification);
        if (newFormat == null) return;
        String format = e.getFormat();
        e.setFormat(newFormat.replace("%original%", format));
    }
}
