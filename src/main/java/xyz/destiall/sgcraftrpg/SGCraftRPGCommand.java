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
import xyz.destiall.sgcraftrpg.packjail.PackJailManager;
import xyz.destiall.sgcraftrpg.path.Path;
import xyz.destiall.sgcraftrpg.path.PathManager;
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
            if (args[0].equalsIgnoreCase("dungeonreload")) {
                if (sender.hasPermission(Permissions.ADMIN)) {
                    plugin.getDungeonManager().reload();
                    sender.sendMessage(Formatter.color("&aReloaded SGCraftRPG configuration..."));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("configreload")) {
                if (sender.hasPermission(Permissions.ADMIN)) {
                    plugin.configReload();
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
                                } else if (result == DungeonInvite.Result.COOLDOWN) {
                                    for (String msg : dm.getMessage("dungeon-cooldown"))
                                        sender.sendMessage(msg);
                                } else if (result == DungeonInvite.Result.NOT_HIGH_LEVEL) {
                                    for (String msg : dm.getMessage("not-high-level"))
                                        sender.sendMessage(msg.replace("{level}", "" + invite.getRoom().getDungeon().getLevelRequirement()));
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
            } else if (args[0].equalsIgnoreCase("pack")) {
                if (!sender.hasPermission(Permissions.ADMIN)) {
                    sender.sendMessage(Formatter.color("&cYou do not have permission!"));
                    return false;
                }

                if (args.length > 2) {
                    PackJailManager pjm = plugin.getPackJailManager();
                    if (args[1].equalsIgnoreCase("jail")) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            sender.sendMessage(Formatter.color("&cPlayer not found!"));
                            return false;
                        }
                        if (pjm.isInJail(player)) {
                            pjm.sendJailMessage(player);
                            sender.sendMessage(Formatter.color("&cPlayer is already in pack jail!"));
                            return false;
                        }
                        pjm.jail(player);
                    } else if (args[1].equalsIgnoreCase("unjail")) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            sender.sendMessage(Formatter.color("&cPlayer not found!"));
                            return false;
                        }
                        if (!pjm.isInJail(player)) {
                            pjm.sendUnjailMessage(player);
                            sender.sendMessage(Formatter.color("&cPlayer is not in pack jail!"));
                            return false;
                        }
                        pjm.back(player);
                    }
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("path")) {
                if (!sender.hasPermission(Permissions.ADMIN)) {
                    sender.sendMessage(Formatter.color("&cYou do not have permission!"));
                    return false;
                }
                PathManager pm = plugin.getPathManager();
                Player player;
                if (args.length > 2) {
                    Path path = pm.getPath(args[2]);
                    if (args.length > 3) {
                        player = Bukkit.getPlayer(args[3]);
                        if (args[1].equalsIgnoreCase("render")) {
                            if (player == null) {
                                sender.sendMessage(Formatter.color("&cPlayer not found!"));
                                return false;
                            }
                            if (path == null) {
                                sender.sendMessage(Formatter.color("&cThat is not a valid path name!"));
                                return false;
                            }
                            if (pm.isRenderingPath(path, player)) {
                                sender.sendMessage(Formatter.color("&cPlayer is already rendering that path!"));
                                return false;
                            }
                            pm.startRenderPath(path, player);
                            sender.sendMessage(Formatter.color("&aStarting rendering path " + path.getName() + " for " + player.getName()));
                        } else if (args[1].equalsIgnoreCase("stoprender")) {
                            if (player == null) {
                                sender.sendMessage(Formatter.color("&cPlayer not found!"));
                                return false;
                            }
                            if (path == null) {
                                sender.sendMessage(Formatter.color("&cThat is not a valid path name!"));
                                return false;
                            }
                            if (!pm.isRenderingPath(path, player)) {
                                sender.sendMessage(Formatter.color("&cPlayer is not rendering that path!"));
                                return false;
                            }
                            pm.stopRenderPath(path, player);
                            sender.sendMessage(Formatter.color("&aStopped rendering path " + path.getName() + " for " + player.getName()));
                        }
                        return false;
                    }
                    if (args[1].equalsIgnoreCase("create")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Formatter.color("&cYou have to be a player to use this command!"));
                            return false;
                        }
                        player = (Player) sender;
                        if (pm.isWalkingPath(player)) {
                            player.sendMessage(Formatter.color("&cPlayer is already creating a path!"));
                            return false;
                        }
                        Path newPath = pm.createPath(args[2]);
                        if (newPath == null) {
                            player.sendMessage(Formatter.color("&cPath with that name already exists!"));
                            return false;
                        }
                        pm.startWalkPath(newPath, player);
                        player.sendMessage(Formatter.color("&aWalking path " + newPath.getName()));
                    }
                    return false;
                }

                if (args[1].equalsIgnoreCase("end")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Formatter.color("&cYou have to be a player to use this command!"));
                        return false;
                    }
                    player = (Player) sender;
                    if (!pm.isWalkingPath(player)) {
                        player.sendMessage(Formatter.color("&cPlayer is not walking path!"));
                        return false;
                    }
                    pm.stopWalkPath(player);
                    player.sendMessage(Formatter.color("&aStopped walking path."));
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
            if (args.length == 1) return Stream.of("configreload", "dungeonreload", "dungeon", "pack", "path").filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());

            if (args.length == 2 && args[0].equalsIgnoreCase("dungeon")) {
                return Stream.of("start", "end", "accept").filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("pack")) {
                return Stream.of("jail", "unjail").filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("path")) {
                return Stream.of("render", "stoprender", "create", "end").filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("dungeon")) {
                if (args[1].equalsIgnoreCase("start")) {
                    return plugin.getDungeonManager().getDungeons().stream().map(Dungeon::getName).filter(a -> a.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
                } else if (args[1].equalsIgnoreCase("end")) {
                    return plugin.getDungeonManager().getInvites().stream().map(i -> ""+i).filter(a -> a.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
                }
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("path")) {
                if (args[1].equalsIgnoreCase("render") || args[1].equalsIgnoreCase("stoprender")) {
                    return plugin.getPathManager().getPaths().stream().map(Path::getName).filter(a -> a.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
