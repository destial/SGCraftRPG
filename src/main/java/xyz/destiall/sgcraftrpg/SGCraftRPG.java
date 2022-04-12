package xyz.destiall.sgcraftrpg;

import com.google.common.collect.Lists;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.sgcraftrpg.dungeon.DungeonManager;
import xyz.destiall.sgcraftrpg.economy.SGCraftEconomy;
import xyz.destiall.sgcraftrpg.listeners.ChatListener;
import xyz.destiall.sgcraftrpg.listeners.DungeonListener;
import xyz.destiall.sgcraftrpg.listeners.InteractListener;
import xyz.destiall.sgcraftrpg.listeners.ItemListener;
import xyz.destiall.sgcraftrpg.listeners.LPListener;
import xyz.destiall.sgcraftrpg.listeners.VillagerListener;
import xyz.destiall.sgcraftrpg.placeholder.PAPIHook;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class SGCraftRPG extends JavaPlugin {
    private static SGCraftRPG instance;

    public Economy ECONOMY;
    public LuckPerms PERMISSIONS;
    private FileConfiguration econConfig;
    private SGCraftEconomy sgCraftEconomy;
    private PAPIHook papiHook;
    private DungeonManager dungeonManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        if (!setupPermission()) {
            getLogger().severe("Unable to find LuckPerms provider!");
        }
        if (!setupEconomy()) {
            getLogger().severe("Unable to find Vault provider!");
        }

        papiHook = new PAPIHook(this);
        dungeonManager = new DungeonManager(this);
        papiHook.register();

        registerCommand("sgcraftrpg", new SGCraftRPGCommand(this));
        registerCommand("balance", new BalanceCommand(this));

        registerEvents(new ChatListener(this));
        registerEvents(new VillagerListener(this));
        registerEvents(new ItemListener(this));
        registerEvents(new InteractListener(this));
        registerEvents(new DungeonListener(this));

        setDefaults();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        registerCommand("sgcraftrpg", null);
        registerCommand("balance", null);
        papiHook.unregister();
        dungeonManager.disable();
    }

    public void setDefaults() {
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
        if (!getConfig().contains("disable-block-interactions")) {
            List<String> disabledBlocks = Lists.newArrayList("ENCHANTING_TABLE", "LEGACY_ENCHANTMENT_TABLE", "ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL", "LEGACY_ANVIL", "GRINDSTONE");
            getConfig().set("disable-block-interactions", disabledBlocks);
            save = true;
        }
        if (!getConfig().contains("luckperms-expiry-commands")) {
            getConfig().set("luckperms-expiry-commands", Collections.singletonList("/cmi nick off {player}"));
            save = true;
        }
        if (save) {
            try {
                getConfig().save(new File(getDataFolder(), "config.yml"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        File econFile = new File(getDataFolder(), "economy.yml");
        if (!econFile.exists()) saveResource("economy.yml", false);
        econConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "economy.yml"));
        sgCraftEconomy = new SGCraftEconomy(this);
        dungeonManager.reload();
    }

    private void registerCommand(String cmd, CommandExecutor executor) {
        getServer().getPluginCommand(cmd).setExecutor(executor);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private boolean setupPermission() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) return false;
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) return false;
        PERMISSIONS = provider.getProvider();
        new LPListener(this);
        return true;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        ECONOMY = rsp.getProvider();
        return true;
    }

    public FileConfiguration getEconConfig() {
        return econConfig;
    }

    public SGCraftEconomy getEconomy() {
        return sgCraftEconomy;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public static SGCraftRPG get() {
        return instance;
    }
}
