package xyz.destiall.sgcraftrpg.placeholder;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.util.Arrays;
import java.util.Iterator;

public class PAPIHook extends PlaceholderExpansion {
    private final SGCraftRPG plugin;
    public PAPIHook(SGCraftRPG plugin) {
        this.plugin = plugin;
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "sgcraftrpg";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("classicon")) {
            PlayerData data = SkillAPI.getPlayerData(player);
            PlayerClass clazz = data.getMainClass();
            if (clazz == null) return "";
            return clazz.getData().getPrefix();
        }
        if (params.equalsIgnoreCase("classiconspace")) {
            PlayerData data = SkillAPI.getPlayerData(player);
            PlayerClass clazz = data.getMainClass();
            if (clazz == null) return "";
            return clazz.getData().getPrefix() + " ";
        }
        if (params.equalsIgnoreCase("classname")) {
            PlayerData data = SkillAPI.getPlayerData(player);
            PlayerClass clazz = data.getMainClass();
            if (clazz == null) return "None";
            return clazz.getData().getName();
        }
        if (params.equalsIgnoreCase("skillpoints")) {
            PlayerData data = SkillAPI.getPlayerData(player);
            return "" + (Integer.parseInt(PlaceholderAPI.setPlaceholders(player, "%sapi_default_currentlevel%")) - data.getInvestedSkillPoints());
        }
        if (params.equalsIgnoreCase("mana")) {
            PlayerData data = SkillAPI.getPlayerData(player);
            return "" + data.getMana();
        }
        if (params.startsWith("effect") && player instanceof Player) {
            String[] split = params.split("_");
            boolean formatted = split[0].endsWith("f");
            PotionEffectType type = null;
            if (split.length > 1) {
                String effect = String.join("_", Arrays.copyOfRange(split, 1, split.length)).toUpperCase();
                try {
                    type = PotionEffectType.getByName(effect);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Iterator<PotionEffect> it = ((Player) player).getActivePotionEffects().iterator();
                if (it.hasNext()) {
                    type = it.next().getType();
                }
            }

            if (type != null) {
                PotionEffect e = ((Player) player).getPotionEffect(type);
                if (e != null) {
                    int seconds = e.getDuration() / 20;
                    String duration = "" + seconds;
                    if (formatted) {
                        int mins = seconds / 60;
                        seconds -= mins * 60;
                        int hours = mins / 60;
                        mins -= hours * 60;
                        String h = (hours < 10 ? "0" : "") + hours;
                        String m = (mins < 10 ? "0" : "") + mins;
                        String s = (seconds < 10 ? "0" : "") + seconds;
                        duration = h + ":" + m + ":" + s;
                    }
                    return friendlyName(e.getType()) + " " + (e.getAmplifier() + 1) + " " + duration;
                }

            }
            return "";
        }
        if (params.startsWith("durationeffect") && player instanceof Player) {
            String[] split = params.split("_");
            boolean formatted = split[0].endsWith("f");
            PotionEffectType type = null;
            if (split.length > 1) {
                String effect = String.join("_", Arrays.copyOfRange(split, 1, split.length)).toUpperCase();
                try {
                    type = PotionEffectType.getByName(effect);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Iterator<PotionEffect> it = ((Player) player).getActivePotionEffects().iterator();
                if (it.hasNext()) {
                    type = it.next().getType();
                }
            }

            if (type != null) {
                PotionEffect e = ((Player) player).getPotionEffect(type);
                if (e != null) {
                    int seconds = e.getDuration() / 20;
                    if (formatted) {
                        int mins = seconds / 60;
                        seconds -= mins * 60;
                        int hours = mins / 60;
                        mins -= hours * 60;
                        String h = (hours < 10 ? "0" : "") + hours;
                        String m = (mins < 10 ? "0" : "") + mins;
                        String s = (seconds < 10 ? "0" : "") + seconds;
                        return h + ":" + m + ":" + s;
                    }
                    return "" + (e.getDuration() / 20);
                }
            }
            return "0";
        }
        if (params.startsWith("amplifiereffect") && player instanceof Player) {
            String[] split = params.split("_");
            PotionEffectType type = null;
            if (split.length > 1) {
                String effect = String.join("_", Arrays.copyOfRange(split, 1, split.length)).toUpperCase();
                try {
                    type = PotionEffectType.getByName(effect);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Iterator<PotionEffect> it = ((Player) player).getActivePotionEffects().iterator();
                if (it.hasNext()) {
                    type = it.next().getType();
                }
            }

            if (type != null) {
                for (PotionEffect e : ((Player) player).getActivePotionEffects()) {
                    if (e.getType() == type) {
                        return "" + (e.getAmplifier() + 1);
                    }
                }
            }
            return "0";
        }

        if (plugin.getEconomy() == null) {
            return "0";
        }

        double money = plugin.getEconomyProvider().getBalance(player);
        if (params.equalsIgnoreCase("balance")) {
            String balance = plugin.getEconomy().getConfig().getString("message.balance");
            return Formatter.money(balance, money, plugin.getEconomy());
        }
        try {
            double price = Double.parseDouble(params);
            return Formatter.money(price, plugin.getEconomy(), true);
        } catch (Exception ignored) {}

        Number coins = Formatter.balance(money, plugin.getEconomy(), params);
        if (coins != null) return "" + coins;
        return "0";
    }

    protected String friendlyName(PotionEffectType type) {
        String name = type.getName().replace("_", " ");
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
