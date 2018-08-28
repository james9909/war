package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.WarzoneConfig;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

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
            Team team = warzone.getTeamByKind(kind);
            if (team != null) {
                String title = kind.getColor() + "Team " + kind.getCapsName();
                List<String> lore = Arrays.asList(
                    String.format(ChatColor.GRAY + "%d/%d players", team.getPlayers().size(), team.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE)),
                    String.format(ChatColor.GRAY + "%d/%d pts", team.getPoints(), team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)),
                    String.format(ChatColor.GRAY + "%d lives left", team.getRemainingLives()),
                    ChatColor.DARK_GRAY + "Click to join team"
                );
                ItemStack item = createItem(kind.getBlockData().getItemType(), title, lore);

                this.addItem(inv, formatter.next(), item, () -> {
                    if (warzone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
                        War.war.badMsg(player, "join.disabled");
                    } else if (warzone.isReinitializing()) {
                        War.war.badMsg(player, "join.disabled");
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
                            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
                            Team previousTeam = warPlayer.getTeam();
                            if (previousTeam != null) {
                                if (previousTeam == team1) {
                                    War.war.badMsg(player, "join.selfteam");
                                    return;
                                }
                                previousTeam.removePlayer(warPlayer);
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
