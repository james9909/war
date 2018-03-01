package com.tommytony.war.command.zonemaker;

import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;

public class SetRewardsCommand extends AbstractZoneMakerCommand {

    public SetRewardsCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        return false;
    }
}
