package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Permissions;

import java.util.List;
import java.util.stream.Collectors;

public class ItemListener implements Listener {
    private final SGCraftRPG plugin;
    public ItemListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked().hasPermission(Permissions.ADMIN)) return;
        if (e.getCurrentItem() == null) return;
        ItemStack item = e.getCurrentItem();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
        FileConfiguration config = plugin.getConfig();
        List<String> list = config.getStringList("despawn-vanilla-items");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (!disabledMaterials.contains(item.getType())) return;
        e.setCurrentItem(null);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (e.getPlayer().hasPermission(Permissions.ADMIN)) return;
        ItemStack offhand = e.getOffHandItem();
        if (offhand == null) return;
        List<String> list = plugin.getConfig().getStringList("disable-offhand");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (!disabledMaterials.contains(offhand.getType())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onSelectOffHand(InventoryClickEvent e) {
        if (e.getWhoClicked().hasPermission(Permissions.ADMIN)) return;
        ItemStack offhand = e.getCursor();
        if (offhand == null) return;
        if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
            if (e.getSlot() == 40) {
                List<String> list = plugin.getConfig().getStringList("disable-offhand");
                List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
                if (!disabledMaterials.contains(e.getCursor().getType())) return;
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteractInventory(InventoryClickEvent e) {
        if (e.getWhoClicked().hasPermission(Permissions.ADMIN)) return;
        if (e.getCurrentItem() == null) return;
        ItemStack item = e.getCurrentItem();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
        FileConfiguration config = plugin.getConfig();
        List<String> list = config.getStringList("despawn-vanilla-items");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (!disabledMaterials.contains(item.getType())) return;
        e.setCurrentItem(null);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer().hasPermission(Permissions.ADMIN)) return;
        FileConfiguration config = plugin.getConfig();
        List<String> list = config.getStringList("despawn-vanilla-items");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        boolean check = false;
        for (Material m : disabledMaterials) {
            if (e.getInventory().contains(m)) {
                check = true;
                break;
            }
        }
        if (check) {
            for (ItemStack item : e.getInventory()) {
                if (item.hasItemMeta() && item.getItemMeta().hasLore()) continue;
                if (disabledMaterials.contains(item.getType())) {
                    item.setAmount(0);
                }
            }
        }
    }
}
