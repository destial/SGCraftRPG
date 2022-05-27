package xyz.destiall.sgcraftrpg;

import com.sucy.party.Parties;
import com.sucy.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.sgcraftrpg.dungeon.Dungeon;
import xyz.destiall.sgcraftrpg.dungeon.DungeonInvite;
import xyz.destiall.sgcraftrpg.dungeon.DungeonManager;
import xyz.destiall.sgcraftrpg.dungeon.DungeonParty;
import xyz.destiall.sgcraftrpg.dungeon.DungeonPlayer;
import xyz.destiall.sgcraftrpg.dungeon.DungeonRoom;
import xyz.destiall.sgcraftrpg.utils.Formatter;
import xyz.destiall.sgcraftrpg.utils.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SGCraftRPGCommand implements CommandExecutor, TabExecutor {
    private final SGCraftRPG plugin;
    private final Parties parties;
    public SGCraftRPGCommand(SGCraftRPG plugin) {
        this.plugin = plugin;
        parties = Parties.getPlugin(Parties.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args != null && args.length != 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission(Permissions.ADMIN)) {
                    plugin.reloadConfig();
                    sender.sendMessage(Formatter.color("&aReloaded SGCraftRPG configuration..."));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("dungeon")) {
                DungeonManager dm = plugin.getDungeonManager();
                if (args.length > 2 && args[1].equalsIgnoreCase("end")) {
                    if (sender.hasPermission(Permissions.ADMIN)) {
                        DungeonInvite invite;
                        try {
                            int id = Integer.parseInt(args[2]);
                            invite = dm.getInvite(id);
                        } catch (NumberFormatException e) {
                            Player player = Bukkit.getPlayer(args[2]);
                            if (player == null) return false;
                            invite = dm.getInvite(player.getUniqueId());
                        }
                        if (invite == null) {
                            sender.sendMessage(Formatter.color("&cInvite does not exist!"));
                            return false;
                        }
                        int delay = 0;
                        if (args.length > 3) {
                            try {
                                delay = Integer.parseInt(args[3]);
                            } catch (Exception ignored) {}
                        }
                        sender.sendMessage(Formatter.color("&aEnding room " + invite.getId()));
                        invite.getRoom().end(delay);
                    } else {
                        sender.sendMessage(Formatter.color("&cYou don't have permission!"));
                    }
                    return false;
                }

                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("start")) {
                        if (sender instanceof Player) {
                            sender.sendMessage(Formatter.color("&cYou must be from console to run this command!"));
                            return false;
                        }
                        if (args.length < 3) {
                            sender.sendMessage(Formatter.color("&4Usage: /sgcraftrpg dungeon start [dungeon] [player]"));
                            return false;
                        }
                        Player player = Bukkit.getPlayer(args[3]);
                        if (player == null) {
                            sender.sendMessage(Formatter.color("&4Player " + args[3] + " not found!"));
                            return false;
                        }

                        Party party = parties.getParty(player);
                        DungeonRoom existing = dm.getDungeonRoom(player);
                        if (existing != null) {
                            for (String msg : dm.getMessage("already-in-room"))
                                player.sendMessage(msg);
                            return false;
                        }
                        if (!sender.hasPermission(Permissions.SOLO)) {
                            if (party == null) {
                                for (String msg : dm.getMessage("no-party"))
                                        player.sendMessage(msg);
                                return false;
                            }
                        } else {
                            if (party == null) {
                                party = new Party(parties, player);
                                parties.addParty(party);
                            }
                        }

                        String dungeonName = args[2].toLowerCase();
                        Dungeon dungeon = dm.getDungeon(dungeonName);
                        if (dungeon == null) {
                            sender.sendMessage(Formatter.color("&cThat is not a dungeon name!"));
                            return false;
                        }

                        if (dungeon.isOnCooldown(player.getUniqueId())) {
                            for (String msg : dm.getMessage("dungeon-cooldown"))
                                player.sendMessage(msg);
                            return false;
                        }

                        if (plugin.getConfig().getBoolean("options.party-leader-only") && !party.isLeader(player)) {
                            for (String msg : dm.getMessage("party-leader-only"))
                                player.sendMessage(msg);
                            return false;
                        }

                        DungeonRoom room = dungeon.getAvailableRoom();

                        if (room == null) {
                            for (String msg : dm.getMessage("dungeon-full"))
                                player.sendMessage(msg);
                            return false;
                        }

                        DungeonParty dungeonParty = dm.addParty(party);
                        if (!dm.invite(dungeonParty, room)) {
                            for (String msg : dm.getMessage("already-invited"))
                                player.sendMessage(msg);
                        }
                        return false;
                    } else if (args[1].equalsIgnoreCase("accept")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Formatter.color("&cYou must be a player to run this command!"));
                            return false;
                        }
                        Player player = (Player) sender;
                        DungeonRoom existing = dm.getDungeonRoom(player);
                        if (existing != null) {
                            for (String msg : dm.getMessage("already-in-room"))
                                sender.sendMessage(msg);
                            return false;
                        }
                        if (args.length > 2) {
                            try {
                                int id = Integer.parseInt(args[2]);
                                DungeonInvite invite = dm.getInvite(id);
                                if (invite == null) {
                                    sender.sendMessage(Formatter.color("&cInvalid invite!"));
                                    return false;
                                }
                                Party party = parties.getParty(player);
                                DungeonParty dungeonParty = dm.addParty(party);
                                DungeonPlayer dungeonPlayer = new DungeonPlayer(player, dungeonParty);
                                DungeonInvite.Result result = invite.accept(dungeonPlayer);
                                if (result == DungeonInvite.Result.ACCEPTED) {
                                    dungeonParty.forEach(p -> {
                                        for (String msg : dm.getMessage("accept-invite-party"))
                                            p.sendMessage(msg.replace("{name}", player.getName()));
                                    });

                                    if (invite.isReady()) {
                                        DungeonRoom room = invite.getRoom();
                                        if (room.isInUse() || !room.isReadyToBeUsed()) {
                                            for (String msg : dm.getMessage("room-full"))
                                                dungeonParty.forEach(p -> p.sendMessage(msg.replace("{dungeon}", invite.getRoom().getDungeon().getName())));
                                            dm.removeInvite(id);
                                            return false;
                                        }
                                        room.setParty(dungeonParty);
                                        room.start(invite);
                                        return false;
                                    }
                                } else if (result == DungeonInvite.Result.ALREADY_ACCEPTED) {
                                    for (String msg : dm.getMessage("already-accepted"))
                                        sender.sendMessage(msg);
                                } else if (result == DungeonInvite.Result.NOT_INVITED) {
                                    for (String msg : dm.getMessage("not-invited"))
                                        sender.sendMessage(msg);
                                } else if (result == DungeonInvite.Result.ALREADY_IN_ROOM) {
                                    for (String msg : dm.getMessage("already-in-room"))
                                        sender.sendMessage(msg);
                                } else {
                                    for (String msg : dm.getMessage("dungeon-cooldown"))
                                        sender.sendMessage(msg);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    sender.sendMessage(Formatter.color("&cUsage: /sgcraftrpg dungeon [start/end] [name]"));
                    return false;
                }
            }
            return false;
        }
        sender.sendMessage(Formatter.color("&cYou do not have permission!"));
        Bukkit.getConsoleSender().sendMessage(Formatter.sender("&c{name} does not have permission: " + Permissions.ADMIN.getName(), sender));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (sender.hasPermission(Permissions.ADMIN)) {
            if (args.length == 1) return Stream.of("reload", "dungeon").filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());

            if (args.length == 2 && args[0].equalsIgnoreCase("dungeon")) {
                return Stream.of("start", "end", "accept").filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("dungeon")) {
                if (args[1].equalsIgnoreCase("start")) {
                    return plugin.getDungeonManager().getDungeons().stream().map(Dungeon::getName).filter(a -> a.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
                } else if (args[1].equalsIgnoreCase("end")) {
                    return plugin.getDungeonManager().getInvites().stream().map(i -> ""+i).filter(a -> a.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
