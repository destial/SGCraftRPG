package xyz.destiall.sgcraftrpg.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

public class PAPIHook extends PlaceholderExpansion {
    private final SGCraftRPG plugin;
    public PAPIHook(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "sgcraftrpg";
    }

    @Override
    public @NotNull String getAuthor() {
        return "destiall";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (plugin.getEconomy() == null) {
            return "0";
        }
        double money = plugin.ECONOMY.getBalance(player);
        if (params.equalsIgnoreCase("balance")) {
            String balance = plugin.getEconConfig().getString("message.balance");
            return Formatter.money(balance, money, plugin.getEconomy());
        }
        return "" + Formatter.balance(money, plugin.getEconomy(), params);
    }
}
