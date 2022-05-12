package xyz.destiall.sgcraftrpg.listeners;

import com.zenya.damageindicator.event.HologramSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DamageListener implements Listener {
    private final Map<LivingEntity, Double> cache = new ConcurrentHashMap<>();
    private final SGCraftRPG plugin;

    public DamageListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof LivingEntity) {
            cache.put((LivingEntity) e.getEntity(), e.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHologram(HologramSpawnEvent e) {
        if (e.getEvent() instanceof EntityRegainHealthEvent) return;
        Double damage = cache.remove(e.getEntity());
        if (damage == null) return;
        if (damage < e.getAmount()) {
            String format = e.getFormat();
            String critFormat = plugin.getConfig().getString("crit-format");
            critFormat = Formatter.color(critFormat);
            critFormat = critFormat.replace("%original%", format);
            e.setFormat(critFormat);
        }
    }
}
