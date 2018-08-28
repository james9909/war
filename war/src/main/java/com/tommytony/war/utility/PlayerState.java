package com.tommytony.war.utility;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;


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
        this.fly = player.getAllowFlight();
    }

    public void resetPlayer(Player player) {
        player.setGameMode(getGamemode());
        double maxH = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.max(Math.min(getHealth(), maxH), 0.0D));
        player.setExhaustion(getExhaustion());
        player.setSaturation(getSaturation());
        player.setFoodLevel(getFoodLevel());
        PotionEffectHelper.restorePotionEffects(player, getPotionEffects());
        player.setLevel(getLevel());
        player.setExp(getExp());
        player.setAllowFlight(canFly());
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
