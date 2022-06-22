package xyz.destiall.sgcraftrpg.packjail;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PackJailManager {
    private final SGCraftRPG plugin;
    private final Map<UUID, Location> lastLocations;
    private Location location;
    private YamlConfiguration config;
    private YamlConfiguration database;

    public PackJailManager(SGCraftRPG plugin) {
        this.plugin = plugin;
        lastLocations = new ConcurrentHashMap<>();
        reload();
        loadDatabase();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "packjail.yml");
        try {
            boolean create = !file.exists();
            config = YamlConfiguration.loadConfiguration(file);

            if (create) {
                config.set("location", location = new Location(Bukkit.getWorlds().get(0), 100, 69, 100));
                config.set("jail-message", "&cYou need to accept our resource pack in order to play!");
                config.set("unjail-message", "&aThank you for accepting our resource pack!");
                config.save(file);
                return;
            }

            location = config.getLocation("location");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDatabase() {
        File datafile = new File(plugin.getDataFolder(), "jail.yml");
        try {
            boolean create = false;
            if (!datafile.exists()) {
                datafile.createNewFile();
                create = true;
            }

            database = YamlConfiguration.loadConfiguration(datafile);

            if (!create) {
                for (String key : database.getKeys(false)) {
                    UUID uuid = UUID.fromString(key);
                    Location location = database.getLocation(key);
                    lastLocations.put(uuid, location);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disable() {
        for (Map.Entry<UUID, Location> entry : lastLocations.entrySet()) {
            database.set(entry.getKey().toString(), entry.getValue());
        }

        File datafile = new File(plugin.getDataFolder(), "jail.yml");
        try {
            database.save(datafile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendJailMessage(Player player) {
        player.sendMessage(Formatter.color(config.getString("jail-message", "&cYou need to accept our resource pack in order to play!")));
    }

    public void sendUnjailMessage(Player player) {
        player.sendMessage(Formatter.color(config.getString("unjail-message", "&aThank you for accepting our resource pack!")));
    }

    public void jail(Player player) {
        lastLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(location);
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendJailMessage(player), 20L);
    }

    public void back(Player player) {
        Location location = lastLocations.remove(player.getUniqueId());
        if (location == null) return;
        player.teleport(location);
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendUnjailMessage(player), 20L);
    }

    public boolean isInJail(Player player) {
        return lastLocations.containsKey(player.getUniqueId());
    }
}
