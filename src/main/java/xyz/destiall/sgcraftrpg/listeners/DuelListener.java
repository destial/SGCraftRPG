package xyz.destiall.sgcraftrpg.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.duel.DuelArena;
import xyz.destiall.sgcraftrpg.duel.DuelManager;
import xyz.destiall.sgcraftrpg.duel.DuelParty;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelListener implements Listener {
    private final ConcurrentHashMap<UUID, Location> locations = new ConcurrentHashMap<>();
    private final DuelManager dm;

    public DuelListener(SGCraftRPG plugin) {
        this.dm = plugin.getDuelManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        DuelParty party = dm.getDuelParty(e.getPlayer());
        if (party == null) return;
        DuelArena room = dm.getArena(party);
        if (room == null) return;

        Location last = party.getLastLocation(e.getPlayer().getUniqueId());
        if (last == null) return;
        e.getPlayer().teleport(last);
        locations.put(e.getPlayer().getUniqueId(), last);
        party.removeFromRoom(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        DuelParty party = dm.getDuelParty(e.getEntity());
        if (party == null) return;
        DuelArena room = dm.getArena(party);
        if (room == null) return;

        Location last = party.getLastLocation(e.getEntity().getUniqueId());
        if (last == null) return;

        e.getEntity().spigot().respawn();
        e.getEntity().teleport(last);
        party.removeFromRoom(e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Location last = locations.remove(e.getPlayer().getUniqueId());
        if (last != null) {
            e.getPlayer().teleport(last);
        }
    }
}
