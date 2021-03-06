package xyz.destiall.sgcraftrpg.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

public class ChatListener implements Listener {
    private final SGCraftRPG plugin;

    public ChatListener(SGCraftRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        for (String key : plugin.getConfig().getConfigurationSection("emotes").getKeys(false)) {
            if ((plugin.getConfig().getBoolean("ignore-uppercase-emotes") && message.equalsIgnoreCase(key)) || key.equals(message)) {
                String format = plugin.getConfig().getString("emotes." + key);
                format = Formatter.sender(format, e.getPlayer());
                format = PlaceholderAPI.setPlaceholders(e.getPlayer(), format);
                plugin.getServer().broadcastMessage(format);
                e.setCancelled(true);
                return;
            }
        }

        if (!plugin.getConfig().getBoolean("chat-message-format.enabled")) {
            for (String key : plugin.getConfig().getConfigurationSection("emojis").getKeys(false)) {
                if ((plugin.getConfig().getBoolean("ignore-uppercase-emojis") && message.toLowerCase().contains(key.toLowerCase())) || message.contains(key)) {
                    String replace = plugin.getConfig().getString("emojis." + key, key);
                    message = message.replace(key, replace);
                }
            }
        }

        e.setMessage(message);
    }
}
