package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.List;
import java.util.stream.Collectors;

public class InteractListener implements Listener {
    private final SGCraftRPG plugin;

    public InteractListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;

        List<String> materials = plugin.getConfig().getStringList("disable-block-interactions");
        List<Material> forbidden = materials.stream().map(Material::getMaterial).collect(Collectors.toList());
        if (forbidden.contains(block.getType())) {
            e.setCancelled(true);
        }
    }
}
