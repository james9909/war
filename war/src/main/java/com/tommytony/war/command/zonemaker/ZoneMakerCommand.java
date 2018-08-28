package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.command.regular.AbstractWarCommand;
import com.tommytony.war.mapper.WarYmlMapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Makes a player zonemaker and other way round.
 *
 * @author Tim Düsterhus
 */
public class ZoneMakerCommand extends AbstractWarCommand {

    public ZoneMakerCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);

        if (!(sender instanceof Player)) {
            return;
        }
        if (!War.war.isZoneMaker((Player) sender)) {
            throw new NotZoneMakerException();
        }
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("You can't do this if you are not in-game.");
            return true;
        }
        Player player = (Player) this.getSender();

        if (War.war.isZoneMaker(player)) {
            if (this.args.length == 0) {
                War.war.getZoneMakersImpersonatingPlayers().add(player.getUniqueId());
                this.msg("You are now impersonating a regular player. Type /zonemaker again to toggle back to war maker mode.");
            } else if (this.args.length == 1) {
                // make someone zonemaker or remove the right
                OfflinePlayer op = Bukkit.getOfflinePlayer(this.args[0]);
                if (op == null) {
                    this.msg("That player has never played before.");
                    return true;
                }
                Player target = op.getPlayer();

                if (War.war.getZoneMakerNames().contains(op.getName())) {
                    // kick
                    War.war.getZoneMakerNames().remove(op.getName());
                    this.msg(this.args[0] + " is not a zone maker anymore.");
                    if (target != null) {
                        War.war.msg(target, player.getName() + " took away your warzone maker privileges.");
                    }
                    War.war.log(player.getName() + " took away zonemaker rights from " + op.getName(), Level.INFO);
                } else {
                    // add
                    War.war.getZoneMakerNames().add(op.getName());
                    this.msg(op.getName() + " is now a zone maker.");
                    if (target != null) {
                        War.war.msg(target, player.getName() + " made you warzone maker.");
                        War.war.log(player.getName() + " made " + target.getName() + " a zonemaker", Level.INFO);
                    }
                }
            } else {
                return false;
            }
        } else {
            if (this.args.length != 0) {
                return false;
            }

            War.war.getZoneMakersImpersonatingPlayers().remove(player.getUniqueId());
            this.msg("You are back as a zone maker.");
            WarYmlMapper.save();
        }

        return true;
    }
}
