package xyz.destiall.sgcraftrpg.dungeon;

import com.sucy.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DungeonManager {
    private final ConcurrentHashMap<Integer, DungeonInvite> invites;
    private final Set<DungeonParty> parties;
    private final SGCraftRPG plugin;
    private final Set<Dungeon> dungeons;
    private YamlConfiguration config;
    private int countdown;
    private long inviteExpiry;
    private int deathCooldownMultiplier;

    public DungeonManager(SGCraftRPG plugin) {
        this.plugin = plugin;
        dungeons = new HashSet<>();
        invites = new ConcurrentHashMap<>();
        parties = ConcurrentHashMap.newKeySet();
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Dungeon dungeon : dungeons) {
                dungeon.tick();
            }
            invites.values().removeIf(i -> {
                if (i.isExpired()) {
                    i.getParty().forEach(p -> {
                        for (String msg : getMessage("invite-expired"))
                            p.sendMessage(msg);
                    });
                    return true;
                }
                return false;
            });
        }, 0L, 20L);
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "dungeons.yml");
        try {
            boolean create = !file.exists();
            config = YamlConfiguration.loadConfiguration(file);
            if (create) {
                config.set("messages.invite", "&aYou have been invited to {dungeon}! &e[Click here to accept]");
                config.set("messages.hover-invite", "&aYou have been invited to {dungeon}!\n &eClick here");
                config.set("messages.accept-invite-party", "&a{name} has accepted the invite!");
                config.set("messages.already-accepted", "&cYou have already accepted the invite!");
                config.set("messages.not-invited", "&cYou are not invited!");
                config.set("messages.dungeon-full", "&cThis dungeon is currently full! Please wait for open slots!");
                config.set("messages.room-full", "&cThis room is currently occupied! Please send another invite!");
                config.set("messages.no-party", "&cYou do not have a party!");
                config.set("messages.already-in-room", "&cYou already in a dungeon!");
                config.set("messages.start-timer", "&cStarting in {time}");
                config.set("messages.start-message", "&aYou have {time} to complete this dungeon. Good luck!");
                config.set("messages.time-remaining", "&aYou have {time} seconds left!");
                config.set("messages.time-ended", "&6Your time has ended!");
                config.set("messages.dungeon-cooldown", "&cYou have just recently entered this dungeon! Wait a while...");
                config.set("messages.party-leader-only", "&cOnly the party leader can initiate a dungeon!");
                config.set("messages.already-invited", "&cYou have already sent an invite!");
                config.set("messages.invite-expired", "&cThe dungeon invite has expired!");
                config.set("messages.dungeon-cooldown-end", "&aYou can now enter {dungeon} again.");
                config.set("messages.dungeon-death", "&cYou have died! You have to wait twice the cooldown length to attempt this dungeon again!");
                config.set("messages.not-high-level", "&cYou are not at a high enough level! You need to be level {level} to enter this dungeon!");

                config.set("options.countdown", 5);
                config.set("options.invite-expiry", 30);
                config.set("options.party-leader-only", false);
                config.set("death-cooldown-multiplier", 2);

                config.set("dungeons.dungeon1.rooms", Arrays.asList(new Location(Bukkit.getWorlds().get(0), 0, 100, 0), new Location(Bukkit.getWorlds().get(0), 100, 100, 100)));
                config.set("dungeons.dungeon1.player-cooldown", 300);
                config.set("dungeons.dungeon1.room-timer", 30);
                config.set("dungeons.dungeon1.room-cooldown", 180);
                config.set("dungeons.dungeon1.level-requirement", 1);

                config.save(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String key : config.getConfigurationSection("dungeons").getKeys(false)) {
            Dungeon dungeon = new Dungeon(this, key, config.getConfigurationSection("dungeons." + key));
            dungeons.add(dungeon);
        }

        countdown = config.getInt("options.countdown", 5);
        inviteExpiry = 1000L * config.getInt("options.invite-expiry", 30);
        deathCooldownMultiplier = config.getInt("options.death-cooldown-multiplier", 2);
    }

    public void reload() {
        disable();
        load();
    }

    public void disable() {
        for (Dungeon dungeon : dungeons) {
            dungeon.clear();
        }
        dungeons.clear();
        invites.clear();
        parties.clear();
    }

    public boolean invite(DungeonParty party, DungeonRoom room) {
        for (DungeonInvite invite : invites.values()) {
            if (invite.getParty() == party) return false;
        }
        int id = getRandomId();
        DungeonInvite invite = new DungeonInvite(id , party, room);
        invites.put(id, invite);
        party.forEach(p -> new DungeonPlayer(p, party).sendInvite(invite));
        return true;
    }

    public Set<Dungeon> getDungeons() {
        return dungeons;
    }

    public DungeonInvite getInvite(int id) {
        return invites.get(id);
    }

    public DungeonInvite getInvite(UUID uuid) {
        return invites.values().stream().filter(i -> i.getInvites().contains(uuid)).findFirst().orElse(null);
    }

    public void removeInvite(int id) {
        invites.remove(id);
    }

    public Dungeon getDungeon(String name) {
        return dungeons.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private int getRandomId() {
        int id = (int) (Math.random() * 100000);
        while (invites.containsKey(id)) {
            id = (int) (Math.random() * 100000);
        }
        return id;
    }

    public DungeonParty addParty(Party party) {
        DungeonParty dp = parties.stream().filter(p -> p.getParty() == party).findFirst().orElse(null);
        if (dp == null) {
            dp = new DungeonParty(party);
            parties.add(dp);
        }
        return dp;
    }

    public void removeParty(Party party) {
        parties.removeIf(p -> p.getParty() == party);
    }

    public SGCraftRPG getPlugin() {
        return plugin;
    }

    public List<String> getMessage(String key) {
        Object object = config.get("messages." + key);
        if (object instanceof String) {
            return Collections.singletonList(Formatter.color((String) object));
        } else if (object instanceof List) {
            List<String> stringList = (List<String>) object;
            return stringList.stream().map(Formatter::color).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public int getCountdown() {
        return countdown;
    }

    public long getInviteExpiry() {
        return inviteExpiry;
    }

    public int getDeathCooldownMultiplier() {
        return deathCooldownMultiplier;
    }

    public Set<Integer> getInvites() {
        return invites.keySet();
    }

    public DungeonParty getDungeonParty(Player player) {
        return parties.stream().filter(p -> p.contains(player)).findFirst().orElse(null);
    }

    public DungeonRoom getDungeonRoom(DungeonParty party) {
        for (Dungeon dungeon : dungeons) {
            DungeonRoom room = dungeon.getRooms().stream().filter(r -> r.isInUse() && r.getParty() == party).findFirst().orElse(null);
            if (room != null) return room;
        }
        return null;
    }

    public DungeonRoom getDungeonRoom(Player player) {
        for (Dungeon dungeon : dungeons) {
            DungeonRoom room = dungeon.getRooms().stream().filter(r -> r.isInUse() && r.getParty().isInRoom(player)).findFirst().orElse(null);
            if (room != null) return room;
        }
        return null;
    }
}
