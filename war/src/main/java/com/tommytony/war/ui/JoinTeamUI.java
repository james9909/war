package com.tommytony.war.ui;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.WarzoneConfig;
import java.text.MessageFormat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Connor on 7/25/2017.
 */
public class JoinTeamUI extends ChestUI {

    private final Warzone warzone;

    public JoinTeamUI(Warzone warzone) {
        super();
        this.warzone = warzone;
    }

    @Override
    public void build(final Player player, Inventory inv) {
        UIFormatter formatter = new UIFormatter(warzone.getTeams().size());
        for (final TeamKind kind : TeamKind.values()) {
            ItemStack item = kind.getBlockHead();
            Team team = warzone.getTeamByKind(kind);
            ItemMeta meta = item.getItemMeta();
            if (team != null) {
                meta.setDisplayName(kind.getColor() + "Team " + kind.getCapsName());
                meta.setLore(ImmutableList.of(MessageFormat.format(ChatColor.GRAY + "{0}/{1} players", team.getPlayers().size(), team.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE)),
                    MessageFormat.format(ChatColor.GRAY + "{0}/{1} pts", team.getPoints(), team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)),
                    MessageFormat.format(ChatColor.GRAY + "{0} lives left", team.getRemainingLives()), ChatColor.DARK_GRAY + "Click to join team"));

                item.setItemMeta(meta);
                this.addItem(inv, formatter.next(), item, () -> {
                    if (warzone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
                        War.war.badMsg(player, "join.disabled");
                    } else if (warzone.isReinitializing()) {
                        War.war.badMsg(player, "join.disabled");
                    } else if (warzone.getWarzoneConfig().getBoolean(WarzoneConfig.AUTOASSIGN)) {
                        War.war.badMsg(player, "join.aarequired");
                    } else if (!warzone.getWarzoneConfig().getBoolean(WarzoneConfig.JOINMIDBATTLE) && warzone.isEnoughPlayers()) {
                        War.war.badMsg(player, "join.progress");
                    } else {
                        Team team1 = warzone.getTeamByKind(kind);
                        if (team1 == null) {
                            War.war.badMsg(player, "join.team404");
                        } else if (!War.war.canPlayWar(player, team1)) {
                            War.war.badMsg(player, "join.permission.single");
                        } else if (team1.isFull()) {
                            War.war.badMsg(player, "join.full.single", team1.getName());
                        } else {
                            Team previousTeam = Team.getTeamByPlayerName(player.getName());
                            if (previousTeam != null) {
                                if (previousTeam == team1) {
                                    War.war.badMsg(player, "join.selfteam");
                                    return;
                                }
                                previousTeam.removePlayer(player);
                                previousTeam.resetSign();
                            }
                            warzone.assign(player, team1);
                        }
                    }
                });
            }
        }
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Warzone \"" + warzone.getName() + "\": Teams";
    }

    @Override
    public int getSize() {
        return 18;
    }
}
