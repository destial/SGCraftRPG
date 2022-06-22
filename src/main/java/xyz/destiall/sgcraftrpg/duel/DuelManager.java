package xyz.destiall.sgcraftrpg.duel;

import com.sucy.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DuelManager {
    private final ConcurrentHashMap<Integer, DuelInvite> invites;
    private final Set<DuelParty> parties;
    private final SGCraftRPG plugin;
    private final Set<DuelArena> arenas;
    private YamlConfiguration config;
    private int countdown;
    private long inviteExpiry;

    public DuelManager(SGCraftRPG plugin) {
        this.plugin = plugin;
        arenas = new HashSet<>();
        invites = new ConcurrentHashMap<>();
        parties = ConcurrentHashMap.newKeySet();
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (DuelArena arena : arenas) {
                arena.tick();
            }
            invites.values().removeIf(i -> {
                if (i.isExpired()) {
                    i.getParty1().forEach(p -> {
                        for (String msg : getMessage("invite-expired"))
                            p.sendMessage(msg);
                    });
                    i.getParty2().forEach(p -> {
                        for (String msg : getMessage("invite-expired"))
                            p.sendMessage(msg);
                    });
                    return true;
                }
                return false;
            });
        }, 0L, 20L);
    }

    public long getInviteExpiry() {
        return inviteExpiry;
    }

    public int getCountdown() {
        return countdown;
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

    private void load() {
        File file = new File(plugin.getDataFolder(), "duels.yml");
        try {
            boolean create = !file.exists();
            config = YamlConfiguration.loadConfiguration(file);
            if (create) {
                config.set("messages.invite", "&aYou have been invited to {arena}! &e[Click here to accept]");
                config.set("messages.hover-invite", "&aYou have been invited to {arena}!\n &eClick here");
                config.set("messages.accept-invite-party", "&a{name} has accepted the invite!");
                config.set("messages.already-accepted", "&cYou have already accepted the invite!");
                config.set("messages.not-invited", "&cYou are not invited!");
                config.set("messages.arena-full", "&cThis arena is currently full! Please wait for open slots!");
                config.set("messages.no-party", "&cYou do not have a party!");
                config.set("messages.already-in-duel", "&cYou already in a duel!");
                config.set("messages.other-already-in-duel", "&cThe other party already in a duel!");
                config.set("messages.start-timer", "&cStarting in {time}");
                config.set("messages.start-message", "&aYou have {time} to complete this duel. Good luck!");
                config.set("messages.time-remaining", "&aYou have {time} seconds left!");
                config.set("messages.time-ended", "&6Your time has ended!");
                config.set("messages.party-leader-only", "&cOnly the party leader can initiate a duel!");
                config.set("messages.already-invited", "&cYou have already sent an invite!");
                config.set("messages.invite-expired", "&cThe duel invite has expired!");

                config.set("options.countdown", 5);
                config.set("options.invite-expiry", 30);
                config.set("options.party-leader-only", false);

                config.set("arenas.arena1.spawn1", new Location(Bukkit.getWorlds().get(0), 0, 100, 0));
                config.set("arenas.arena1.spawn2", new Location(Bukkit.getWorlds().get(0), 0, 100, 0));
                config.set("arenas.arena1.player-cooldown", 300);
                config.set("arenas.arena1.timer", 30);
                config.set("arenas.arena1.cooldown", 180);

                config.save(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
            DuelArena arena = new DuelArena(this, key, config.getConfigurationSection("arenas." + key));
            arenas.add(arena);
        }

        countdown = config.getInt("options.countdown", 5);
        inviteExpiry = 1000L * config.getInt("options.invite-expiry", 30);
    }

    public DuelParty getDuelParty(Player player) {
        return parties.stream().filter(p -> p.isInRoom(player)).findFirst().orElse(null);
    }

    private int getRandomId() {
        int id = (int) (Math.random() * 100000);
        while (invites.containsKey(id)) {
            id = (int) (Math.random() * 100000);
        }
        return id;
    }

    public boolean invite(DuelParty party1, DuelParty party2, DuelArena arena) {
        for (DuelInvite invite : invites.values()) {
            if (invite.getParty1() == party1) return false;
            if (invite.getParty1() == party2) return false;
            if (invite.getParty2() == party2) return false;
            if (invite.getParty2() == party1) return false;
        }
        int id = getRandomId();
        DuelInvite invite = new DuelInvite(id , party1, party2, arena);
        invites.put(id, invite);
        party1.forEach(p -> new DuelPlayer(p, party1).sendInvite(invite));
        party2.forEach(p -> new DuelPlayer(p, party2).sendInvite(invite));
        return true;
    }

    public DuelArena getArena(Player player) {
        for (DuelArena arena : arenas) {
            if (!arena.isInUse()) continue;
            if (arena.getParty1().isInRoom(player)) return arena;
            if (arena.getParty2().isInRoom(player)) return arena;
        }
        return null;
    }

    public DuelArena getArena(String name) {
        return arenas.stream().filter(a -> a.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public DuelArena getEmptyArena() {
        for (DuelArena arena : arenas) {
            if (!arena.isInUse()) return arena;
        }
        return null;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public DuelArena getArena(DuelParty party) {
        for (DuelArena arena : arenas) {
            if (!arena.isInUse()) continue;
            if (arena.getParty1() == party || arena.getParty2() == party)
                return arena;
        }
        return null;
    }

    public Set<DuelArena> getArenas() {
        return arenas;
    }

    public DuelInvite getInvite(int id) {
        return invites.get(id);
    }

    public DuelInvite getInvite(UUID uuid) {
        return invites.values().stream().filter(i -> i.getInvites().contains(uuid)).findFirst().orElse(null);
    }

    public void removeInvite(int id) {
        invites.remove(id);
    }

    public DuelParty addParty(Party party) {
        DuelParty dp = parties.stream().filter(p -> p.getParty() == party).findFirst().orElse(null);
        if (dp == null) {
            dp = new DuelParty(party);
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

    public void reload() {
        disable();
        load();
    }

    public void disable() {
        for (DuelArena arena : arenas) {
            if (arena.isInUse()) {
                arena.end(0);
            }
        }
        arenas.clear();
        invites.clear();
        parties.clear();
    }
}
