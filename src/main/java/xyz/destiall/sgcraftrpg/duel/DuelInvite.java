package xyz.destiall.sgcraftrpg.duel;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class DuelInvite {
    private final DuelParty party1;
    private final DuelParty party2;
    private final DuelArena arena;
    private final HashSet<UUID> accepted;
    private final HashSet<UUID> invited;
    private final int id;
    private final long expiry;

    public DuelInvite(int id, DuelParty party1, DuelParty party2, DuelArena arena) {
        accepted = new HashSet<>();
        invited = new HashSet<>();
        this.id = id;
        this.party1 = party1;
        this.party2 = party2;
        this.arena = arena;
        expiry = System.currentTimeMillis() + arena.getManager().getInviteExpiry();
    }

    public boolean isExpired() {
        return expiry < System.currentTimeMillis() && !isReady();
    }

    public boolean isReady() {
        for (UUID one : invited) {
            if (!accepted.contains(one)) {
                return false;
            }
        }
        return true;
    }

    public HashSet<UUID> getInvites() {
        return invited;
    }

    public int getId() {
        return id;
    }

    public DuelArena getArena() {
        return arena;
    }

    public DuelParty getParty1() {
        return party1;
    }

    public DuelParty getParty2() {
        return party2;
    }

    public Result accept(Player player) {
        if (!invited.contains(player.getUniqueId())) return Result.NOT_INVITED;
        if (isReady()) return Result.ALREADY_IN_ARENA;
        return accepted.add(player.getUniqueId()) ? Result.ACCEPTED : Result.ALREADY_ACCEPTED;
    }

    public void countdown(Consumer<DuelParty> func) {
        new BukkitRunnable() {
            private int count = arena.getManager().getCountdown();
            @Override
            public void run() {
                if (count < 0) {
                    cancel();
                    party1.saveLastLocation();
                    party2.saveLastLocation();
                    func.accept(party1);
                    func.accept(party2);
                    return;
                }

                for (String msg : arena.getManager().getMessage("start-timer")) {
                    BaseComponent[] component = new ComponentBuilder(Formatter.variables(msg, "{time}", ""+count)).create();
                    invited.forEach(uuid -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p == null) return;
                        p.spigot().sendMessage(component);
                        p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.5f, 1);
                    });
                }

                count--;

            }
        }.runTaskTimer(arena.getManager().getPlugin(), 0L, 20L);
    }

    public enum Result {
        ACCEPTED,
        ALREADY_ACCEPTED,
        NOT_INVITED,
        ALREADY_IN_ARENA,
        COOLDOWN,
    }
}
