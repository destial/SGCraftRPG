package xyz.destiall.sgcraftrpg.dungeon;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class DungeonInvite {
    private final DungeonParty party;
    private final DungeonRoom room;
    private final Set<UUID> accepted;
    private final Set<UUID> invited;
    private final int id;
    private final long expiry;

    public DungeonInvite(int id, DungeonParty party, DungeonRoom room) {
        accepted = new HashSet<>();
        invited = new HashSet<>();
        this.id = id;
        this.party = party;
        this.room = room;
        expiry = System.currentTimeMillis() + room.getDungeon().getManager().getInviteExpiry();
    }

    public boolean isExpired() {
        return expiry < System.currentTimeMillis() && !isReady();
    }

    public Set<UUID> getInvites() {
        return invited;
    }

    public int getId() {
        return id;
    }

    public DungeonRoom getRoom() {
        return room;
    }

    public DungeonParty getParty() {
        return party;
    }

    public Result accept(DungeonPlayer player) {
        if (!invited.contains(player.getId())) return Result.NOT_INVITED;
        if (isReady()) return Result.ALREADY_IN_ROOM;
        if (room.getDungeon().isOnCooldown(player.getId())) return Result.COOLDOWN;
        return accepted.add(player.getId()) ? Result.ACCEPTED : Result.ALREADY_ACCEPTED;
    }

    public boolean isReady() {
        for (UUID one : invited) {
            if (!accepted.contains(one)) {
                return false;
            }
        }
        return true;
    }

    public void countdown(Consumer<DungeonParty> func) {
        new BukkitRunnable() {
            private int count = room.getDungeon().getManager().getCountdown();
            @Override
            public void run() {
                if (count < 0) {
                    cancel();
                    party.saveLastLocation();
                    func.accept(party);
                    return;
                }
                BaseComponent[] component = new ComponentBuilder(room.getDungeon().getManager().getMessage("start-timer").replace("{time}", ""+count)).create();
                invited.forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) return;
                    p.spigot().sendMessage(component);
                    p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.5f, 1);
                });
                count--;

            }
        }.runTaskTimer(room.getDungeon().getManager().getPlugin(), 0L, 20L);
    }

    public enum Result {
        ACCEPTED,
        ALREADY_ACCEPTED,
        NOT_INVITED,
        ALREADY_IN_ROOM,
        COOLDOWN,
    }
}