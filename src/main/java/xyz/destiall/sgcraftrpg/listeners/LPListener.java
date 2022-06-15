package xyz.destiall.sgcraftrpg.listeners;

import net.luckperms.api.event.user.UserCacheLoadEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.List;

public class LPListener {
    private final SGCraftRPG plugin;

    public LPListener(SGCraftRPG plugin) {
        this.plugin = plugin;
        plugin.PERMISSIONS.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, this::onUserEvent);
        plugin.PERMISSIONS.getEventBus().subscribe(plugin, UserCacheLoadEvent.class, this::onUserEvent);
        plugin.PERMISSIONS.getEventBus().subscribe(plugin, UserLoadEvent.class, this::onUserEvent);
    }

    private void onUserEvent(UserLoadEvent e) {
        expire(e.getUser());
    }

    private void onUserEvent(UserCacheLoadEvent e) {
        expire(e.getUser());
    }

    private void onUserEvent(UserDataRecalculateEvent e) {
        expire(e.getUser());
    }

    private void expire(User user) {
        long nodes = user.getNodes(NodeType.INHERITANCE)
                .stream()
                .filter(Node::hasExpired).count();

        if (nodes > 0) {
            String name = user.getUsername();
            if (name == null) return;
            List<String> commands = plugin.getConfig().getStringList("luckperms-expiry-commands");
            for (String cmd : commands) {
                try {
                    String run = cmd.replace("{player}", name);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), run);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
