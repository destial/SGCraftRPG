package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
import java.util.stream.Collectors;

public class VillagerListener implements Listener {
    private final SGCraftRPG plugin;
    public VillagerListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntityType() != EntityType.VILLAGER && e.getEntityType() != EntityType.ZOMBIE_VILLAGER) return;
        FileConfiguration config = plugin.getConfig();
        if (e.getEntityType() == EntityType.VILLAGER) {
            Villager villager = (Villager) e.getEntity();
            if (villager.getProfession() == Villager.Profession.WEAPONSMITH) {
                if (config.getBoolean("disable-spawning.weaponsmith", false)) e.setCancelled(true);
            } else if (villager.getProfession() == Villager.Profession.ARMORER) {
                if (config.getBoolean("disable-spawning.armorsmith", false)) e.setCancelled(true);
            } else if (villager.getProfession() == Villager.Profession.TOOLSMITH) {
                if (config.getBoolean("disable-spawning.toolsmith", false)) e.setCancelled(true);
            }
        } else {
            if (config.getBoolean("disable-spawning.zombie_villager", false)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChangeProfession(VillagerCareerChangeEvent e) {
        if (e.isCancelled()) return;
        FileConfiguration config = plugin.getConfig();
        if (e.getProfession() == Villager.Profession.WEAPONSMITH) {
            if (config.getBoolean("disable-spawning.weaponsmith", false)) e.setCancelled(true);
        } else if (e.getProfession() == Villager.Profession.ARMORER) {
            if (config.getBoolean("disable-spawning.armorsmith", false)) e.setCancelled(true);
        } else if (e.getProfession() == Villager.Profession.TOOLSMITH) {
            if (config.getBoolean("disable-spawning.toolsmith", false)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCure(EntityTransformEvent e) {
        if (e.getTransformReason() == EntityTransformEvent.TransformReason.CURED || e.getTransformReason() == EntityTransformEvent.TransformReason.INFECTION) {
            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("disable-spawning.zombie_villager", false)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAcquireTrade(VillagerAcquireTradeEvent e) {
        FileConfiguration config = plugin.getConfig();
        ItemStack result = e.getRecipe().getResult();
        List<String> list = config.getStringList("disable-trades");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (disabledMaterials.contains(result.getType())) e.setCancelled(true);
    }

    @EventHandler
    public void onRefreshTrade(VillagerReplenishTradeEvent e) {
        FileConfiguration config = plugin.getConfig();
        ItemStack result = e.getRecipe().getResult();
        List<String> list = config.getStringList("disable-trades");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (disabledMaterials.contains(result.getType())) e.setCancelled(true);
    }
}
