package xyz.destiall.sgcraftrpg.duel;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

public class DuelArena {
    private final DuelManager dm;
    private final String name;
    private final Location spawn1;
    private final Location spawn2;
    private final long roomTimer;
    private DuelParty party1;
    private DuelParty party2;
    private DuelInvite invite;
    private long endTime;
    private long closeTime;

    public DuelArena(DuelManager dm, String name, ConfigurationSection section) {
        this.dm = dm;
        this.name = name;
        this.spawn1 = section.getLocation("spawn1");
        this.spawn2 = section.getLocation("spawn2");
        endTime = 0;
        closeTime = 0;
        roomTimer = 1000L * section.getInt("timer", 120);
    }

    public long getRoomTimer() {
        return roomTimer;
    }

    public DuelManager getManager() {
        return dm;
    }

    public void tick() {
        if (isEnding()) {
            close();
            return;
        }
        if (isInUse()) {
            if (isEmptyRoom() || hasTimerEnded()) {
                end(0);
                return;
            }
            long remaining = endTime - System.currentTimeMillis();
            int seconds = (int) (remaining / 1000L);
            if (seconds % 20 == 0 || seconds <= 5) {
                party1.forEachInRoom(p -> {
                    for (String msg : getManager().getMessage("time-remaining")) {
                        p.sendMessage(msg.replace("{time}", ""+seconds));
                    }
                });
                party2.forEachInRoom(p -> {
                    for (String msg : getManager().getMessage("time-remaining")) {
                        p.sendMessage(msg.replace("{time}", ""+seconds));
                    }
                });
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean isEmptyRoom() {
        return endTime != 0 && party1 != null && party2 != null;
    }

    public void setParty1(DuelParty party) {
        this.party1 = party;
    }

    public void setParty2(DuelParty party) {
        this.party2 = party;
    }

    public DuelParty getParty1() {
        return party1;
    }

    public DuelParty getParty2() {
        return party2;
    }

    public boolean isInUse() {
        return party1 != null && party2 != null;
    }

    public boolean isEnding() {
        return closeTime != 0 && closeTime <= System.currentTimeMillis();
    }

    public boolean hasTimerEnded() {
        return endTime != 0 && endTime < System.currentTimeMillis();
    }

    public void start(DuelInvite invite) {
        if (party1 != null || party2 != null) return;
        this.invite = invite;
        party1 = invite.getParty1();
        party2 = invite.getParty2();

        SGCraftRPG.get().getLogger().info("Starting duel arena " + getName() + " with id " + invite.getId());
        invite.countdown((party) -> {
            party.teleportRoom(this);
            endTime = System.currentTimeMillis() + getRoomTimer();
            party.forEachInRoom((p) -> {
                for (String msg : getManager().getMessage("start-message"))
                    p.sendMessage(msg.replace("{time}", ""+(getRoomTimer() / 1000L)));
            });
        });
    }

    public Location getSpawn(DuelParty party) {
        return party.equals(party1) ? spawn1 : spawn2;
    }

    public void end(int delay) {
        if (party1 == null || party2 == null) return;
        if (delay <= 0) {
            close();
            return;
        }
        SGCraftRPG.get().getLogger().info("Ending duel arena " + getName() + " with id " + invite.getId() + " in " + delay + " seconds");
        closeTime = System.currentTimeMillis() + (1000L * delay);
    }

    private void close() {
        SGCraftRPG.get().getLogger().info("Ending duel arena " + getName() + " with id " + invite.getId());
        if (hasTimerEnded()) {
            party1.forEachInRoom(p -> {
                for (String msg : dm.getMessage("time-ended"))
                    p.sendMessage(msg);
            });
            party2.forEachInRoom(p -> {
                for (String msg : dm.getMessage("time-ended"))
                    p.sendMessage(msg);
            });
        }
        SGCraftRPG.get().getLogger().info("In party 1: " + party1.getParty().getMembers());
        SGCraftRPG.get().getLogger().info("In party 2: " + party2.getParty().getMembers());
        dm.removeInvite(invite.getId());
        party1.teleportBack();
        party2.teleportBack();
        party1 = null;
        party2 = null;
        invite = null;
        endTime = 0;
        closeTime = 0;
    }
}
