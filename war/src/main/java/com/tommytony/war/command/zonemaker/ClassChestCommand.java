package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.config.WarConfigBag;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassChestCommand extends AbstractZoneMakerCommand {

    public ClassChestCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (args.length != 2) {
            return false;
        }
        CommandSender sender = this.getSender();
        if (!(sender instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        String action = args[0];
        String loadoutName = args[1];
        Player player = (Player) sender;
        switch (action) {
            case "set":
                BlockState state = player.getTargetBlock(null, 10).getState();
                if (!(state instanceof Chest)) {
                    this.badMsg("classchest.notachest");
                    return true;
                }
                War.war.getDefaultInventories().addLoadout(loadoutName, (Chest) state);
                WarConfigBag.afterUpdate(player, loadoutName + " set", false);
                this.msg("classchest.set");
                break;
            case "remove":
                if (!War.war.getDefaultInventories().containsLoadout(loadoutName)) {
                    this.msg("classchest.notfound");
                    return true;
                }
                War.war.getDefaultInventories().removeLoadout(loadoutName);
                WarConfigBag.afterUpdate(player, loadoutName + " removed", false);
                this.msg("classchest.removed");
                break;
            default:
                return false;
        }
        return true;
    }
}
