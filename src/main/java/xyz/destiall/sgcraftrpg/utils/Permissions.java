package xyz.destiall.sgcraftrpg.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class Permissions {
    public static final Permission ALL = new Permission("sgcraftrpg.*");
    public static final Permission ADMIN = new Permission("sgcraftrpg.admin");

    static {
        ADMIN.addParent(ALL, true);
        Bukkit.getPluginManager().addPermission(ADMIN);
    }
}
