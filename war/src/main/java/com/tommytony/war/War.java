package com.tommytony.war;

import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.config.FlagReturn;
import com.tommytony.war.config.InventoryBag;
import com.tommytony.war.config.KillstreakReward;
import com.tommytony.war.config.MySQLConfig;
import com.tommytony.war.config.ScoreboardType;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.TeamConfigBag;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.TeamSpawnStyle;
import com.tommytony.war.config.WarConfig;
import com.tommytony.war.config.WarConfigBag;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.config.WarzoneConfigBag;
import com.tommytony.war.job.CapturePointTimer;
import com.tommytony.war.job.HelmetProtectionTask;
import com.tommytony.war.job.UpdateScoreboardJob;
import com.tommytony.war.listeners.MagicSpellsListener;
import com.tommytony.war.listeners.WarBlockListener;
import com.tommytony.war.listeners.WarEntityListener;
import com.tommytony.war.listeners.WarPlayerListener;
import com.tommytony.war.mapper.WarYmlMapper;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import com.tommytony.war.stats.StatManager;
import com.tommytony.war.structure.ZonePortal;
import com.tommytony.war.ui.UIManager;
import com.tommytony.war.utility.InventoryManager;
import com.tommytony.war.utility.Reward;
import com.tommytony.war.utility.SizeCounter;
import com.tommytony.war.utility.WarLogFormatter;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Main class of War
 *
 * @author tommytony, Tim Düsterhus
 * @package bukkit.tommytony.war
 */
public class War extends JavaPlugin {

    static final boolean HIDE_BLANK_MESSAGES = true;
    public static War war;
    private InventoryManager inventoryManager;
    private static ResourceBundle messages = ResourceBundle.getBundle("messages");
    private final Set<String> zoneMakerNames = new HashSet<>();
    private final Set<String> commandWhitelist = new HashSet<>();
    private final List<Warzone> incompleteZones = new ArrayList<>();
    private final Set<UUID> zoneMakersImpersonatingPlayers = new HashSet<>();
    private final Map<UUID, String> wandBearers = new HashMap<>(); // player uuid to zonename
    private final List<String> deadlyAdjectives = new ArrayList<>();
    private final List<String> killerVerbs = new ArrayList<>();
    private final InventoryBag defaultInventories = new InventoryBag();
    private final WarConfigBag warConfig = new WarConfigBag();
    private final WarzoneConfigBag warzoneDefaultConfig = new WarzoneConfigBag();
    private final TeamConfigBag teamDefaultConfig = new TeamConfigBag();
    // general
    private WarPlayerListener playerListener = new WarPlayerListener();
    private WarEntityListener entityListener = new WarEntityListener();
    private WarBlockListener blockListener = new WarBlockListener();
    private MagicSpellsListener magicSpellsListener = new MagicSpellsListener();
    private WarCommandHandler commandHandler = new WarCommandHandler();
    private PluginDescriptionFile desc = null;
    private boolean loaded = false;
    // Zones and hub
    private List<Warzone> warzones = new ArrayList<>();
    private KillstreakReward killstreakReward;
    private MySQLConfig mysqlConfig;
    private Economy econ = null;
    private UIManager UIManager;
    private HashMap<String, ZonePortal> portals = new HashMap<>();

    public War() {
        super();
        War.war = this;
    }

    public static void reloadLanguage() {
        String[] parts = War.war.getWarConfig().getString(WarConfig.LANGUAGE).replace("-", "_").split("_");
        Locale lang = new Locale(parts[0]);
        if (parts.length >= 2) {
            lang = new Locale(parts[0], parts[1]);
        }
        War.messages = ResourceBundle.getBundle("messages", lang);
    }

    /**
     * @see JavaPlugin#onEnable()
     * @see War#loadWar()
     */
    public void onEnable() {
        this.loadWar();
    }

    /**
     * @see JavaPlugin#onDisable()
     * @see War#unloadWar()
     */
    public void onDisable() {
        this.unloadWar();
    }

    /**
     * Initializes war
     */
    public void loadWar() {
        this.setLoaded(true);
        this.desc = this.getDescription();

        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        } catch (Exception e) {
            this.log("SQLite3 driver not found!", Level.SEVERE);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.UIManager = new UIManager(this);

        // Register events
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.entityListener, this);
        pm.registerEvents(this.blockListener, this);
        pm.registerEvents(this.magicSpellsListener, this);
        pm.registerEvents(this.UIManager, this);

