package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import com.tommytony.war.volume.Volume;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Connor on 7/27/2017.
 */
class EditTeamUI extends ChestUI {

    private final Team team;

    EditTeamUI(Team team) {
        super();
        this.team = team;
    }

    @Override
    public void build(final Player player, Inventory inv) {
        int i = 0;
        ItemStack item = createItem(Material.GOLDEN_SHOVEL, ChatColor.GREEN + "Add additional spawn", null);
        this.addItem(inv, i++, item, () -> {
            if (team.getZone().getVolume().contains(player.getLocation())) {
                team.addTeamSpawn(player.getLocation());
                player.sendTitle("", "Additional spawn added", 10, 20, 10);
            } else {
                player.sendTitle("", ChatColor.RED + "Can't add a spawn outside of the zone!", 10, 20, 10);
            }
        });

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Rewards", null);
        this.addItem(inv, i++, item, () -> War.war.getUIManager().assignUI(player, new EditRewardsListUI(null, team)));

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Loadouts", null);
        this.addItem(inv, i++, item, () -> War.war.getUIManager().assignUI(player, new EditLoadoutsListUI(null, team)));

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit options", null);
        this.addItem(inv, i++, item, () -> War.war.getUIManager().assignUI(player, new EditTeamConfigUI(null, team)));

        item = createDeleteItem();
        this.addItem(inv, getSize() - 1, item, () -> {
            if (team.getFlagVolume() != null) {
                team.getFlagVolume().resetBlocks();
            }
            for (Volume spawnVolume : team.getSpawnVolumes().values()) {
                spawnVolume.resetBlocks();
            }
            final Warzone zone = team.getZone();
            zone.getTeams().remove(team);
            WarzoneYmlMapper.save(zone);
            War.war.msg(player, "Team " + team.getName() + " removed.");
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Warzone \"" + team.getZone().getName() + "\": Team \"" + team.getName() + "\"";
    }

    @Override
    public int getSize() {
        return 9 * 3;
    }
}
