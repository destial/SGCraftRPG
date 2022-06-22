package xyz.destiall.sgcraftrpg.dungeon;

import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

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
    private final int levelRequirement;

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
        pCooldown = 1000L * section.getInt("player-cooldown", 60);
        roomTimer = 1000L * section.getInt("room-timer", 120);
        roomCooldown = 1000L * section.getInt("room-cooldown", 60);
        levelRequirement = section.getInt("level-requirement", 1);
    }

    public int getLevelRequirement() {
        return levelRequirement;
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

    public void putOnCooldown(Player player, int multiplier) {
        long cooldownTime = getCooldownTime(player);
        playerCooldown.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownTime * multiplier));
    }

    public void putOnCooldown(DungeonParty party) {
        long now = System.currentTimeMillis();
        party.forEachInRoom(p -> {
            long cooldownTime = getCooldownTime(p);
            playerCooldown.put(p.getUniqueId(), now + cooldownTime);
        });
    }

    public long getCooldownTime(Player player) {
        String node = "sgcraftrpg.dungeon." + getName().toLowerCase() + ".";
        long cd = pCooldown;
        User user = SGCraftRPG.get().getLuckPermsProvider().getUserManager().getUser(player.getUniqueId());
        if (user == null) return cd;

        QueryOptions options = QueryOptions.builder(QueryMode.CONTEXTUAL).context(user.getQueryOptions().context()).build();
        Map<String, Boolean> data = user.getCachedData().getPermissionData(options).getPermissionMap();
        int i = (int) (pCooldown / 1000);
        for (Map.Entry<String, Boolean> entry : data.entrySet()) {
            if (entry.getValue() && entry.getKey().startsWith(node)) {
                try {
                    int parse = Integer.parseInt(entry.getKey().substring(node.length()));
                    if (parse < i) {
                        i = parse;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        cd = i * 1000L;
        return cd;
    }

    public void tick() {
        for (DungeonRoom room : rooms) {
            room.tick();
        }

        long now = System.currentTimeMillis();
        playerCooldown.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    for (String msg : dm.getMessage("dungeon-cooldown-end")) {
                        player.sendMessage(msg.replace("{dungeon}", name));
                    }
                }
                return true;
            }
            return false;
        });
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
            if (room.isInUse() || !room.hasTimerEnded() || !room.isReadyToBeUsed())
                room.end(0);
        }
        for (UUID uuid : playerCooldown.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                for (String msg : dm.getMessage("dungeon-cooldown-end")) {
                    player.sendMessage(msg.replace("{dungeon}", name));
                }
            }
        }
        playerCooldown.clear();
        rooms.clear();
    }

    public DungeonManager getManager() {
        return dm;
    }
}
