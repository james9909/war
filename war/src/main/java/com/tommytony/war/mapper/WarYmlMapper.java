package com.tommytony.war.mapper;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.KillstreakReward;
import com.tommytony.war.config.MySQLConfig;
import com.tommytony.war.job.RestoreYmlWarzonesJob;
import com.tommytony.war.utility.Reward;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarYmlMapper {

    public static void load() {
        (War.war.getDataFolder()).mkdir();
        (new File(War.war.getDataFolder().getPath() + "/dat")).mkdir();
        File warYmlFile = new File(War.war.getDataFolder().getPath() + "/war.yml");

        boolean newWar = false;
        if (!warYmlFile.exists()) {
            // Save defaults to disk
            newWar = true;
            WarYmlMapper.save();
            War.war.log("war.yml settings file created.", Level.INFO);
        }

        YamlConfiguration warYmlConfig = YamlConfiguration.loadConfiguration(warYmlFile);
        ConfigurationSection warRootSection = warYmlConfig.getConfigurationSection("set");

        // warzones
        List<String> warzones = warRootSection.getStringList("war.info.warzones");
        RestoreYmlWarzonesJob restoreWarzones = new RestoreYmlWarzonesJob(warzones);    // during conversion, this should execute just after the RestoreTxtWarzonesJob
        if (War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, restoreWarzones) == -1) {
            War.war.log("Failed to schedule warzone-restore job. No warzone was loaded.", Level.WARNING);
        }

        // zone makers
        List<String> makers = warRootSection.getStringList("war.info.zonemakers");
        War.war.getZoneMakerNames().clear();
        for (String makerName : makers) {
            if (makerName != null && !makerName.equals("")) {
                War.war.getZoneMakerNames().add(makerName);
            }
        }

        // command whitelist
        List<String> whitelist = warRootSection.getStringList("war.info.commandwhitelist");
        War.war.getCommandWhitelist().clear();
        for (String command : whitelist) {
            if (command != null && !command.equals("")) {
                War.war.getCommandWhitelist().add(command);
            }
        }

		ConfigurationSection rewardsSection = warRootSection.getConfigurationSection("team.default.reward");
        if (rewardsSection != null) {

            Reward winReward = RewardYmlMapper.fromConfigToReward(rewardsSection, "win");
            War.war.getDefaultInventories().setWinReward(winReward);

            Reward lossReward = RewardYmlMapper.fromConfigToReward(rewardsSection, "loss");
            War.war.getDefaultInventories().setLossReward(lossReward);
        }

        // default loadouts
        ConfigurationSection loadoutsSection = warRootSection.getConfigurationSection("classes");
        War.war.getDefaultInventories().setLoadouts(LoadoutYmlMapper.fromConfigToLoadouts(loadoutsSection));

        // War settings
        ConfigurationSection warConfigSection = warRootSection.getConfigurationSection("war.config");
        War.war.getWarConfig().loadFrom(warConfigSection);

        // Warzone default settings
        ConfigurationSection warzoneConfigSection = warRootSection.getConfigurationSection("warzone.default.config");
        War.war.getWarzoneDefaultConfig().loadFrom(warzoneConfigSection);

        // Team default settings
        ConfigurationSection teamConfigSection = warRootSection.getConfigurationSection("team.default.config");
        War.war.getTeamDefaultConfig().loadFrom(teamConfigSection);

        // Killstreak config
        if (warRootSection.isConfigurationSection("war.killstreak")) {
            War.war.setKillstreakReward(new KillstreakReward(warRootSection.getConfigurationSection("war.killstreak")));
        }

        if (warRootSection.isConfigurationSection("war.mysql")) {
            War.war.setMysqlConfig(new MySQLConfig(warRootSection.getConfigurationSection("war.mysql")));
        }
    }

    public static void save() {
        YamlConfiguration warYmlConfig = new YamlConfiguration();
        ConfigurationSection warRootSection = warYmlConfig.createSection("set");
        (new File(War.war.getDataFolder().getPath())).mkdir();
        (new File(War.war.getDataFolder().getPath() + "/dat")).mkdir();

        // default loadouts
        ConfigurationSection loadoutsSection = warRootSection.createSection("classes");
        LoadoutYmlMapper.fromLoadoutsToConfig(War.war.getDefaultInventories().getLoadouts(), loadoutsSection);

        // War settings
        ConfigurationSection warConfigSection = warRootSection.createSection("war.config");
        War.war.getWarConfig().saveTo(warConfigSection);

        // Warzone default settings
        ConfigurationSection warzoneConfigSection = warRootSection.createSection("warzone.default.config");
        War.war.getWarzoneDefaultConfig().saveTo(warzoneConfigSection);

        // Team default settings
        ConfigurationSection teamDefault = warRootSection.createSection("team.default");
        ConfigurationSection teamConfigSection = teamDefault.createSection("config");
        War.war.getTeamDefaultConfig().saveTo(teamConfigSection);

        // defaultReward
        ConfigurationSection rewardsSection = teamDefault.createSection("reward");
        RewardYmlMapper.fromRewardToConfig(rewardsSection, "win", War.war.getDefaultInventories().getWinReward());
        RewardYmlMapper.fromRewardToConfig(rewardsSection, "loss", War.war.getDefaultInventories().getLossReward());

        ConfigurationSection warInfoSection = warRootSection.createSection("war.info");

        // warzones
        List<String> warzones = new ArrayList<>();
        for (Warzone zone : War.war.getWarzones()) {
            warzones.add(zone.getName());
        }
        warInfoSection.set("warzones", warzones);

        // zone makers
        warInfoSection.set("zonemakers", War.war.getZoneMakerNames());

        // whitelisted commands during a game
        warInfoSection.set("commandwhitelist", War.war.getCommandWhitelist());

        ConfigurationSection killstreakSection = warRootSection.createSection("war.killstreak");
        War.war.getKillstreakReward().saveTo(killstreakSection);

        ConfigurationSection mysqlSection = warRootSection.createSection("war.mysql");
        War.war.getMysqlConfig().saveTo(mysqlSection);

        // Save to disk
        File warConfigFile = new File(War.war.getDataFolder().getPath() + "/war.yml");
        try {
            warYmlConfig.save(warConfigFile);
        } catch (IOException e) {
            War.war.log("Failed to save war.yml", Level.WARNING);
            e.printStackTrace();
        }
    }
}
