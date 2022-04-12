package xyz.destiall.sgcraftrpg.dungeon;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.UUID;

public class DungeonPlayer {
    private final Player player;
    private final DungeonParty party;

    public DungeonPlayer(Player player, DungeonParty party) {
        this.player = player;
        this.party = party;
    }

    public DungeonParty getParty() {
        return party;
    }

    public Player getPlayer() {
        return player;
    }

    public void sendInvite(DungeonInvite invite) {
        DungeonManager dm = SGCraftRPG.get().getDungeonManager();
        String dungeonName = invite.getRoom().getDungeon().getName();
        TextComponent component = new TextComponent(dm.getMessage("invite").replace("{dungeon}", dungeonName));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sgcraftrpg dungeon accept " + invite.getId()));
        TextComponent hover = new TextComponent(dm.getMessage("hover-invite").replace("{dungeon}", dungeonName));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));

        player.spigot().sendMessage(component);

        invite.getInvites().add(player.getUniqueId());
    }

    public UUID getId() {
        return player.getUniqueId();
    }
}
