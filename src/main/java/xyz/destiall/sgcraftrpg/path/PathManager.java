package xyz.destiall.sgcraftrpg.path;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PathManager implements Listener {
    private final SGCraftRPG plugin;
    private final Map<Path, Set<Player>> paths;
    private final Map<UUID, Path> walkingPath;
    private final File directory;
    private BukkitTask task;

    public PathManager(SGCraftRPG plugin) {
        this.plugin = plugin;
        walkingPath = new ConcurrentHashMap<>();
        paths = new ConcurrentHashMap<>();
        directory = new File(plugin.getDataFolder(), "paths" + File.separator);
        if (!directory.exists()) {
            directory.mkdir();
        }
        load();
    }

    public void load() {
        File[] files = directory.listFiles(f -> f.isFile() && f.getName().endsWith(".yml"));
        if (files == null || files.length == 0) return;
        for (File file : files) {
            Path path = new Path(file);
            path.load();
            paths.put(path, ConcurrentHashMap.newKeySet());
        }

        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Map.Entry<Path, Set<Player>> entry : paths.entrySet()) {
                for (Player player : entry.getValue()) {
                    entry.getKey().render(player);
                }
            }
        }, 0L, 10L);
    }

    public void reload() {
        disable();
        load();
    }

    public Path createPath(String name) {
        return paths.keySet().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElseGet(() -> {
            Path path = new Path(name);
            paths.put(path, ConcurrentHashMap.newKeySet());
            return path;
        });
    }

    public Path getPath(String name) {
        return paths.keySet().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void startWalkPath(Path path, Player player) {
        walkingPath.put(player.getUniqueId(), path);
        paths.put(path, ConcurrentHashMap.newKeySet());
        startRenderPath(path, player);
    }

    public void stopWalkPath(Player player) {
        Path path = walkingPath.remove(player.getUniqueId());
        if (path == null) return;
        path.save(directory);
        stopRenderPath(path, player);
    }

    public void startRenderPath(Path path, Player player) {
        paths.get(path).add(player);
    }

    public void stopRenderPath(Path path, Player player) {
        paths.get(path).remove(player);
    }

    public boolean isRenderingPath(Path path, Player player) {
        return paths.get(path).contains(player);
    }

    public boolean isWalkingPath(Player player) {
        return walkingPath.containsKey(player.getUniqueId());
    }

    public void disable() {
        if (task != null) task.cancel();
        walkingPath.clear();
        for (Path path : paths.keySet()) {
            path.clear();
        }
        paths.clear();
    }

    public Collection<Path> getPaths() {
        return paths.keySet();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Path path = walkingPath.get(e.getPlayer().getUniqueId());
        if (path == null) return;
        path.addPoint(e.getPlayer().getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        walkingPath.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        walkingPath.remove(e.getPlayer().getUniqueId());
        for (Set<Player> players : paths.values()) {
            players.remove(e.getPlayer());
        }
    }
}
