package com.tommytony.war.job;

import org.bukkit.configuration.ConfigurationSection;

public class RestoreYmlPortalsJob implements Runnable {

    private final ConfigurationSection portalConfig;

    public RestoreYmlPortalsJob(ConfigurationSection portalConfig) {
        this.portalConfig = portalConfig;
    }

        @Override
    public void run() {

    }
}
