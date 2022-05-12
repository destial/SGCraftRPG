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
                if (sender.hasPermission(Permissions.ADMIN)) {
                    if (args.length > 2 && args[1].equalsIgnoreCase("end")) {
                        DungeonInvite invite;
                        try {
                            int id = Integer.parseInt(args[2]);
                            invite = dm.getInvite(id);
                        } catch (Exception e) {
                            Player player = Bukkit.getPlayer(args[2]);
                            if (player == null) return false;
                            invite = dm.getInvite(player.getUniqueId());
                        }
                        if (invite == null) {
                            sender.sendMessage(Formatter.color("&cInvite does not exist!"));
                            return false;
                        }
                        invite.getRoom().end();
                        return false;
                    }
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Formatter.color("&cYou must be a player to use this command!"));
                    return false;
                }
                Player player = (Player) sender;
                if (args.length > 1) {
                    Party party = parties.getParty(player);
                    if (args[1].equalsIgnoreCase("start")) {
                        DungeonRoom existing = dm.getDungeonRoom(player);
                        if (existing != null) {
                            player.sendMessage(dm.getMessage("already-in-room"));
                            return false;
                        }
                        if (args.length > 2) {
                            if (!sender.hasPermission(Permissions.SOLO)) {
                                if (party == null) {
                                    player.sendMessage(dm.getMessage("no-party"));
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
                                player.sendMessage(Formatter.color("&cThat is not a dungeon name!"));
                                return false;
                            }

                            if (dungeon.isOnCooldown(player.getUniqueId())) {
                                player.sendMessage(dm.getMessage("dungeon-cooldown"));
                                return false;
                            }

                            if (plugin.getConfig().getBoolean("options.party-leader-only") && !party.isLeader(player)) {
                                sender.sendMessage(dm.getMessage("party-leader-only"));
                                return false;
                            }

                            DungeonRoom room = dungeon.getAvailableRoom();

                            if (room == null) {
                                sender.sendMessage(dm.getMessage("dungeon-full"));
                                return false;
                            }

                            DungeonParty dungeonParty = dm.addParty(party);
                            if (!dm.invite(dungeonParty, room)) {
                                sender.sendMessage(dm.getMessage("already-invited"));
                            }
                            return false;
                        }
                        sender.sendMessage(Formatter.color("&cUsage: /sgcraftrpg dungeon start [name]"));
                        return false;
                    } else if (args[1].equalsIgnoreCase("accept")) {
                        DungeonRoom existing = dm.getDungeonRoom(player);
                        if (existing != null) {
                            sender.sendMessage(dm.getMessage("already-in-room"));
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
                                DungeonParty dungeonParty = dm.addParty(party);
                                DungeonPlayer dungeonPlayer = new DungeonPlayer(player, dungeonParty);
                                DungeonInvite.Result result = invite.accept(dungeonPlayer);
                                if (result == DungeonInvite.Result.ACCEPTED) {
                                    dungeonParty.forEach(p -> p.sendMessage(dm.getMessage("accept-invite-party").replace("{name}", player.getName())));

                                    if (invite.isReady()) {
                                        DungeonRoom room = invite.getRoom();
                                        if (room.isInUse() || !room.isReadyToBeUsed()) {
                                            dungeonParty.forEach(p -> p.sendMessage(dm.getMessage("room-full").replace("{dungeon}", invite.getRoom().getDungeon().getName())));
                                            dm.removeInvite(id);
                                            return false;
                                        }
                                        room.setParty(dungeonParty);
                                        room.start(invite);
                                        return false;
                                    }
                                } else if (result == DungeonInvite.Result.ALREADY_ACCEPTED) {
                                    sender.sendMessage(dm.getMessage("already-accepted"));
                                } else if (result == DungeonInvite.Result.NOT_INVITED) {
                                    sender.sendMessage(dm.getMessage("not-invited"));
                                } else if (result == DungeonInvite.Result.ALREADY_IN_ROOM) {
                                    sender.sendMessage(dm.getMessage("already-in-room"));
                                } else {
                                    sender.sendMessage(dm.getMessage("dungeon-cooldown"));
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
                    return plugin.getDungeonManager().getInvites().stream().map((i) -> ""+i).filter(a -> a.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