        // Add defaults
        warConfig.put(WarConfig.BUILDINZONESONLY, false);
        warConfig.put(WarConfig.DISABLEBUILDMESSAGE, false);
        warConfig.put(WarConfig.DISABLEPVPMESSAGE, false);
        warConfig.put(WarConfig.KEEPOLDZONEVERSIONS, true);
        warConfig.put(WarConfig.MAXZONES, 12);
        warConfig.put(WarConfig.PVPINZONESONLY, false);
        warConfig.put(WarConfig.TNTINZONESONLY, false);
        warConfig.put(WarConfig.RESETSPEED, 5000);
        warConfig.put(WarConfig.MAXSIZE, 750);
        warConfig.put(WarConfig.LANGUAGE, Locale.getDefault().toString());
        warConfig.put(WarConfig.AUTOJOIN, "");
        warConfig.put(WarConfig.TPWARMUP, 0);
        warConfig.put(WarConfig.LOADOUTCMD, "");

        warzoneDefaultConfig.put(WarzoneConfig.AUTOASSIGN, false);
        warzoneDefaultConfig.put(WarzoneConfig.BLOCKHEADS, true);
        warzoneDefaultConfig.put(WarzoneConfig.DISABLED, false);
        warzoneDefaultConfig.put(WarzoneConfig.FRIENDLYFIRE, false);
        warzoneDefaultConfig.put(WarzoneConfig.GLASSWALLS, true);
        warzoneDefaultConfig.put(WarzoneConfig.INSTABREAK, false);
        warzoneDefaultConfig.put(WarzoneConfig.MINPLAYERS, 1);
        warzoneDefaultConfig.put(WarzoneConfig.MINTEAMS, 1);
        warzoneDefaultConfig.put(WarzoneConfig.MONUMENTHEAL, 5);
        warzoneDefaultConfig.put(WarzoneConfig.NOCREATURES, false);
        warzoneDefaultConfig.put(WarzoneConfig.NODROPS, false);
        warzoneDefaultConfig.put(WarzoneConfig.PVPINZONE, true);
        warzoneDefaultConfig.put(WarzoneConfig.REALDEATHS, false);
        warzoneDefaultConfig.put(WarzoneConfig.RESETONEMPTY, false);
        warzoneDefaultConfig.put(WarzoneConfig.RESETONCONFIGCHANGE, false);
        warzoneDefaultConfig.put(WarzoneConfig.RESETONLOAD, false);
        warzoneDefaultConfig.put(WarzoneConfig.RESETONUNLOAD, false);
        warzoneDefaultConfig.put(WarzoneConfig.UNBREAKABLE, false);
        warzoneDefaultConfig.put(WarzoneConfig.DEATHMESSAGES, true);
        warzoneDefaultConfig.put(WarzoneConfig.JOINMIDBATTLE, true);
        warzoneDefaultConfig.put(WarzoneConfig.AUTOJOIN, false);
        warzoneDefaultConfig.put(WarzoneConfig.SCOREBOARD, ScoreboardType.NONE);
        warzoneDefaultConfig.put(WarzoneConfig.SOUPHEALING, false);
        warzoneDefaultConfig.put(WarzoneConfig.ALLOWENDER, true);
        warzoneDefaultConfig.put(WarzoneConfig.RESETBLOCKS, true);
        warzoneDefaultConfig.put(WarzoneConfig.CAPTUREPOINTTIME, 15);
        warzoneDefaultConfig.put(WarzoneConfig.PREPTIME, 0);

        teamDefaultConfig.put(TeamConfig.FLAGMUSTBEHOME, true);
        teamDefaultConfig.put(TeamConfig.FLAGPOINTSONLY, false);
        teamDefaultConfig.put(TeamConfig.FLAGRETURN, FlagReturn.BOTH);
        teamDefaultConfig.put(TeamConfig.LIFEPOOL, 7);
        teamDefaultConfig.put(TeamConfig.MAXSCORE, 10);
        teamDefaultConfig.put(TeamConfig.NOHUNGER, false);
        teamDefaultConfig.put(TeamConfig.RESPAWNTIMER, 0);
        teamDefaultConfig.put(TeamConfig.SATURATION, 10);
        teamDefaultConfig.put(TeamConfig.SPAWNSTYLE, TeamSpawnStyle.SMALL);
        teamDefaultConfig.put(TeamConfig.TEAMSIZE, 10);
        teamDefaultConfig.put(TeamConfig.PERMISSION, "war.player");
        teamDefaultConfig.put(TeamConfig.XPKILLMETER, false);
        teamDefaultConfig.put(TeamConfig.KILLSTREAK, false);
        teamDefaultConfig.put(TeamConfig.BLOCKWHITELIST, "all");
        teamDefaultConfig.put(TeamConfig.PLACEBLOCK, true);
        teamDefaultConfig.put(TeamConfig.APPLYPOTION, "");
        teamDefaultConfig.put(TeamConfig.ECOREWARD, 0.0);
        teamDefaultConfig.put(TeamConfig.INVENTORYDROP, false);
        teamDefaultConfig.put(TeamConfig.BORDERDROP, false);

