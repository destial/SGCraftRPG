package xyz.destiall.sgcraftrpg.dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Dungeon {
    private final DungeonManager dm;
    private final ConcurrentHashMap<UUID, Long> playerCooldown;
    private final ArrayList<DungeonRoom> rooms;
    private final String name;
    private final long pCooldown;
    private final long roomTimer;
    private final long roomCooldown;

    public Dungeon(DungeonManager dm, String name, ConfigurationSection section) {
        this.dm = dm;
        this.name = name;
        playerCooldown = new ConcurrentHashMap<>();
        rooms = new ArrayList<>();
        List<Location> locations = (List<Location>) section.getList("rooms", new ArrayList<>());
        for (Location location : locations) {
            DungeonRoom room = new DungeonRoom(this, location);
            rooms.add(room);
        }
        pCooldown = 1000L * section.getInt("player-cooldown");
        roomTimer = 1000L * section.getInt("room-timer");
        roomCooldown = 1000L * section.getInt("room-cooldown");
    }

    public String getName() {
        return name;
    }

    public DungeonRoom getAvailableRoom() {
        return rooms.stream().filter(r -> !r.isInUse() && r.isReadyToBeUsed()).findFirst().orElse(null);
    }

    public List<DungeonRoom> getRooms() {
        return rooms;
    }

    public void putOnCooldown(DungeonParty party) {
        long now = System.currentTimeMillis();
        party.forEachInRoom(p -> playerCooldown.put(p.getUniqueId(), now + pCooldown));
    }

    public void tick() {
        for (DungeonRoom room : rooms) {
            room.tick();
        }

        List<UUID> remove = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : playerCooldown.entrySet()) {
            if (entry.getValue() < now) remove.add(entry.getKey());
        }
        for (UUID uuid : remove) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(dm.getMessage("dungeon-cooldown-end").replace("{dungeon}", name));
            }
            playerCooldown.remove(uuid);
        }
    }

    public boolean isOnCooldown(UUID uuid) {
        return playerCooldown.containsKey(uuid);
    }

    public long getRoomTimer() {
        return roomTimer;
    }

    public long getRoomCooldown() {
        return roomCooldown;
    }

    public void clear() {
        for (DungeonRoom room : rooms) {
            if (room.isInUse() || !room.hasTimerEnded() || !room.isReadyToBeUsed()) room.end();
        }
        rooms.clear();
    }

    public DungeonManager getManager() {
        return dm;
    }
}