package com.tommytony.war.command.admin;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarYmlMapper;
import com.tommytony.war.structure.WarHub;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Places the warhub
 *
 * @author Tim Düsterhus
 */
public class SetWarHubCommand extends AbstractWarAdminCommand {

    public SetWarHubCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotWarAdminException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        if (this.args.length != 0) {
            return false;
        }
        Player player = (Player) this.getSender();

        if (War.war.getWarzones().size() > 0) {
            if (War.war.getWarHub() != null) {
                // reset existing hub
                War.war.getWarHub().getVolume().resetBlocks();
                War.war.getWarHub().setLocation(player.getLocation());
                War.war.getWarHub().initialize();
                this.msg("War hub moved.");
                War.war.log(this.getSender().getName() + " moved the warhub", Level.INFO);
            } else {
                War.war.setWarHub(new WarHub(player.getLocation()));
                War.war.getWarHub().initialize();
                this.msg("War hub created.");
                War.war.log(this.getSender().getName() + " created the warhub", Level.INFO);
            }
            WarYmlMapper.save();
        } else {
            this.msg("No warzones yet.");
        }

        return true;
    }
}
