package xyz.destiall.sgcraftrpg;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.sgcraftrpg.utils.Formatter;
import xyz.destiall.sgcraftrpg.utils.Permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SGCraftRPGCommand implements CommandExecutor, TabExecutor {
    private final SGCraftRPG plugin;
    public SGCraftRPGCommand(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (sender.hasPermission(Permissions.ADMIN)) {
            if (args == null || args.length == 0) {
                sender.sendMessage(Formatter.color("&cCommands: /sgcraftrpg reload"));
                return false;
            }
            plugin.reloadConfig();
            sender.sendMessage(Formatter.color("&aReloaded SGChat configuration..."));
            return true;
        }
        sender.sendMessage(Formatter.color("&cYou do not have permission!"));
        Bukkit.getConsoleSender().sendMessage(Formatter.sender("&c{name} does not have permission: sgcraftrpg.admin", sender));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission(Permissions.ADMIN)) {
            return Collections.singletonList("reload");
        }
        return new ArrayList<>();
    }
}
