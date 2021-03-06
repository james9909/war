package com.tommytony.war.command.regular;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

/**
 * Lists all warzones
 *
 * @author Tim Düsterhus
 */
public class WarzonesCommand extends AbstractWarCommand {

    public WarzonesCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (this.args.length != 0) {
            return false;
        }
        StringBuilder warzonesMessage = new StringBuilder(War.war.getString("zone.zoneinfo.prefix"));
        if (War.war.getWarzones().isEmpty()) {
            warzonesMessage.append(War.war.getString("zone.teaminfo.none"));
        } else {
            for (Warzone warzone : War.war.getWarzones()) {
                warzonesMessage.append('\n');
                warzonesMessage.append(MessageFormat.format(War.war.getString("zone.zoneinfo.format"), warzone.getName(), warzone.getTeams().size(), warzone.getPlayerCount()));
            }
        }

        if (this.getSender() instanceof Player) {
            warzonesMessage.append("\n").append(War.war.getString("zone.zoneinfo.teleport"));
        }
        this.msg(warzonesMessage.toString());

        return true;
    }
}
