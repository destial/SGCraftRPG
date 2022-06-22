package xyz.destiall.sgcraftrpg.wg;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.CommandStringFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.destiall.sgcraftrpg.SGCraftRPG;
import xyz.destiall.sgcraftrpg.utils.Formatter;

import java.io.File;

public class WGManager {
    private final SGCraftRPG plugin;
    private final FlagRegistry registry;
    private YamlConfiguration config;

    public WGManager(SGCraftRPG plugin) {
        this.plugin = plugin;
        registry = WorldGuard.getInstance().getFlagRegistry();

        for (Flags f : Flags.values()) {
            Flag<?> flag = null;
            switch (f.getType()) {
                case STATE -> flag = new StateFlag(f.getName(), false);
                case DOUBLE -> flag = new DoubleFlag(f.getName());
                case STRING -> flag = new StringFlag(f.getName());
                case COMMAND -> flag = new CommandStringFlag(f.getName());
                case INTEGER -> flag = new IntegerFlag(f.getName());
                case LOCATION -> flag = new LocationFlag(f.getName());
            }
            if (flag == null) continue;
            registerFlag(flag);
        }
        load();
    }

    public void disable() {}

    public void reload() {
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "wgflags.yml");
        try {
            boolean create = !file.exists();
            config = YamlConfiguration.loadConfiguration(file);
            if (create) {
                config.set("messages." + Flags.NO_CLASS_ENTER.getName(), "&cYou cannot enter this region without a class!");
                config.set("messages." + Flags.NO_CLASS_EXIT.getName(), "&cYou cannot exit this region without a class!");
                config.set("messages." + Flags.NO_NORMAL_TOOLS.getName(), "&cYou cannot use normal tools in this region!");
                config.set("messages." + Flags.LEVEL_REQUIREMENT.getName(), "&cYou are not at a high enough level to enter here! You need to be level {level}");

                config.save(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMessage(Flags flag) {
        return Formatter.color(config.getString("messages." + flag.getName()));
    }

    public void registerFlag(Flag<?> flag) {
        if (registry == null) return ;
        try {
            registry.register(flag);
        } catch (Exception ignored) {}
    }

    public <F extends Flag<?>> F getFlag(Flags flag, Class<F> clazz) {
        if (registry == null) return null;
        return clazz.cast(registry.get(flag.getName()));
    }

    public enum Flags {
        NO_CLASS_ENTER(Type.STATE),
        NO_CLASS_EXIT(Type.STATE),
        NO_NORMAL_TOOLS(Type.STATE),
        LEVEL_REQUIREMENT(Type.INTEGER)
        ;

        private final Type type;
        private final String name;

        Flags(Type type) {
            name = name().toLowerCase().replace("_", "-");
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        enum Type {
            STATE,
            INTEGER,
            STRING,
            COMMAND,
            DOUBLE,
            LOCATION,
        }
    }
}
