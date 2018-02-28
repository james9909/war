package com.tommytony.war.utility;

import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;


public class PlayerState {

    private Player player;

    private float exhaustion;
    private float saturation;
    private int foodLevel;
    private double health;
    private GameMode gamemode;
    private Collection<PotionEffect> potionEffects;
    private float exp;
    private int level;
    private boolean fly;
    private Location location;

    public PlayerState(Player player) {
        this.player = player;
        this.update();
    }

    public void update() {
        this.location = player.getLocation();
        this.gamemode = player.getGameMode();
        this.health = player.getHealth();
        this.exhaustion = player.getExhaustion();
        this.saturation = player.getSaturation();
        this.foodLevel = player.getFoodLevel();
        this.potionEffects = player.getActivePotionEffects();
        this.exp =  player.getExp();
        this.level = player.getLevel();
        this.fly = player.isFlying();
    }

    public Player getPlayer() {
        return player;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public float getSaturation() {
        return saturation;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public double getHealth() {
        return health;
    }

    public GameMode getGamemode() {
        return gamemode;
    }

    public Collection<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public float getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public boolean canFly() {
        return fly;
    }

    public Location getLocation() {
        return location;
    }
}
