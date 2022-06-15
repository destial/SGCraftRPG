package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Permissions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemListener implements Listener {
    private final SGCraftRPG plugin;
    private final List<CookingRecipe<?>> cookingRecipes;
    public ItemListener(SGCraftRPG plugin) {
        this.plugin = plugin;
        cookingRecipes = new ArrayList<>();
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe instanceof CookingRecipe<?> c) {
                cookingRecipes.add(c);
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked().hasPermission(Permissions.ADMIN)) return;
        ItemStack item = e.getCurrentItem();
        List<String> list = plugin.getConfig().getStringList("despawn-vanilla-items");
        if (item != null) {
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
            boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == item.getType());
            if (match) {
                e.setCurrentItem(null);
                return;
            }
        }
        ItemStack result = e.getRecipe().getResult();
        if (result.hasItemMeta() && result.getItemMeta().hasLore()) return;
        boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == result.getType());
        if (match) e.setCurrentItem(null);
    }

    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent e) {
        ItemStack item = e.getRecipe().getResult();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
        List<String> list = plugin.getConfig().getStringList("despawn-vanilla-items");
        boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == item.getType());
        if (match) e.setTotalCookTime(Integer.MAX_VALUE);
    }

    @EventHandler
    public void onFurnaceBurnItem(FurnaceBurnEvent e) {
        Furnace f = (Furnace) e.getBlock().getState();
        ItemStack smelting = f.getInventory().getSmelting();
        Iterator<CookingRecipe<?>> it = cookingRecipes.iterator();
        ItemStack item = null;
        if (smelting == null) return;
        while (it.hasNext()) {
            CookingRecipe<?> recipe = it.next();
            if (recipe.getInput().getType() == smelting.getType()) {
                item = recipe.getResult();
                break;
            }
        }
        if (item == null || item.hasItemMeta() && item.getItemMeta().hasLore()) return;
        List<String> list = plugin.getConfig().getStringList("despawn-vanilla-items");
        ItemStack finalItem = item;
        boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == finalItem.getType());
        if (match) e.setCancelled(true);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (e.getPlayer().hasPermission(Permissions.ADMIN)) return;
        ItemStack offhand = e.getOffHandItem();
        if (offhand == null) return;
        List<String> list = plugin.getConfig().getStringList("disable-offhand");
        boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == offhand.getType());
        if (match) e.setCancelled(true);
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        if (e.getWhoClicked().hasPermission(Permissions.ADMIN)) return;
        ItemStack offhand = e.getCursor();
        if (offhand != null) {
            if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                if (e.getSlot() == 40) {
                    List<String> list = plugin.getConfig().getStringList("disable-offhand");
                    boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == e.getCursor().getType());
                    if (match) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        ItemStack item = e.getCurrentItem();
        List<String> list = plugin.getConfig().getStringList("despawn-vanilla-items");
        if (item != null) {
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
            boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == item.getType());
            if (match) {
                e.setCurrentItem(null);
                return;
            }
        }

        if (e.getClickedInventory() != null) {
            ItemStack clickedItem = e.getClickedInventory().getItem(e.getSlot());
            if (clickedItem == null || clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) return;
            boolean match = list.stream().map(Material::getMaterial).anyMatch(m -> m == clickedItem.getType());
            if (match) {
                clickedItem.setAmount(0);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer().hasPermission(Permissions.ADMIN)) return;
        List<String> list = plugin.getConfig().getStringList("despawn-vanilla-items");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        boolean check = disabledMaterials.stream().anyMatch(m -> m != null && e.getInventory().contains(m));
        if (check) {
            for (ItemStack item : e.getInventory()) {
                if (item == null || (item.hasItemMeta() && item.getItemMeta().hasLore())) continue;
                if (disabledMaterials.stream().anyMatch(m -> m == item.getType())) {
                    item.setAmount(0);
                }
            }
        }
    }
}
