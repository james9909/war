package com.tommytony.war.ui;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.FlagReturn;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.bags.TeamConfigBag;
import com.tommytony.war.config.TeamSpawnStyle;
import com.tommytony.war.config.WarConfig;
import com.tommytony.war.config.bags.WarConfigBag;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.config.bags.WarzoneConfigBag;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

/**
 * Created by Connor on 7/28/2017.
 */
public class UIConfigHelper {

    static int addTeamConfigOptions(final ChestUI ui, final Player player, Inventory inv, final TeamConfigBag config, final Team team, final Warzone warzone, int i) {
        ItemStack item;
        ItemMeta meta;
        for (final TeamConfig option : TeamConfig.values()) {
            if (option.getTitle() == null) {
                continue;
            }
            String inheritance = "";
            if (!config.contains(option) && warzone != null) {
                if (warzone.getTeamDefaultConfig().contains(option)) {
                    inheritance = ChatColor.DARK_GRAY + "Inherited from warzone config defaults";
                } else {
                    inheritance = ChatColor.DARK_GRAY + "Inherited from War config defaults";
                }
            }
            String name = ChatColor.RESET + "" + ChatColor.YELLOW + option.getTitle();
            String status = ChatColor.GRAY + "Currently: ";
            String[] desc = option.getDescription().split("\n");
            for (int j = 0; j < desc.length; j++) {
                desc[j] = ChatColor.RESET + "" + ChatColor.GRAY + desc[j];
            }
            if (option.getConfigType() == Boolean.class) {
                status += config.resolveBoolean(option) ? ChatColor.GREEN + "true" : ChatColor.DARK_GRAY + "false";
                item = new Dye(config.resolveBoolean(option) ? DyeColor.LIME : DyeColor.GRAY).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                    config.put(option, !config.resolveBoolean(option));
                    onTeamConfigUpdate(player, option, config, team, warzone);
                });
            } else if (option.getConfigType() == Integer.class || option.getConfigType() == Double.class || option.getConfigType() == String.class) {
                status += ChatColor.LIGHT_PURPLE + config.resolveValue(option).toString();
                item = new Dye(DyeColor.PURPLE).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, new Runnable() {
                    @Override
                    public void run() {
                        War.war.getUIManager().getPlayerMessage(player, "Type a new value for option " + option.name().toLowerCase() + ": ", new StringRunnable() {
                            @Override
                            public void run() {
                                if (option.getConfigType() == Integer.class) {
                                    config.put(option, Integer.parseInt(this.getValue()));
                                } else if (option.getConfigType() == Double.class) {
                                    config.put(option, Double.parseDouble(this.getValue()));
                                } else {
                                    config.put(option, this.getValue());
                                }
                                onTeamConfigUpdate(player, option, config, team, warzone);
                            }
                        });
                    }
                });
            } else if (option.getConfigType() == FlagReturn.class) {
                status += ChatColor.YELLOW + config.resolveValue(option).toString();
                item = new Dye(DyeColor.PINK).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                    FlagReturn next = FlagReturn.BOTH;
                    FlagReturn[] values = FlagReturn.values();
                    for (int i1 = 0; i1 < values.length; i1++) {
                        FlagReturn flagReturn = values[i1];
                        if (flagReturn == config.resolveFlagReturn() && i1 != values.length - 1) {
                            next = values[i1 + 1];
                            break;
                        }
                    }
                    config.put(option, next);
                    onTeamConfigUpdate(player, option, config, team, warzone);
                });
            } else if (option.getConfigType() == TeamSpawnStyle.class) {
                status += ChatColor.YELLOW + config.resolveValue(option).toString();
                item = new Dye(DyeColor.PINK).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                    TeamSpawnStyle next = TeamSpawnStyle.INVISIBLE;
                    TeamSpawnStyle[] values = TeamSpawnStyle.values();
                    for (int i1 = 0; i1 < values.length; i1++) {
                        TeamSpawnStyle tss = values[i1];
                        if (tss == config.resolveSpawnStyle() && i1 != values.length - 1) {
                            next = values[i1 + 1];
                            break;
                        }
                    }
                    config.put(option, next);
                    onTeamConfigUpdate(player, option, config, team, warzone);
                });
            } else {
                status += ChatColor.RED + config.resolveValue(option).toString();
                item = new ItemStack(Material.COMPASS, 1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                });
            }
        }
        return i;
    }

    private static void onTeamConfigUpdate(Player player, TeamConfig option, TeamConfigBag config, Team team, Warzone warzone) {
        if (team != null) {
            TeamConfigBag.afterUpdate(team, player, option.name() + " set to " + config.resolveValue(option).toString(), false);
        } else if (warzone != null) {
            WarzoneConfigBag.afterUpdate(warzone, player, option.name() + " set to " + config.resolveValue(option).toString(), false);
        } else {
            WarConfigBag.afterUpdate(player, option.name() + " set to " + config.resolveValue(option).toString(), false);
        }
        War.war.getUIManager().assignUI(player, new EditTeamConfigUI(warzone, team));
    }

    static int addWarzoneConfigOptions(final ChestUI ui, final Player player, Inventory inv, final WarzoneConfigBag config, final Warzone warzone, int i) {
        ItemStack item;
        ItemMeta meta;
        for (final WarzoneConfig option : WarzoneConfig.values()) {
            if (option.getTitle() == null) {
                continue;
            }
            String inheritance = "";
            if (!config.contains(option)) {
                inheritance = ChatColor.DARK_GRAY + "Inherited from War config defaults";
            }

            String name = ChatColor.RESET + "" + ChatColor.YELLOW + option.getTitle();
            String status = ChatColor.GRAY + "Currently: ";
            String[] desc = option.getDescription().split("\n");
            for (int j = 0; j < desc.length; j++) {
                desc[j] = ChatColor.RESET + "" + ChatColor.GRAY + desc[j];
            }
            if (option.getConfigType() == Boolean.class) {
                status += config.getBoolean(option) ? ChatColor.GREEN + "true" : ChatColor.DARK_GRAY + "false";
                item = new Dye(config.getBoolean(option) ? DyeColor.LIME : DyeColor.GRAY).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                    config.put(option, !config.getBoolean(option));
                    onWarzoneConfigUpdate(player, option, config, warzone);
                });
            } else if (option.getConfigType() == Integer.class || option.getConfigType() == Double.class || option.getConfigType() == String.class) {
                status += ChatColor.LIGHT_PURPLE + config.getValue(option).toString();
                item = new Dye(DyeColor.PURPLE).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, new Runnable() {
                    @Override
                    public void run() {
                        War.war.getUIManager().getPlayerMessage(player, "Type a new value for option " + option.name().toLowerCase() + ": ", new StringRunnable() {
                            @Override
                            public void run() {
                                if (option.getConfigType() == Integer.class) {
                                    config.put(option, Integer.parseInt(this.getValue()));
                                } else if (option.getConfigType() == Double.class) {
                                    config.put(option, Double.parseDouble(this.getValue()));
                                } else {
                                    config.put(option, this.getValue());
                                }
                                onWarzoneConfigUpdate(player, option, config, warzone);
                            }
                        });
                    }
                });
            } else {
                status += ChatColor.RED + config.getValue(option).toString();
                item = new ItemStack(Material.COMPASS, 1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).add(inheritance).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                });
            }
        }
        return i;
    }

    private static void onWarzoneConfigUpdate(Player player, WarzoneConfig option, WarzoneConfigBag config, Warzone warzone) {
        if (warzone != null) {
            WarzoneConfigBag.afterUpdate(warzone, player, option.name() + " set to " + config.getValue(option).toString(), false);
        } else {
            WarConfigBag.afterUpdate(player, option.name() + " set to " + config.getValue(option).toString(), false);
        }
        War.war.getUIManager().assignUI(player, new EditZoneConfigUI(warzone));
    }

    static int addWarConfigOptions(final ChestUI ui, final Player player, Inventory inv, final WarConfigBag config, int i) {
        ItemStack item;
        ItemMeta meta;
        for (final WarConfig option : WarConfig.values()) {
            if (option.getTitle() == null) {
                continue;
            }

            String name = ChatColor.RESET + "" + ChatColor.YELLOW + option.getTitle();
            String status = ChatColor.GRAY + "Currently: ";
            String[] desc = option.getDescription().split("\n");
            for (int j = 0; j < desc.length; j++) {
                desc[j] = ChatColor.RESET + "" + ChatColor.GRAY + desc[j];
            }
            if (option.getConfigType() == Boolean.class) {
                status += config.getBoolean(option) ? ChatColor.GREEN + "true" : ChatColor.DARK_GRAY + "false";
                item = new Dye(config.getBoolean(option) ? DyeColor.LIME : DyeColor.GRAY).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                    config.put(option, !config.getBoolean(option));
                    onWarConfigUpdate(player, option, config);
                });
            } else if (option.getConfigType() == Integer.class || option.getConfigType() == Double.class || option.getConfigType() == String.class) {
                status += ChatColor.LIGHT_PURPLE + config.getValue(option).toString();
                item = new Dye(DyeColor.PURPLE).toItemStack(1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, new Runnable() {
                    @Override
                    public void run() {
                        War.war.getUIManager().getPlayerMessage(player, "Type a new value for option " + option.name().toLowerCase() + ": ", new StringRunnable() {
                            @Override
                            public void run() {
                                if (option.getConfigType() == Integer.class) {
                                    config.put(option, Integer.parseInt(this.getValue()));
                                } else if (option.getConfigType() == Double.class) {
                                    config.put(option, Double.parseDouble(this.getValue()));
                                } else {
                                    config.put(option, this.getValue());
                                }
                                onWarConfigUpdate(player, option, config);
                            }
                        });
                    }
                });
            } else {
                status += ChatColor.RED + config.getValue(option).toString();
                item = new ItemStack(Material.COMPASS, 1);
                meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(new ImmutableList.Builder<String>().add(desc).add(status).build());
                item.setItemMeta(meta);
                ui.addItem(inv, i++, item, () -> {
                });
            }
        }
        return i;
    }

    private static void onWarConfigUpdate(Player player, WarConfig option, WarConfigBag config) {
        WarConfigBag.afterUpdate(player, option.name() + " set to " + config.getValue(option).toString(), false);
        War.war.getUIManager().assignUI(player, new WarAdminUI());
    }
}
