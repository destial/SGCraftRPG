package xyz.destiall.sgcraftrpg.listeners;

import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import xyz.destiall.sgcraftrpg.SGCraftRPG;

import java.util.List;
import java.util.stream.Collectors;

public class LPListener {
    private final SGCraftRPG plugin;

    public LPListener(SGCraftRPG plugin) {
        this.plugin = plugin;
        plugin.PERMISSIONS.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, this::onUserCacheChange);
    }

    private void onUserCacheChange(UserDataRecalculateEvent e) {
        List<InheritanceNode> nodes = e.getUser().getNodes(NodeType.INHERITANCE)
                .stream()
                .filter(Node::hasExpired)
                .collect(Collectors.toList());

        if (nodes.size() > 0) {
            String name = e.getUser().getUsername();
            if (name == null) return;
            List<String> commands = plugin.getConfig().getStringList("luckperms-expiry-commands");
            commands.forEach((cmd) -> {
                try {
                    String run = cmd.replace("{player}", name);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), run);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}
