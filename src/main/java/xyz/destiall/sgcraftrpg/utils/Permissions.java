package xyz.destiall.sgcraftrpg.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class Permissions {
    public static final Permission SOLO = new Permission("sgcraftrpg.dungeon.solo");
    public static final Permission ADMIN = new Permission("sgcraftrpg.admin");

    public static void register() {
        ADMIN.addParent(SOLO, true);
        try {
            Bukkit.getPluginManager().addPermission(ADMIN);
            Bukkit.getPluginManager().addPermission(SOLO);
        } catch (Exception ignored) {}
    }

    public static void unregister() {
        try {
            Bukkit.getPluginManager().removePermission(ADMIN);
            Bukkit.getPluginManager().removePermission(SOLO);
        } catch (Exception ignored) {}
    }
}
