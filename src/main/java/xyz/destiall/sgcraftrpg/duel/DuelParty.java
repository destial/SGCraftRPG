package xyz.destiall.sgcraftrpg.duel;

import com.sucy.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class DuelParty {
    private final HashMap<UUID, Location> lastLocations;
    private final HashSet<UUID> inRoom;
    private final Party party;
    public DuelParty(Party party) {
        lastLocations = new HashMap<>();
        inRoom = new HashSet<>();
        this.party = party;
    }

    public Party getParty() {
        return party;
    }


    public void forEach(Consumer<Player> func) {
        if (party == null) return;
        for (String name : party.getMembers()) {
            Player player = Bukkit.getPlayer(name);
            if (player == null) continue;
            func.accept(player);
        }
    }

    public void forEachInRoom(Consumer<Player> func) {
        for (UUID uuid : inRoom) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            func.accept(player);
        }
    }

    public void teleportRoom(DuelArena room) {
        if (party == null) return;
        for (String name : party.getMembers()) {
            Player player = Bukkit.getPlayer(name);
            if (player == null) continue;
            inRoom.add(player.getUniqueId());
            player.teleport(room.getSpawn(this));
        }
    }

    public Location getLastLocation(UUID uuid) {
        return lastLocations.get(uuid);
    }

    public boolean contains(Player player) {
        if (party == null) return false;
        return party.getMembers().contains(player.getName());
    }

    public boolean isInRoom(Player player) {
        return inRoom.contains(player.getUniqueId());
    }

    public void saveLastLocation() {
        if (party == null) return;
        for (String name : party.getMembers()) {
            Player player = Bukkit.getPlayer(name);
            if (player == null) continue;
            lastLocations.put(player.getUniqueId(), player.getLocation());
        }
    }

    public boolean noOneInRoom() {
        return inRoom.isEmpty();
    }

    public boolean equals(DuelParty o) {
        if (this == o) return true;
        return Objects.equals(party, o.party);
    }

    public void removeFromRoom(UUID uuid) {
        inRoom.remove(uuid);
    }

    public void teleportBack() {
        SGCraftRPG.get().getLogger().info("Teleporting players " + inRoom);
        for (UUID uuid : inRoom) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.teleport(lastLocations.get(player.getUniqueId()));
        }
        inRoom.clear();
    }
}
