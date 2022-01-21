package xyz.destiall.sgcraftrpg;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.sgcraftrpg.economy.SGCraftEconomy;
import xyz.destiall.sgcraftrpg.listeners.ItemListener;
import xyz.destiall.sgcraftrpg.listeners.VillagerListener;
import xyz.destiall.sgcraftrpg.placeholder.PAPIHook;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.io.File;
import java.util.List;

@SuppressWarnings("all")
public final class SGCraftRPG extends JavaPlugin implements Listener {
    public Economy ECONOMY;
    private FileConfiguration econConfig;
    private SGCraftEconomy sgCraftEconomy;
    private PAPIHook papiHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!setupEconomy()) {
            getLogger().severe("Unable to find economy provider!");
        } else {
            reloadConfig();
        }
        getServer().getPluginCommand("sgcraftrpg").setExecutor(new SGCraftRPGCommand(this));
        getServer().getPluginCommand("balance").setExecutor(new BalanceCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        papiHook = new PAPIHook(this);
        papiHook.register();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((JavaPlugin) this);
        getServer().getPluginCommand("sgcraftrpg").setExecutor(null);
        getServer().getPluginCommand("balance").setExecutor(null);
        papiHook.unregister();
    }

    @Override
    public void reloadConfig() {
        boolean save = false;
        if (!getConfig().contains("disable-spawning")) {
            getConfig().set("disable-spawning.armorsmith", true);
            getConfig().set("disable-spawning.toolsmith", true);
            getConfig().set("disable-spawning.weaponsmith", true);
            getConfig().set("disable-spawning.zombie_villager", true);
            save = true;
        }
        if (!getConfig().contains("disable-trades")) {
            List<String> disabledTrades = Lists.newArrayList("DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS");
            getConfig().set("disable-trades", disabledTrades);
            save = true;
        }
        if (!getConfig().contains("despawn-vanilla-items")) {
            List<String> disabledTrades = Lists.newArrayList("DIAMOND_SWORD", "DIAMOND_PICKAXE");
            getConfig().set("despawn-vanilla-items", disabledTrades);
            save = true;
        }
        if (save) {
            try {
                getConfig().save(new File(getDataFolder(), "config.yml"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.reloadConfig();
        File econFile = new File(getDataFolder(), "economy.yml");
        if (!econFile.exists()) saveResource("economy.yml", false);
        econConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "economy.yml"));
        sgCraftEconomy = new SGCraftEconomy(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        for (String key : getConfig().getConfigurationSection("emojis").getKeys(false)) {
            if ((getConfig().getBoolean("ignore-uppercase-emojis") && message.toLowerCase().contains(key.toLowerCase())) || message.contains(key)) {
                message = message.replace(key, getConfig().getString("emojis." + key));
            }
        }
        message = PlaceholderAPI.setPlaceholders(e.getPlayer(), message);
        e.setMessage(message);
        for (String key : getConfig().getConfigurationSection("emotes").getKeys(false)) {
            if ((getConfig().getBoolean("ignore-uppercase-emotes") && message.equalsIgnoreCase(key)) || key.equals(message)) {
                String format = getConfig().getString("emotes." + key);
                format = Formatter.sender(format, e.getPlayer());
                format = PlaceholderAPI.setPlaceholders(e.getPlayer(), format);
                e.setCancelled(true);
                getServer().broadcastMessage(format);
                break;
            }
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        ECONOMY = rsp.getProvider();
        return true;
    }

    public FileConfiguration getEconConfig() {
        return econConfig;
    }

    public SGCraftEconomy getEconomy() {
        return sgCraftEconomy;
    }
}
