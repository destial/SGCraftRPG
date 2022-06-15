package xyz.destiall.sgcraftrpg.path;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Path {
    private final List<Location> points;
    private final String name;
    private File yml;
    private Color color;
    private Location lastAddedPoint;
    private int distance;

    public Path(String name) {
        this.name = name;
        points = new ArrayList<>();
        color = Color.YELLOW;
        distance = 20;
    }

    public Path(File file) {
        this.yml = file;
        this.name = file.getName().replace(".yml", "").trim();
        points = new ArrayList<>();
        color = Color.YELLOW;
        distance = 20;
    }

    public void clear() {
        points.clear();
    }

    public void load() {
        try {
            boolean create = false;
            if (!yml.exists()) {
                yml.createNewFile();
                create = true;
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(yml);
            if (create) {
                points.add(new Location(Bukkit.getWorlds().get(0), 100, 100, 100));
                points.add(new Location(Bukkit.getWorlds().get(0), 101, 100, 100));
                points.add(new Location(Bukkit.getWorlds().get(0), 100, 100, 101));
                color = Color.YELLOW;
                distance = 20;
                config.set("points", points);
                config.set("color.red", color.getRed());
                config.set("color.green", color.getGreen());
                config.set("color.blue", color.getBlue());
                config.set("distance", distance);

                config.save(yml);
            } else {
                points.addAll((Collection<Location>) config.getList("points", new ArrayList<>()));
                color = Color.fromRGB(config.getInt("color.red", 255), config.getInt("color.green", 255), config.getInt("color.blue", 255));
                if (!points.isEmpty()) {
                    lastAddedPoint = points.get(points.size() - 1);
                }
                distance = config.getInt("distance", 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void save(File directory) {
        try {
            File yml = this.yml != null ? this.yml : new File(directory, name + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(yml);
            config.set("points", points);
            config.set("color.red", color.getRed());
            config.set("color.green", color.getGreen());
            config.set("color.blue", color.getBlue());
            config.set("distance", distance);
            config.save(yml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPoint(Location location) {
        if (lastAddedPoint != null) {
            if (lastAddedPoint.getBlockX() == location.getBlockX() &&
                lastAddedPoint.getBlockY() == location.getBlockY() &&
                lastAddedPoint.getBlockZ() == location.getBlockZ())
                return;
        }

        int y = location.getBlockY();
        if (!location.getBlock().isEmpty()) {
            y += 1;
        }

        location.setX(location.getBlockX() + 0.5f);
        location.setY(y + 0.5f);
        location.setZ(location.getBlockZ() + 0.5f);
        points.add(location);
        lastAddedPoint = location;
    }

    public void render(Player player) {
        Particle.DustOptions options = new Particle.DustOptions(color, 2);
        for (Location point : points) {
            if (point.distanceSquared(player.getLocation()) > distance * distance) continue;

            player.spawnParticle(Particle.REDSTONE, point, 1, options);
        }
    }
}
