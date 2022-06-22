package xyz.destiall.sgcraftrpg.duel;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.UUID;

public class DuelPlayer {
    private final Player player;
    private final DuelParty party;

    public DuelPlayer(Player player, DuelParty party) {
        this.player = player;
        this.party = party;
    }

    public DuelParty getParty() {
        return party;
    }

    public Player getPlayer() {
        return player;
    }

    public void sendInvite(DuelInvite invite) {
        DuelManager dm = SGCraftRPG.get().getDuelManager();
        String dungeonName = invite.getArena().getName();

        TextComponent component = new TextComponent(dm.getMessage("invite").get(0).replace("{arena}", dungeonName));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sgcraftrpg duel accept " + invite.getId()));

        TextComponent hover = new TextComponent(dm.getMessage("hover-invite").get(0).replace("{arena}", dungeonName));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));

        player.spigot().sendMessage(component);

        invite.getInvites().add(player.getUniqueId());
    }

    public UUID getId() {
        return player.getUniqueId();
    }
}
