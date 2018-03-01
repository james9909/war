package com.tommytony.war.command.regular;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.utility.Loadout;
import com.tommytony.war.utility.LoadoutSelection;
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
            return true;
        }
        Team team = zone.getPlayerTeam(player.getName());
        if (team == null) {
            return true;
        }
        LoadoutSelection loadoutSelection = zone.getLoadoutSelections().get(player.getName());
        if (loadoutSelection == null) {
            return true;
        }

        if (loadoutSelection.isStillInSpawn()) {
            String loadoutName = args[0];
            Loadout loadout = War.war.getDefaultInventories().getLoadout(loadoutName);
            if (loadout == null) {
                this.badMsg("zone.class.notfound");
                return true;
            }
            player.getInventory().clear();
            loadout.giveItems(player);
            this.msg("zone.class.equip", loadoutName);
        } else {
            this.badMsg("zone.class.reenter");
        }
        return true;
    }
}
