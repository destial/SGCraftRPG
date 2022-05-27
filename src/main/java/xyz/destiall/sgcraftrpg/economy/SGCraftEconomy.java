package xyz.destiall.sgcraftrpg.economy;

import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Pair;

import java.util.ArrayList;

public class SGCraftEconomy {
    private final ArrayList<Pair<String, Double>> coins;

    public SGCraftEconomy(SGCraftRPG plugin) {
        coins = new ArrayList<>();
        for (String coin : plugin.getEconConfig().getConfigurationSection("economy").getKeys(false)) {
            coins.add(new Pair<>(coin, plugin.getEconConfig().getDouble("economy." + coin)));
        }
        coins.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
    }

    public ArrayList<Pair<String, Double>> getCoins() {
        return coins;
    }
}
