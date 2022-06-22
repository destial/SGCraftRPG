package xyz.destiall.sgcraftrpg;

import com.comphenix.protocol.ProtocolLibrary;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.sgcraftrpg.duel.DuelManager;
import xyz.destiall.sgcraftrpg.dungeon.DungeonManager;
import xyz.destiall.sgcraftrpg.economy.EconomyManager;
import xyz.destiall.sgcraftrpg.listeners.ChatListener;
import xyz.destiall.sgcraftrpg.listeners.DamageListener;
import xyz.destiall.sgcraftrpg.listeners.DuelListener;
import xyz.destiall.sgcraftrpg.listeners.DungeonListener;
import xyz.destiall.sgcraftrpg.listeners.InteractListener;
import xyz.destiall.sgcraftrpg.listeners.ItemListener;
import xyz.destiall.sgcraftrpg.listeners.LPListener;
import xyz.destiall.sgcraftrpg.listeners.VillagerListener;
import xyz.destiall.sgcraftrpg.packjail.PackJailManager;
import xyz.destiall.sgcraftrpg.path.PathManager;
import xyz.destiall.sgcraftrpg.placeholder.PAPIHook;
import xyz.destiall.sgcraftrpg.utils.Permissions;
import xyz.destiall.sgcraftrpg.wg.WGListener;
import xyz.destiall.sgcraftrpg.wg.WGManager;

public final class SGCraftRPG extends JavaPlugin {
    private static SGCraftRPG instance;

    private Economy economyProvider;
    private LuckPerms permissionsProvider;
    private EconomyManager sgCraftEconomy;
    private PAPIHook papiHook;
    private DungeonManager dungeonManager;
    private DuelManager duelManager;
    private PackJailManager packJailManager;
    private PathManager pathManager;
    private WGManager wgManager;

    @Override
    public void onLoad() {
        instance = this;
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            wgManager = new WGManager(this);
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!setupPermission()) {
            getLogger().severe("Unable to find LuckPerms provider!");
        }
        if (!setupEconomy()) {
            getLogger().severe("Unable to find Vault provider!");
        }

        papiHook = new PAPIHook(this);
        dungeonManager = new DungeonManager(this);
        duelManager = new DuelManager(this);
        packJailManager = new PackJailManager(this);
        pathManager = new PathManager(this);
        sgCraftEconomy = new EconomyManager(this);

        Permissions.register();

        registerCommand("sgcraftrpg", new SGCraftRPGCommand(this));
        registerCommand("balance", new BalanceCommand(this));

        registerEvents(new ChatListener(this));
        registerEvents(new VillagerListener(this));
        registerEvents(new ItemListener(this));
        registerEvents(new InteractListener(this));
        registerEvents(new DungeonListener(this));
        registerEvents(new DuelListener(this));
        registerEvents(new DamageListener(this));
        registerEvents(pathManager);
        if (wgManager != null) {
            registerEvents(new WGListener(wgManager));
        }
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
        packJailManager.disable();
        pathManager.disable();
        duelManager.disable();
        if (wgManager != null) {
            wgManager.disable();
        }
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }

    public void configReload() {
        super.reloadConfig();


        sgCraftEconomy.reload();
        packJailManager.reload();
        pathManager.reload();
        duelManager.reload();

        if (wgManager != null) {
            wgManager.reload();
        }
    }

    @Override
    public void reloadConfig() {
        configReload();
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
        permissionsProvider = provider.getProvider();
        new LPListener(this);
        return true;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economyProvider = rsp.getProvider();
        return true;
    }

    public Economy getEconomyProvider() {
        return economyProvider;
    }

    public LuckPerms getLuckPermsProvider() {
        return permissionsProvider;
    }

    public EconomyManager getEconomy() {
        return sgCraftEconomy;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public PackJailManager getPackJailManager() {
        return packJailManager;
    }

    public PathManager getPathManager() {
        return pathManager;
    }

    public static SGCraftRPG get() {
        return instance;
    }
}
