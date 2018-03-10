package com.tommytony.war.job;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TeleportToSpawnTimer extends BukkitRunnable {

    private Player player;
    private Location location;

    public TeleportToSpawnTimer(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    @Override
    public void run() {
        player.setVelocity(new Vector());
        player.teleport(location);
        player.setVelocity(new Vector());
    }
}
