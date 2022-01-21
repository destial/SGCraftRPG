package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.List;
import java.util.stream.Collectors;

public class ItemListener implements Listener {
    private final SGCraftRPG plugin;
    public ItemListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {

    }

    @EventHandler
    public void onInteractInventory(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasLore()) {
                switch (item.getType()) {
                    case WOODEN_AXE:
                        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                            new AttributeModifier("override",
                                                 4d,
                                                        AttributeModifier.Operation.ADD_NUMBER));
                        break;
                }
                return;
            }
        }
        FileConfiguration config = plugin.getConfig();
        List<String> list = config.getStringList("despawn-vanilla-items");
        List<Material> disabledMaterials = list.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (!disabledMaterials.contains(item.getType())) return;
        e.setCurrentItem(null);
    }
}
