package xyz.destiall.sgcraftrpg.listeners;

import com.Zrips.CMI.AllListeners.PlayerListeners;
import com.Zrips.CMI.commands.list.effect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.path.Path;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InteractListener implements Listener {
    private final SGCraftRPG plugin;
    private final Map<UUID, PotionEffect> deaths;

    public InteractListener(SGCraftRPG plugin) {
        this.plugin = plugin;
        deaths = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;

        List<String> materials = plugin.getConfig().getStringList("disable-block-interactions");
        boolean match = materials.stream().map(Material::getMaterial).anyMatch(m -> m == block.getType());
        if (match) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PotionEffect effect = deaths.remove(e.getPlayer().getUniqueId());
            if (effect == null) return;
            effect.apply(e.getPlayer());
            plugin.getLogger().info("Re-applying xp boost to " + e.getPlayer().getName());
        }, 10L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeath(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getFinalDamage() >= p.getHealth()) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                if (effect.getType() == PotionEffectType.LUCK) {
                    deaths.put(e.getEntity().getUniqueId(), effect);
                    plugin.getLogger().info(e.getEntity().getName() + " died with xp boost");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        for (PotionEffect effect : e.getEntity().getActivePotionEffects()) {
            if (effect.getType() == PotionEffectType.LUCK) {
                deaths.put(e.getEntity().getUniqueId(), effect);
                plugin.getLogger().info(e.getEntity().getName() + " died with xp boost");
            }
        }
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPlayedBefore()) return;
        Path path = plugin.getPathManager().getPath("tutorial");
        plugin.getPathManager().startRenderPath(path, e.getPlayer());
    }
}
