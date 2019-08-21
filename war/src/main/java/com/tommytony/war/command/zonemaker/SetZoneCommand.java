package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.command.ZoneSetter;
import com.tommytony.war.utility.Compat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetZoneCommand extends AbstractZoneMakerCommand {

    public SetZoneCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        Player player = (Player) this.getSender();

        if (this.args.length == 1) {
            Compat.BlockPair pair = Compat.getWorldEditSelection(player);
            if (pair != null) {
                ZoneSetter setter = new ZoneSetter(player, this.args[0]);
                setter.placeCorner1(pair.getBlock1());
                setter.placeCorner2(pair.getBlock2());
                return true;
            }
            War.war.addWandBearer(player, this.args[0]);
        } else {
            // args.length == 2
            if (!this.args[1].equals("southeast") && !this.args[1].equals("northwest") && !this.args[1].equals("se") && !this.args[1].equals("nw") && !this.args[1].equals("corner1") && !this.args[1]
                    .equals("corner2") && !this.args[1].equals("c1") && !this.args[1].equals("c2") && !this.args[1].equals("pos1") && !this.args[1].equals("pos2") && !this.args[1].equals("wand")) {
                return false;
            }

            ZoneSetter setter = new ZoneSetter(player, this.args[0]);
            switch (this.args[1]) {
                case "northwest":
                case "nw":
                    setter.placeNorthwest();
                    break;
                case "southeast":
                case "se":
                    setter.placeSoutheast();
                    break;
                case "corner1":
                case "c1":
                case "pos1":
                    setter.placeCorner1();
                    break;
                case "corner2":
                case "c2":
                case "pos2":
                    setter.placeCorner2();
                    break;
                case "wand":
                    War.war.addWandBearer(player, this.args[0]);
                    break;
            }
        }

        return true;
    }
}
