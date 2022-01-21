package xyz.destiall.sgcraftrpg;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class BalanceCommand implements CommandExecutor, TabExecutor {
    private final SGCraftRPG plugin;

    public BalanceCommand(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        OfflinePlayer target = null;
        if (args != null && args.length > 0) {
            String name = args[0];
            target = Bukkit.getOfflinePlayer(name);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        }
        if (target != null) {
            String balance = plugin.getEconConfig().getString("message.balance");
            double money = plugin.ECONOMY.getBalance(target);
            sender.sendMessage(Formatter.money(balance, money, plugin.getEconomy()));
            return true;
        }
        sender.sendMessage("You cannot do that from console!");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return args != null ? Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()).stream().filter(p -> args.length == 0 || p.startsWith(args[0])).collect(Collectors.toList()) : new ArrayList<>();
    }
}
