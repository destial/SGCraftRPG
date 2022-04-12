package xyz.destiall.sgcraftrpg.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class Permissions {
    public static final Permission SOLO = new Permission("sgcraftrpg.dungeon.solo");
    public static final Permission ADMIN = new Permission("sgcraftrpg.admin");

    static {
        ADMIN.addParent(SOLO, true);
        Bukkit.getPluginManager().addPermission(ADMIN);
        Bukkit.getPluginManager().addPermission(SOLO);
    }
}
