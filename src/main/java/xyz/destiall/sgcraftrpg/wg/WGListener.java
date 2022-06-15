package xyz.destiall.sgcraftrpg.wg;

import com.Zrips.CMI.events.CMIAsyncPlayerTeleportEvent;
import com.Zrips.CMI.events.CMIPlayerTeleportEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class WGListener implements Listener {
    private final WGManager manager;

    public WGListener(WGManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        processFlags(e.getFrom(), e.getTo(), e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(CMIPlayerTeleportEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        processFlags(e.getPlayer().getLocation(), e.getTo(), e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(CMIAsyncPlayerTeleportEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        processFlags(e.getPlayer().getLocation(), e.getTo(), e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        processFlags(e.getFrom(), e.getTo(), e.getPlayer(), e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null) return;
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
        QueryResult result = queryFlag(e.getPlayer().getLocation(), WGManager.Flags.NO_NORMAL_TOOLS);

        if (isTool(item.getType()) && result.accepted) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(manager.getMessage(WGManager.Flags.NO_NORMAL_TOOLS));
        }
    }

    private void processFlags(Location from, Location to, Player player, Cancellable e) {
        PlayerData data = SkillAPI.getPlayerData(player);
        PlayerClass clazz = data.getMainClass();
        int level = clazz == null ? 0 : clazz.getLevel();
        QueryResult enter = queryFlag(to, WGManager.Flags.NO_CLASS_ENTER);
        if (enter.accepted) {
            if (clazz == null) {
                e.setCancelled(true);
                player.sendMessage(manager.getMessage(WGManager.Flags.NO_CLASS_ENTER));
                return;
            }
        }
        QueryResult requirement = queryFlag(to, WGManager.Flags.LEVEL_REQUIREMENT, level);
        if (requirement.accepted) {
            e.setCancelled(true);
            player.sendMessage(manager.getMessage(WGManager.Flags.LEVEL_REQUIREMENT).replace("{level}", ""+requirement.integer));
            return;
        }
        QueryResult toResult = queryFlag(to, WGManager.Flags.NO_CLASS_EXIT);
        QueryResult fromResult = queryFlag(from, WGManager.Flags.NO_CLASS_EXIT);
        if (!toResult.accepted && fromResult.accepted) {
            if (clazz == null) {
                e.setCancelled(true);
                player.sendMessage(manager.getMessage(WGManager.Flags.NO_CLASS_EXIT));
            }
        }
    }

    private ApplicableRegionSet getRegion(Location location) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(location));
    }

    private QueryResult queryFlag(Location location, WGManager.Flags flag) {
        ApplicableRegionSet set = getRegion(location);
        if (set == null) return new QueryResult();
        StateFlag f = manager.getFlag(flag, StateFlag.class);
        if (f == null) return new QueryResult();
        QueryResult result = new QueryResult();
        result.accepted = set.queryValue(null, f) == StateFlag.State.ALLOW;
        return result;
    }

    private QueryResult queryFlag(Location location, WGManager.Flags flag, int integer) {
        ApplicableRegionSet set = getRegion(location);
        if (set == null) return new QueryResult();
        IntegerFlag f = manager.getFlag(flag, IntegerFlag.class);
        if (f == null) return new QueryResult();
        Integer query = set.queryValue(null, f);
        QueryResult result = new QueryResult();
        result.integer = query != null ? query : 0;
        result.accepted = integer < result.integer;
        return result;
    }

    private boolean isTool(Material material) {
        return material.name().contains("_AXE") || material.name().contains("_HOE") || material.name().contains("_SWORD") ||
                material.name().contains("_PICKAXE") || material.name().contains("_SHOVEL") || material.name().contains("_SPADE");
    }

    public static class QueryResult {
        public int integer;
        public boolean accepted;
    }

}
