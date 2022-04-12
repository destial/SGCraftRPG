package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.dungeon.DungeonManager;
import xyz.destiall.sgcraftrpg.dungeon.DungeonParty;
import xyz.destiall.sgcraftrpg.dungeon.DungeonRoom;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonListener implements Listener {
    private final DungeonManager dm;

    private final Map<UUID, Location> locations = new ConcurrentHashMap<>();

    public DungeonListener(SGCraftRPG plugin) {
        this.dm = plugin.getDungeonManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Location last = locations.remove(e.getPlayer().getUniqueId());
        if (last != null) {
            e.getPlayer().teleport(last);
        }
    }
}