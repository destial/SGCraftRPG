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

    public DungeonRoom(Dungeon dungeon, Location spawn) {
        this.dungeon = dungeon;
        this.spawn = spawn;
        endTime = 0;
        cooldownTime = 0;
    }

    public void tick() {
        if (isInUse() && hasTimerEnded()) {
            end();
        }
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

    public boolean hasTimerEnded() {
        return endTime != 0 && endTime < System.currentTimeMillis();
    }

    public void start(DungeonInvite invite) {
        if (party == null) return;
        this.invite = invite;

        invite.countdown((party) -> {
            party.teleportRoom(this);
            endTime = System.currentTimeMillis() + dungeon.getRoomTimer();
            party.forEachInRoom((p) -> p.sendMessage(dungeon.getManager().getMessage("start-message").replace("{time}", ""+(dungeon.getRoomTimer() / 1000L))));
        });
    }

    public Location getSpawn() {
        return spawn;
    }

    public void end() {
        if (party == null) return;
        SGCraftRPG.get().getLogger().info("Ending room " + dungeon.getName());
        dungeon.putOnCooldown(party);
        party.forEachInRoom(p -> p.sendMessage(dungeon.getManager().getMessage("time-ended")));
        party.teleportBack();

        dungeon.getManager().removeInvite(invite.getId());
        party = null;
        invite = null;
        endTime = 0;
        cooldownTime = System.currentTimeMillis() + dungeon.getRoomCooldown();
    }
}
