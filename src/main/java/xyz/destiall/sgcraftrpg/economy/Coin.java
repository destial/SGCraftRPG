package xyz.destiall.sgcraftrpg.economy;

import org.bukkit.configuration.ConfigurationSection;

public class Coin {
    private final String title;
    private final String color;
    private final String name;
    private final String shortForm;
    private final double value;

    public Coin(String title, ConfigurationSection section) {
        this.title = title;
        this.value = section.getDouble("value");
        this.color = section.getString("color");
        this.name = section.getString("name");
        this.shortForm = section.getString("short");
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public double getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    public String getShortForm() {
        return shortForm;
    }
}
