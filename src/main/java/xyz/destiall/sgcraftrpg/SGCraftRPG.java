package xyz.destiall.sgcraftrpg;

import com.comphenix.protocol.ProtocolLibrary;
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
import xyz.destiall.sgcraftrpg.listeners.DamageListener;
import xyz.destiall.sgcraftrpg.listeners.DungeonListener;
import xyz.destiall.sgcraftrpg.listeners.InteractListener;
import xyz.destiall.sgcraftrpg.listeners.ItemListener;
import xyz.destiall.sgcraftrpg.listeners.LPListener;
import xyz.destiall.sgcraftrpg.listeners.VillagerListener;
import xyz.destiall.sgcraftrpg.placeholder.PAPIHook;
import xyz.destiall.sgcraftrpg.utils.Permissions;

import java.io.File;

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
        File econFile = new File(getDataFolder(), "economy.yml");
        if (!econFile.exists()) saveResource("economy.yml", false);
        econConfig = YamlConfiguration.loadConfiguration(econFile);
        sgCraftEconomy = new SGCraftEconomy(this);

        papiHook.register();
        Permissions.register();

        registerCommand("sgcraftrpg", new SGCraftRPGCommand(this));
        registerCommand("balance", new BalanceCommand(this));

        registerEvents(new ChatListener(this));
        registerEvents(new VillagerListener(this));
        registerEvents(new ItemListener(this));
        registerEvents(new InteractListener(this));
        registerEvents(new DungeonListener(this));
        registerEvents(new DamageListener(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        registerCommand("sgcraftrpg", null);
        registerCommand("balance", null);
        Permissions.unregister();
        papiHook.unregister();
        dungeonManager.disable();
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        File econFile = new File(getDataFolder(), "economy.yml");
        if (!econFile.exists()) saveResource("economy.yml", false);
        econConfig = YamlConfiguration.loadConfiguration(econFile);
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
