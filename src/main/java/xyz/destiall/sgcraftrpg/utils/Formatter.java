package xyz.destiall.sgcraftrpg.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.destiall.sgcraftrpg.economy.Coin;
import xyz.destiall.sgcraftrpg.economy.EconomyManager;

import java.util.HashMap;
import java.util.Map;
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

    public static String money(double money, EconomyManager economy, boolean shortForm) {
        StringBuilder builder = new StringBuilder();
        for (Coin c : economy.getCoins()) {
            double representation = c.getValue();
            int amount = 0;
            while (money >= representation) {
                amount++;
                money -= representation;
            }
            if (amount == 0) continue;
            builder.append(c.getColor());
            builder.append(amount);
            builder.append(shortForm ? c.getShortForm() : c.getName());
            builder.append(" ");
        }
        return color(builder.toString().trim());
    }

    public static String money(String s, double balance, EconomyManager economy) {
        String format = color(s);
        for (Coin coin : economy.getCoins()) {
            double representation = coin.getValue();
            int amount = 0;
            while (balance >= representation) {
                amount++;
                balance -= representation;
            }
            format = variables(format, "{" + coin.getTitle() + "}", "" + amount);
        }
        return format;
    }

    public static Number balance(double balance, EconomyManager economy, String coin) {
        HashMap<String, Number> mapping = new HashMap<>();
        for (Coin c : economy.getCoins()) {
            double representation = c.getValue();
            int amount = 0;
            while (balance >= representation) {
                amount++;
                balance -= representation;
            }
            mapping.put(c.getTitle(), amount);
        }
        return mapping.get(coin);
    }
}
