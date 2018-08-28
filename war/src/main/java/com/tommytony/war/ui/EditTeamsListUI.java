package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.TeamKind;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

/**
 * Created by Connor on 7/25/2017.
 */
public class EditTeamsListUI extends ChestUI {

    private final Warzone warzone;

    public EditTeamsListUI(Warzone warzone) {
        super();
        this.warzone = warzone;
    }

    @Override
    public void build(final Player player, Inventory inv) {
        int i = 0;
        for (final TeamKind kind : TeamKind.values()) {
            ItemStack item = kind.getBlockHead();
            final Team team = warzone.getTeamByKind(kind);
            ItemMeta meta = item.getItemMeta();
            if (team == null) {
                meta.setDisplayName("Create new team");
                item.setItemMeta(meta);
                this.addItem(inv, i, item, () -> {
                    if (!warzone.getVolume().contains(player.getLocation())) {
                        player.sendTitle("", ChatColor.RED + "Can't add a spawn outside of the zone!", 10, 20, 10);
                        return;
                    }

                    Team newTeam = new Team(kind.toString(), kind, Collections.<Location>emptyList(), warzone);
                    newTeam.setRemainingLives(newTeam.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL));
                    warzone.getTeams().add(newTeam);
                    newTeam.addTeamSpawn(player.getLocation());
                    player.sendTitle("", "Team " + newTeam.getName() + " created with spawn here.", 10, 20, 10);
                });
            } else {
                meta.setDisplayName("Edit team " + kind.getColor() + kind.name().toLowerCase());
                item.setItemMeta(meta);
                this.addItem(inv, i, item, () -> War.war.getUIManager().assignUI(player, new EditTeamUI(team)));
            }
            i++;
            if (i == 9) {
                i++;
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
