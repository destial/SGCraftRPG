package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.dungeon.DungeonManager;
import xyz.destiall.sgcraftrpg.dungeon.DungeonParty;
import xyz.destiall.sgcraftrpg.dungeon.DungeonRoom;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonListener implements Listener {
    private final ConcurrentHashMap<UUID, Location> locations = new ConcurrentHashMap<>();
    private final DungeonManager dm;

    public DungeonListener(SGCraftRPG plugin) {
        this.dm = plugin.getDungeonManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        DungeonParty party = dm.getDungeonParty(e.getPlayer());
        if (party == null) return;
        DungeonRoom room = dm.getDungeonRoom(party);
        if (room == null) return;

        Location last = party.getLastLocation(e.getPlayer().getUniqueId());
        if (last == null) return;
        e.getPlayer().teleport(last);
        locations.put(e.getPlayer().getUniqueId(), last);
        party.removeFromRoom(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        DungeonParty party = dm.getDungeonParty(e.getEntity());
        if (party == null) return;
        DungeonRoom room = dm.getDungeonRoom(party);
        if (room == null) return;

        Location last = party.getLastLocation(e.getEntity().getUniqueId());
        if (last == null) return;

        e.getEntity().spigot().respawn();
        e.getEntity().teleport(last);
        party.removeFromRoom(e.getEntity().getUniqueId());

        room.getDungeon().putOnCooldown(e.getEntity(), dm.getDeathCooldownMultiplier());
        for (String msg : dm.getMessage("dungeon-death"))
            e.getEntity().sendMessage(msg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Location last = locations.remove(e.getPlayer().getUniqueId());
        if (last != null) {
            e.getPlayer().teleport(last);
        }
    }
}
