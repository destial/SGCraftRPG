package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.inventory.ItemStack;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.List;

public class VillagerListener implements Listener {
    private final SGCraftRPG plugin;

    public VillagerListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntityType() != EntityType.VILLAGER && e.getEntityType() != EntityType.ZOMBIE_VILLAGER) return;
        if (e.getEntityType() == EntityType.VILLAGER) {
            Villager villager = (Villager) e.getEntity();
            if (villager.getProfession() == Villager.Profession.WEAPONSMITH) {
                if (plugin.getConfig().getBoolean("disable-spawning.weaponsmith", false)) {
                    e.setCancelled(true);
                }
            } else if (villager.getProfession() == Villager.Profession.ARMORER) {
                if (plugin.getConfig().getBoolean("disable-spawning.armorsmith", false)) {
                    e.setCancelled(true);
                }
            } else if (villager.getProfession() == Villager.Profession.TOOLSMITH) {
                if (plugin.getConfig().getBoolean("disable-spawning.toolsmith", false)) {
                    e.setCancelled(true);
                }
            }
        } else {
            if (plugin.getConfig().getBoolean("disable-spawning.zombie_villager", false)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChangeProfession(VillagerCareerChangeEvent e) {
        if (e.getProfession() == Villager.Profession.WEAPONSMITH) {
            if (plugin.getConfig().getBoolean("disable-spawning.weaponsmith", false)) {
                e.setCancelled(true);
            }
        } else if (e.getProfession() == Villager.Profession.ARMORER) {
            if (plugin.getConfig().getBoolean("disable-spawning.armorsmith", false)) {
                e.setCancelled(true);
            }
        } else if (e.getProfession() == Villager.Profession.TOOLSMITH) {
            if (plugin.getConfig().getBoolean("disable-spawning.toolsmith", false)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCure(EntityTransformEvent e) {
        if (e.getTransformReason() == EntityTransformEvent.TransformReason.CURED ||
            e.getTransformReason() == EntityTransformEvent.TransformReason.INFECTION) {

            if (plugin.getConfig().getBoolean("disable-spawning.zombie_villager", false)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAcquireTrade(VillagerAcquireTradeEvent e) {
        ItemStack result = e.getRecipe().getResult();
        List<String> list = plugin.getConfig().getStringList("disable-trades");
        if (list.stream().map(Material::getMaterial).anyMatch(m -> m == result.getType())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRefreshTrade(VillagerReplenishTradeEvent e) {
        ItemStack result = e.getRecipe().getResult();
        List<String> list = plugin.getConfig().getStringList("disable-trades");
        if (list.stream().map(Material::getMaterial).anyMatch(m -> m == result.getType())) {
            e.setCancelled(true);
        }
    }
}
