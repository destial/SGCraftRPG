package xyz.destiall.sgcraftrpg.dungeon;

import org.bukkit.Location;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

public class DungeonRoom {
    private final Location spawn;
    private final Dungeon dungeon;
    private DungeonParty party;
    private DungeonInvite invite;
    private long endTime;
    private long cooldownTime;
    private long closeTime;

    public DungeonRoom(Dungeon dungeon, Location spawn) {
        this.dungeon = dungeon;
        this.spawn = spawn;
        endTime = 0;
        cooldownTime = 0;
        closeTime = 0;
    }

    public void tick() {
        if (isEnding()) {
            close();
            return;
        }
        if (isInUse()) {
            if (isEmptyRoom() || hasTimerEnded()) {
                end(0);
            }
        }
    }

    public boolean isEmptyRoom() {
        return endTime != 0 && party != null && party.noOneInRoom();
    }

    public void setParty(DungeonParty party) {
        this.party = party;
    }

    public DungeonParty getParty() {
        return party;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public boolean isInUse() {
        return party != null;
    }

    public boolean isReadyToBeUsed() {
        return cooldownTime < System.currentTimeMillis();
    }

    public boolean isEnding() {
        return closeTime != 0 && closeTime <= System.currentTimeMillis();
    }

    public boolean hasTimerEnded() {
        return endTime != 0 && endTime < System.currentTimeMillis();
    }

    public void start(DungeonInvite invite) {
        if (party == null) return;
        this.invite = invite;

        SGCraftRPG.get().getLogger().info("Starting room " + dungeon.getName() + " with id " + invite.getId());
        invite.countdown((party) -> {
            party.teleportRoom(this);
            endTime = System.currentTimeMillis() + dungeon.getRoomTimer();
            party.forEachInRoom((p) -> {
                for (String msg : dungeon.getManager().getMessage("start-message"))
                    p.sendMessage(msg.replace("{time}", ""+(dungeon.getRoomTimer() / 1000L)));
            });
        });
    }

    public Location getSpawn() {
        return spawn;
    }

    public void end(int delay) {
        if (party == null) return;
        if (delay <= 0) {
            close();
            return;
        }
        SGCraftRPG.get().getLogger().info("Ending room " + dungeon.getName() + " with id " + invite.getId() + " in " + delay + " seconds");
        closeTime = System.currentTimeMillis() + (1000L * delay);
    }

    private void close() {
        SGCraftRPG.get().getLogger().info("Ending room " + dungeon.getName() + " with id " + invite.getId());
        if (hasTimerEnded()) {
            party.forEachInRoom(p -> {
                for (String msg : dungeon.getManager().getMessage("time-ended"))
                    p.sendMessage(msg);
            });
        }
        dungeon.putOnCooldown(party);
        dungeon.getManager().removeInvite(invite.getId());
        party.teleportBack();
        party = null;
        invite = null;
        endTime = 0;
        closeTime = 0;
        cooldownTime = System.currentTimeMillis() + dungeon.getRoomCooldown();
    }
}