        this.getDefaultInventories().clearLoadouts();

        List<ItemStack> winRewardList = new ArrayList<>();
        winRewardList.add(new ItemStack(Material.CAKE, 1));
        Reward winReward = new Reward(winRewardList);
        this.getDefaultInventories().setWinReward(winReward);

        List<ItemStack> lossRewardList = new ArrayList<>();
        lossRewardList.add(new ItemStack(Material.COAL, 1));
        Reward lossReward = new Reward(lossRewardList);
        this.getDefaultInventories().setLossReward(lossReward);

        this.inventoryManager = new InventoryManager();

        this.getCommandWhitelist().add("who");
        this.setKillstreakReward(new KillstreakReward());
        this.setMysqlConfig(new MySQLConfig());
        StatManager.initializeTables();

        // Add constants
        this.getDeadlyAdjectives().clear();
        for (String adjective : this.getString("pvp.kill.adjectives").split(";")) {
            this.getDeadlyAdjectives().add(adjective);
        }
        this.getKillerVerbs().clear();
        for (String verb : this.getString("pvp.kill.verbs").split(";")) {
            this.getKillerVerbs().add(verb);
        }

        // Load files
        WarYmlMapper.load();

        // Start tasks
        HelmetProtectionTask helmetProtectionTask = new HelmetProtectionTask();
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, helmetProtectionTask, 250, 100);

        CapturePointTimer cpt = new CapturePointTimer();
        cpt.runTaskTimer(this, 100, 20);
        UpdateScoreboardJob usj = new UpdateScoreboardJob();
        usj.runTaskTimerAsynchronously(this, 0, 10);

        if (this.mysqlConfig.isEnabled()) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (Exception ex) {
                this.log("MySQL driver not found!", Level.SEVERE);
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.econ = rsp.getProvider();
            }
        }

        War.reloadLanguage();

        // Get own log file
        try {
            // Create an appending file handler
            new File(this.getDataFolder() + "/temp/").mkdir();
            FileHandler handler = new FileHandler(this.getDataFolder() + "/temp/war.log", true);

            // Add to War-specific logger
            Formatter formatter = new WarLogFormatter();
            handler.setFormatter(formatter);
            this.getLogger().addHandler(handler);
        } catch (IOException e) {
            this.getLogger().log(Level.WARNING, "Failed to create War log file");
        }

        // Size check
        long datSize = SizeCounter.getFileOrDirectorySize(new File(this.getDataFolder() + "/dat/")) / 1024 / 1024;
        long tempSize = SizeCounter.getFileOrDirectorySize(new File(this.getDataFolder() + "/temp/")) / 1024 / 1024;

        if (datSize + tempSize > 100) {
            this.log("War data files are taking " + datSize + "MB and its temp files " + tempSize + "MB. Consider permanently deleting old warzone versions and backups in /plugins/War/temp/.", Level.WARNING);
        }

        this.log("War v" + this.desc.getVersion() + " is on.", Level.INFO);
    }

    /**
     * Cleans up war
     */
    public void unloadWar() {
        for (Warzone warzone : this.warzones) {
            warzone.unload();
        }
        this.warzones.clear();

        this.getServer().getScheduler().cancelTasks(this);
        this.playerListener.purgeLatestPositions();

        HandlerList.unregisterAll(this);
        this.log("War v" + this.desc.getVersion() + " is off.", Level.INFO);
        this.setLoaded(false);
    }

    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, String, String[])
     */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return this.commandHandler.handle(sender, cmd, args);
    }

    public void safelyEnchant(ItemStack target, Enchantment enchantment, int level) {
        if (level > enchantment.getMaxLevel()) {
            target.addUnsafeEnchantment(enchantment, level);
        } else {
            target.addEnchantment(enchantment, level);
        }
    }

    public String updateTeamFromNamedParams(Team team, CommandSender commandSender, String[] arguments) {
        try {
            Map<String, String> namedParams = new HashMap<>();
            Map<String, String> thirdParameter = new HashMap<>();
            for (String namedPair : arguments) {
                String[] pairSplit = namedPair.split(":");
                if (pairSplit.length == 2) {
                    namedParams.put(pairSplit[0].toLowerCase(), pairSplit[1]);
                } else if (pairSplit.length == 3) {
                    namedParams.put(pairSplit[0].toLowerCase(), pairSplit[1]);
                    thirdParameter.put(pairSplit[0].toLowerCase(), pairSplit[2]);
                }
            }

            StringBuilder returnMessage = new StringBuilder();
            returnMessage.append(team.getTeamConfig().updateFromNamedParams(namedParams));

            return returnMessage.toString();
        } catch (Exception e) {
            return "PARSE-ERROR";
        }
    }

    public String updateZoneFromNamedParams(Warzone warzone, CommandSender commandSender, String[] arguments) {
        try {
            Map<String, String> namedParams = new HashMap<>();
            Map<String, String> thirdParameter = new HashMap<>();
            for (String namedPair : arguments) {
                String[] pairSplit = namedPair.split(":");
                if (pairSplit.length == 2) {
                    namedParams.put(pairSplit[0].toLowerCase(), pairSplit[1]);
                } else if (pairSplit.length == 3) {
                    namedParams.put(pairSplit[0].toLowerCase(), pairSplit[1]);
                    thirdParameter.put(pairSplit[0].toLowerCase(), pairSplit[2]);
                }
            }

            StringBuilder returnMessage = new StringBuilder();
            if (namedParams.containsKey("author")) {
                for (String author : namedParams.get("author").split(",")) {
                    if (!author.equals("") && !warzone.getAuthors().contains(author)) {
                        warzone.addAuthor(author);
                        returnMessage.append(" author " + author + " added.");
                    }
                }
            }
            if (namedParams.containsKey("deleteauthor")) {
                for (String author : namedParams.get("deleteauthor").split(",")) {
                    if (warzone.getAuthors().contains(author)) {
                        warzone.getAuthors().remove(author);
                        returnMessage.append(" " + author + " removed from zone authors.");
                    }
                }
            }

            returnMessage.append(warzone.getWarzoneConfig().updateFromNamedParams(namedParams));
            returnMessage.append(warzone.getTeamDefaultConfig().updateFromNamedParams(namedParams));

            return returnMessage.toString();
        } catch (Exception e) {
            return "PARSE-ERROR";
        }
    }

    public String updateFromNamedParams(CommandSender commandSender, String[] arguments) {
        try {
            Map<String, String> namedParams = new HashMap<>();
            Map<String, String> thirdParameter = new HashMap<>();
            for (String namedPair : arguments) {
                String[] pairSplit = namedPair.split(":");
                if (pairSplit.length == 2) {
                    namedParams.put(pairSplit[0].toLowerCase(), pairSplit[1]);
                } else if (pairSplit.length == 3) {
                    namedParams.put(pairSplit[0].toLowerCase(), pairSplit[1]);
                    thirdParameter.put(pairSplit[0].toLowerCase(), pairSplit[2]);
                }
            }

            StringBuilder returnMessage = new StringBuilder();

            returnMessage.append(this.getWarConfig().updateFromNamedParams(namedParams));
            returnMessage.append(this.getWarzoneDefaultConfig().updateFromNamedParams(namedParams));
            returnMessage.append(this.getTeamDefaultConfig().updateFromNamedParams(namedParams));

            return returnMessage.toString();
        } catch (Exception e) {
            return "PARSE-ERROR";
        }
    }

    public String printConfig(Team team) {
        ChatColor teamColor = ChatColor.AQUA;

        ChatColor normalColor = ChatColor.WHITE;

        String teamConfigStr = "";
        InventoryBag invs = team.getInventories();
        // teamConfigStr += getLoadoutsString(invs);

        for (TeamConfig teamConfig : TeamConfig.values()) {
            Object value = team.getTeamConfig().getValue(teamConfig);
            if (value != null) {
                teamConfigStr += " " + teamConfig.toStringWithValue(value).replace(":", ":" + teamColor) + normalColor;
            }
        }

        return " ::" + teamColor + "Team " + team.getName() + teamColor + " config" + normalColor + "::" + ifEmptyInheritedForTeam(teamConfigStr);
    }

    public String printConfig(Warzone zone) {
        ChatColor teamColor = ChatColor.AQUA;
        ChatColor zoneColor = ChatColor.DARK_AQUA;
        ChatColor authorColor = ChatColor.GREEN;
        ChatColor normalColor = ChatColor.WHITE;

        String warzoneConfigStr = "";
        for (WarzoneConfig warzoneConfig : WarzoneConfig.values()) {
            Object value = zone.getWarzoneConfig().getValue(warzoneConfig);
            if (value != null) {
                warzoneConfigStr += " " + warzoneConfig.toStringWithValue(value).replace(":", ":" + zoneColor) + normalColor;
            }
        }

        String teamDefaultsStr = "";
        // teamDefaultsStr += getLoadoutsString(zone.getDefaultInventories());
        for (TeamConfig teamConfig : TeamConfig.values()) {
            Object value = zone.getTeamDefaultConfig().getValue(teamConfig);
            if (value != null) {
                teamDefaultsStr += " " + teamConfig.toStringWithValue(value).replace(":", ":" + teamColor) + normalColor;
            }
        }

        return "::" + zoneColor + "Warzone " + authorColor + zone.getName() + zoneColor + " config" + normalColor + "::" + " author:" + authorColor + ifEmptyEveryone(zone.getAuthorsString()) + normalColor + ifEmptyInheritedForWarzone(warzoneConfigStr) + " ::" + teamColor + "Team defaults" + normalColor + "::" + ifEmptyInheritedForWarzone(teamDefaultsStr);
    }

    private String ifEmptyInheritedForWarzone(String maybeEmpty) {
        if (maybeEmpty.equals("")) {
            maybeEmpty = " all values inherited (see " + ChatColor.GREEN + "/warcfg -p)" + ChatColor.WHITE;
        }
        return maybeEmpty;
    }

    private String ifEmptyInheritedForTeam(String maybeEmpty) {
        if (maybeEmpty.equals("")) {
            maybeEmpty = " all values inherited (see " + ChatColor.GREEN + "/warcfg -p" + ChatColor.WHITE + " and " + ChatColor.GREEN + "/zonecfg -p" + ChatColor.WHITE + ")";
        }
        return maybeEmpty;
    }

    private String ifEmptyEveryone(String maybeEmpty) {
        if (maybeEmpty.equals("")) {
            maybeEmpty = "*";
        }
        return maybeEmpty;
    }

    public String printConfig() {
        ChatColor teamColor = ChatColor.AQUA;
        ChatColor zoneColor = ChatColor.DARK_AQUA;
        ChatColor globalColor = ChatColor.DARK_GREEN;
        ChatColor normalColor = ChatColor.WHITE;

        String warConfigStr = "";
        for (WarConfig warConfig : WarConfig.values()) {
            warConfigStr += " " + warConfig.toStringWithValue(this.getWarConfig().getValue(warConfig)).replace(":", ":" + globalColor) + normalColor;
        }

        String warzoneDefaultsStr = "";
        for (WarzoneConfig warzoneConfig : WarzoneConfig.values()) {
            warzoneDefaultsStr += " " + warzoneConfig.toStringWithValue(this.getWarzoneDefaultConfig().getValue(warzoneConfig)).replace(":", ":" + zoneColor) + normalColor;
        }

        String teamDefaultsStr = "";
        // teamDefaultsStr += getLoadoutsString(this.getDefaultInventories());
        for (TeamConfig teamConfig : TeamConfig.values()) {
            teamDefaultsStr += " " + teamConfig.toStringWithValue(this.getTeamDefaultConfig().getValue(teamConfig)).replace(":", ":" + teamColor) + normalColor;
        }

        return normalColor + "::" + globalColor + "War config" + normalColor + "::" + warConfigStr + normalColor + " ::" + zoneColor + "Warzone defaults" + normalColor + "::" + warzoneDefaultsStr + normalColor + " ::" + teamColor + "Team defaults" + normalColor + "::" + teamDefaultsStr;
    }

    private void setZoneRallyPoint(String warzoneName, Player player) {
        Warzone zone = this.findWarzone(warzoneName);
        if (zone == null) {
            this.badMsg(player, "Can't set rally point. No such warzone.");
        } else {
            zone.setRallyPoint(player.getLocation());
            WarzoneYmlMapper.save(zone);
        }
    }

    public void addWarzone(Warzone zone) {
        this.warzones.add(zone);
    }

    public List<Warzone> getWarzones() {
        return this.warzones;
    }

    /**
     * Get a list of warzones that are not disabled.
     *
     * @return List of enabled warzones.
     */
    public List<Warzone> getEnabledWarzones() {
        List<Warzone> enabledZones = new ArrayList<>(this.warzones.size());
        for (Warzone zone : this.warzones) {
            if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
                enabledZones.add(zone);
            }
        }
        return enabledZones;
    }

    /**
     * Get a list of warzones that have players in them.
     *
     * @return List of enabled warzones with players.
     */
    public List<Warzone> getActiveWarzones() {
        List<Warzone> activeZones = new ArrayList<>(this.warzones.size());
        for (Warzone zone : this.warzones) {
            if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED) && zone.getPlayerCount() > 0) {
                activeZones.add(zone);
            }
        }
        return activeZones;
    }

    public void msg(CommandSender sender, String str) {
        if (messages.containsKey(str)) {
            str = this.getString(str);
        }
        if (HIDE_BLANK_MESSAGES && (str == null || str.isEmpty())) {
            return;
        }
        if (sender instanceof Player) {
            StringBuilder output = new StringBuilder(ChatColor.GRAY.toString()).append(this.getString("war.prefix")).append(ChatColor.WHITE).append(' ');
            output.append(this.colorKnownTokens(str, ChatColor.WHITE));
            sender.sendMessage(output.toString());
        } else {
            sender.sendMessage(str);
        }
    }

    public void badMsg(CommandSender sender, String str) {
        if (messages.containsKey(str)) {
            str = this.getString(str);
        }
        if (HIDE_BLANK_MESSAGES && (str == null || str.isEmpty())) {
            return;
        }
        if (sender instanceof Player) {
            StringBuilder output = new StringBuilder(ChatColor.GRAY.toString()).append(this.getString("war.prefix")).append(ChatColor.RED).append(' ');
            output.append(this.colorKnownTokens(str, ChatColor.RED));
            sender.sendMessage(output.toString());
        } else {
            sender.sendMessage(str);
        }
    }

    public void msg(CommandSender sender, String str, Object... obj) {
        if (messages.containsKey(str)) {
            str = this.getString(str);
        }
        if (HIDE_BLANK_MESSAGES && (str == null || str.isEmpty())) {
            return;
        }
        if (sender instanceof Player) {
            StringBuilder output = new StringBuilder(ChatColor.GRAY.toString()).append(this.getString("war.prefix")).append(ChatColor.WHITE).append(' ');
            output.append(MessageFormat.format(this.colorKnownTokens(str, ChatColor.WHITE), obj));
            sender.sendMessage(output.toString());
        } else {
            StringBuilder output = new StringBuilder();
            output.append(MessageFormat.format(str, obj));
            sender.sendMessage(output.toString());
        }
    }

    public void badMsg(CommandSender sender, String str, Object... obj) {
        if (messages.containsKey(str)) {
            str = this.getString(str);
        }
        if (HIDE_BLANK_MESSAGES && (str == null || str.isEmpty())) {
            return;
        }
        if (sender instanceof Player) {
            StringBuilder output = new StringBuilder(ChatColor.GRAY.toString()).append(this.getString("war.prefix")).append(ChatColor.RED).append(' ');
            output.append(MessageFormat.format(this.colorKnownTokens(str, ChatColor.RED), obj));
            sender.sendMessage(output.toString());
        } else {
            StringBuilder output = new StringBuilder();
            output.append(MessageFormat.format(str, obj));
            sender.sendMessage(output.toString());
        }
    }

    /**
     * Colors the teams and examples in messages
     *
     * @param str message-string
     * @param msgColor current message-color
     * @return String Message with colored teams
     */
    private String colorKnownTokens(String str, ChatColor msgColor) {
        str = str.replaceAll("Ex -", ChatColor.BLUE + "Ex -" + ChatColor.GRAY);
        str = str.replaceAll("\\\\", ChatColor.BLUE + "\\\\" + ChatColor.GRAY);
        str = str.replaceAll("->", ChatColor.LIGHT_PURPLE + "->" + ChatColor.GRAY);
        str = str.replaceAll("/teamcfg", ChatColor.AQUA + "/teamcfg" + ChatColor.GRAY);
        str = str.replaceAll("Team defaults", ChatColor.AQUA + "Team defaults" + ChatColor.GRAY);
        str = str.replaceAll("Team config", ChatColor.AQUA + "Team config" + ChatColor.GRAY);
        str = str.replaceAll("/zonecfg", ChatColor.DARK_AQUA + "/zonecfg" + ChatColor.GRAY);
        str = str.replaceAll("Warzone defaults", ChatColor.DARK_AQUA + "Warzone defaults" + ChatColor.GRAY);
        str = str.replaceAll("Warzone config", ChatColor.DARK_AQUA + "Warzone config" + ChatColor.GRAY);
        str = str.replaceAll("/warcfg", ChatColor.DARK_GREEN + "/warcfg" + ChatColor.GRAY);
        str = str.replaceAll("War config", ChatColor.DARK_GREEN + "War config" + ChatColor.GRAY);
        str = str.replaceAll("Print config", ChatColor.WHITE + "Print config" + ChatColor.GREEN);

        for (TeamKind kind : TeamKind.values()) {
            str = str.replaceAll(" " + kind.toString(), " " + kind.getColor() + kind.toString() + msgColor);
            str = str.replaceAll(kind.toString() + "/", kind.getColor() + kind.toString() + ChatColor.GRAY + "/");
        }

        return str;
    }

    /**
     * Logs a specified message with a specified level
     *
     * @param str message to log
     * @param lvl level to use
     */
    public void log(String str, Level lvl) {
        this.getLogger().log(lvl, str);
    }

    // the only way to find a zone that has only one corner
    public Warzone findWarzone(String warzoneName) {
        for (Warzone warzone : this.warzones) {
            if (warzone.getName().toLowerCase().equals(warzoneName.toLowerCase())) {
                return warzone;
            }
        }
        for (Warzone warzone : this.incompleteZones) {
            if (warzone.getName().equals(warzoneName)) {
                return warzone;
            }
        }
        return null;
    }

    /**
     * Checks whether the given player is allowed to play in a certain team
     *
     * @param player Player to check
     * @param team Team to check
     * @return true if the player may play in the team
     */
    public boolean canPlayWar(Player player, Team team) {
        return player.hasPermission(team.getTeamConfig().resolveString(TeamConfig.PERMISSION));
    }

    /**
     * Checks whether the given player is allowed to warp.
     *
     * @param player Player to check
     * @return true if the player may warp
     */
    public boolean canWarp(Player player) {
        return player.hasPermission("war.warp");
    }

    /**
     * Checks whether the given player is allowed to build outside zones
     *
     * @param player Player to check
     * @return true if the player may build outside zones
     */
    public boolean canBuildOutsideZone(Player player) {
        if (this.getWarConfig().getBoolean(WarConfig.BUILDINZONESONLY)) {
            return player.hasPermission("war.build");
        }
        return true;
    }

    /**
     * Checks whether the given player is allowed to pvp outside zones
     *
     * @param player Player to check
     * @return true if the player may pvp outside zones
     */
    public boolean canPvpOutsideZones(Player player) {
        if (this.getWarConfig().getBoolean(WarConfig.PVPINZONESONLY)) {
            return player.hasPermission("war.pvp");
        } else {
            return true;
        }
    }

    /**
     * Checks whether the given player is a zone maker
     *
     * @param player Player to check
     * @return true if the player is a zone maker
     */
    public boolean isZoneMaker(Player player) {
        // sort out disguised first
        if (zoneMakersImpersonatingPlayers.contains(player.getUniqueId())) {
            return false;
        }
        if (zoneMakerNames.contains(player.getName())) {
            return true;
        }

        return player.hasPermission("war.zonemaker");
    }

    /**
     * Checks whether the given player is a War admin
     *
     * @param player Player to check
     * @return true if the player is a War admin
     */
    public boolean isWarAdmin(Player player) {
        return player.hasPermission("war.admin");
    }

    public void addWandBearer(Player player, String zoneName) {
        if (this.wandBearers.containsKey(player.getUniqueId())) {
            String alreadyHaveWand = this.wandBearers.get(player.getUniqueId());
            if (player.getInventory().first(Material.WOOD_SWORD) != -1) {
                if (zoneName.equals(alreadyHaveWand)) {
                    this.badMsg(player, "You already have a wand for zone " + alreadyHaveWand + ". Drop the wooden sword first.");
                } else {
                    // new zone, already have sword
                    this.wandBearers.remove(player.getUniqueId());
                    this.wandBearers.put(player.getUniqueId(), zoneName);
                    this.msg(player, "Switched wand to zone " + zoneName + ".");
                }
            } else {
                // lost his sword, or new warzone
                if (zoneName.equals(alreadyHaveWand)) {
                    // same zone, give him a new sword
                    player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD, 1, (byte) 8));
                    this.msg(player, "Here's a new sword for zone " + zoneName + ".");
                }
            }
        } else {
            if (player.getInventory().firstEmpty() == -1) {
                this.badMsg(player, "Your inventory is full. Please drop an item and try again.");
            } else {
                this.wandBearers.put(player.getUniqueId(), zoneName);
                player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD, 1, (byte) 8));
                // player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.WOOD_SWORD));
                this.msg(player, "You now have a wand for zone " + zoneName + ". Left-click with wooden sword for corner 1. Right-click for corner 2.");
                War.war.log(player.getName() + " now has a wand for warzone " + zoneName, Level.INFO);
            }
        }
    }

    public boolean isWandBearer(Player player) {
        return this.wandBearers.containsKey(player.getUniqueId());
    }

    public String getWandBearerZone(Player player) {
        if (this.isWandBearer(player)) {
            return this.wandBearers.get(player.getUniqueId());
        }
        return "";
    }

    public void removeWandBearer(Player player) {
        if (this.wandBearers.containsKey(player.getUniqueId())) {
            this.wandBearers.remove(player.getUniqueId());
        }
    }

    public Warzone zoneOfZoneWallAtProximity(Location location) {
        for (Warzone zone : this.warzones) {
            if (zone.getWorld() == location.getWorld() && zone.isNearWall(location)) {
                return zone;
            }
        }
        return null;
    }

    public Set<String> getZoneMakerNames() {
        return this.zoneMakerNames;
    }

    public Set<String> getCommandWhitelist() {
        return this.commandWhitelist;
    }

    public Set<UUID> getZoneMakersImpersonatingPlayers() {
        return this.zoneMakersImpersonatingPlayers;
    }

    public List<Warzone> getIncompleteZones() {
        return this.incompleteZones;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public InventoryBag getDefaultInventories() {
        return defaultInventories;
    }

    public List<String> getDeadlyAdjectives() {
        return deadlyAdjectives;
    }

    public List<String> getKillerVerbs() {
        return killerVerbs;
    }

    public TeamConfigBag getTeamDefaultConfig() {
        return this.teamDefaultConfig;
    }

    public WarzoneConfigBag getWarzoneDefaultConfig() {
        return this.warzoneDefaultConfig;
    }

    public WarConfigBag getWarConfig() {
        return this.warConfig;
    }

    public KillstreakReward getKillstreakReward() {
        return killstreakReward;
    }

    public void setKillstreakReward(KillstreakReward killstreakReward) {
        this.killstreakReward = killstreakReward;
    }

    public MySQLConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public void setMysqlConfig(MySQLConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
    }

    public String getString(String key) {
        return messages.getString(key);
    }

    public Locale getLoadedLocale() {
        return messages.getLocale();
    }

    /**
     * Convert serialized effect to actual effect.
     *
     * @param serializedEffect String stored in configuration. Format: TYPE;DURATION;AMPLIFY
     * @return Potion effect or null otherwise
     */
    public PotionEffect getPotionEffect(String serializedEffect) {
        String[] arr = serializedEffect.split(";");
        if (arr.length != 3) {
            return null;
        }
        try {
            PotionEffectType type = PotionEffectType.getByName(arr[0]);
            int duration = Integer.parseInt(arr[1]);
            int amplification = Integer.parseInt(arr[2]);
            return new PotionEffect(type, duration, amplification);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public Economy getEconomy() {
        return econ;
    }

    public UIManager getUIManager() {
        return UIManager;
    }

    public void setUIManager(UIManager UIManager) {
        this.UIManager = UIManager;
    }

    private String getCoordinates(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return String.format("%d,%d,%d", x, y, z);
    }

    public void addPortal(ZonePortal portal) {
        String coordinates = getCoordinates(portal.getLocation());
        portals.put(coordinates, portal);
    }

    public void removePortal(ZonePortal portal) {
        String coordinates = getCoordinates(portal.getLocation());
        portals.remove(coordinates);
    }

    public ZonePortal getZonePortal(Location other) {
        String coordinates = getCoordinates(other);
        return portals.getOrDefault(coordinates, null);
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}
