package com.tommytony.war.command.regular;

import com.tommytony.war.Team;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.utility.Loadout;
import com.tommytony.war.utility.LoadoutSelection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChooseClassCommand extends AbstractWarCommand {

    public ChooseClassCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (args.length != 1) {
            return false;
        }
        CommandSender sender = this.getSender();
        if (!(sender instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }
        Player player = (Player) sender;

        Warzone zone = getWarzoneByLocation();
        if (zone == null) {
            this.badMsg("You are not in a warzone.");
            return true;
        }

        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team team = warPlayer.getTeam();
        if (team == null) {
            this.badMsg("You are not on a team");
            return true;
        }

        LoadoutSelection loadoutSelection = warPlayer.getLoadoutSelection();
        if (loadoutSelection == null) {
            this.badMsg("No loadout selection");
            return true;
        }

        if (loadoutSelection.isStillInSpawn()) {
            String loadoutName = args[0];
            List<Loadout> loadouts = team.getInventories().resolveLoadouts();

            for (int i = 0; i < loadouts.size(); i++) {
                Loadout loadout = loadouts.get(i);
                if (loadout.getName().equals(loadoutName)) {
                    player.getInventory().clear();
                    loadout.giveItems(player);
                    this.msg("zone.class.equip", loadoutName);
                    loadoutSelection.setSelectedLoadout(i);
                    return true;
                }
            }

            this.badMsg("zone.class.notfound");
            return true;
        } else {
            this.badMsg("zone.class.reenter");
        }
        return true;
    }
}
