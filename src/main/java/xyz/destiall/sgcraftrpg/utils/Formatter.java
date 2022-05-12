package xyz.destiall.sgcraftrpg.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.destiall.sgcraftrpg.economy.SGCraftEconomy;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatter {
    private static final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");
    private static final Pattern hexBracketPattern = Pattern.compile("<#([A-Fa-f0-9]){6}}");

    public static String color(String s) {
        Matcher matcher = hexPattern.matcher(s);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = s.substring(0, matcher.start());
            final String after = s.substring(matcher.end());
            s = before + hexColor + after;
            matcher = hexPattern.matcher(s);
        }
        matcher = hexBracketPattern.matcher(s);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = s.substring(0, matcher.start());
            final String after = s.substring(matcher.end());
            s = before + hexColor + after;
            matcher = hexBracketPattern.matcher(s);
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String sender(String s, CommandSender sender) {
        return variables(s, "{name}", sender.getName());
    }

    public static String variables(String s, String replace, String arg) {
        return color(s.replace(replace, arg));
    }

    public static String money(String s, double balance, SGCraftEconomy economy) {
        String format = color(s);
        for (Pair<String,Double> coin : economy.getCoins()) {
            double representation = coin.getValue();
            int amount = 0;
            while (balance >= representation) {
                amount++;
                balance -= representation;
            }
            format = variables(format, "{" + coin.getKey() + "}", "" + amount);
        }
        return format;
    }

    public static Integer balance(double balance, SGCraftEconomy economy, String coin) {
        HashMap<String, Integer> mapping = new HashMap<>();
        for (Pair<String,Double> c : economy.getCoins()) {
            double representation = c.getValue();
            int amount = 0;
            while (balance >= representation) {
                amount++;
                balance -= representation;
            }
            mapping.put(c.getKey(), amount);
        }
        return mapping.get(coin);
    }
}
