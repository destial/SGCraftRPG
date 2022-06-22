package xyz.destiall.sgcraftrpg.economy;

import org.bukkit.configuration.file.YamlConfiguration;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.io.File;
import java.util.ArrayList;

public class EconomyManager {
    private final SGCraftRPG plugin;
    private YamlConfiguration econConfig;
    private final ArrayList<Coin> coins;

    public EconomyManager(SGCraftRPG plugin) {
        this.plugin = plugin;
        coins = new ArrayList<>();
        load();
    }

    public void load() {
        File econFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!econFile.exists()) {
            plugin.saveResource("economy.yml", false);
        }

        econConfig = YamlConfiguration.loadConfiguration(econFile);
        for (String coin : econConfig.getConfigurationSection("economy").getKeys(false)) {
            coins.add(new Coin(coin, econConfig.getConfigurationSection("economy." + coin)));
        }

        coins.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
    }

    public void disable() {
        coins.clear();
    }

    public void reload() {
        disable();
        load();
    }

    public YamlConfiguration getConfig() {
        return econConfig;
    }

    public ArrayList<Coin> getCoins() {
        return coins;
    }
}
