package com.tommytony.war.command;

import com.tommytony.war.War;
import com.tommytony.war.command.admin.LoadWarCommand;
import com.tommytony.war.command.admin.NotWarAdminException;
import com.tommytony.war.command.admin.UnloadWarCommand;
import com.tommytony.war.command.regular.AbstractWarCommand;
import com.tommytony.war.command.regular.ChooseClassCommand;
import com.tommytony.war.command.regular.JoinCommand;
import com.tommytony.war.command.regular.LeaveCommand;
import com.tommytony.war.command.regular.TeamCommand;
import com.tommytony.war.command.regular.TeamsCommand;
import com.tommytony.war.command.regular.WarzonesCommand;
import com.tommytony.war.command.zonemaker.ClassChestCommand;
import com.tommytony.war.command.zonemaker.DeleteBombCommand;
import com.tommytony.war.command.zonemaker.DeleteCakeCommand;
import com.tommytony.war.command.zonemaker.DeleteCapturePointCommand;
import com.tommytony.war.command.zonemaker.DeleteMonumentCommand;
import com.tommytony.war.command.zonemaker.DeletePortalCommand;
import com.tommytony.war.command.zonemaker.DeleteTeamCommand;
import com.tommytony.war.command.zonemaker.DeleteTeamFlagCommand;
import com.tommytony.war.command.zonemaker.DeleteZoneCommand;
import com.tommytony.war.command.zonemaker.NextBattleCommand;
import com.tommytony.war.command.zonemaker.NotZoneMakerException;
import com.tommytony.war.command.zonemaker.RenameZoneCommand;
import com.tommytony.war.command.zonemaker.ResetZoneCommand;
import com.tommytony.war.command.zonemaker.SaveZoneCommand;
import com.tommytony.war.command.zonemaker.SetBombCommand;
import com.tommytony.war.command.zonemaker.SetCakeCommand;
import com.tommytony.war.command.zonemaker.SetCapturePointCommand;
import com.tommytony.war.command.zonemaker.SetMonumentCommand;
import com.tommytony.war.command.zonemaker.SetTeamCommand;
import com.tommytony.war.command.zonemaker.SetTeamConfigCommand;
import com.tommytony.war.command.zonemaker.SetTeamFlagCommand;
import com.tommytony.war.command.zonemaker.SetWarConfigCommand;
import com.tommytony.war.command.zonemaker.SetZoneCommand;
import com.tommytony.war.command.zonemaker.SetZoneConfigCommand;
import com.tommytony.war.command.zonemaker.SetZonePortalCommand;
import com.tommytony.war.command.zonemaker.ZoneMakerCommand;
import com.tommytony.war.ui.WarUI;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * Handles commands received by War
 *
 * @author Tim Düsterhus
 * @package bukkit.tommytony.war
 */
public class WarCommandHandler {

    /**
     * Handles a command
     *
     * @param sender The sender of the command
     * @param cmd The command
     * @param args The arguments
     * @return Success
     */
    public boolean handle(CommandSender sender, Command cmd, String[] args) {
        String command = cmd.getName();
        String[] arguments = null;

        // parse prefixed commands
        if ((command.equals("war") || command.equals("War")) && args.length > 0) {
            command = args[0];
            arguments = new String[args.length - 1];
            for (int i = 1; i <= arguments.length; i++) {
                arguments[i - 1] = args[i];
            }

            if (arguments.length == 1 && (arguments[0].equals("help") || arguments[0].equals("h"))) {
                // show /war help
                War.war.badMsg(sender, cmd.getUsage());
                return true;
            }
        } else if (command.equals("war") || command.equals("War")) {
            if (sender instanceof Player) {
                War.war.getUIManager().assignUI((Player) sender, new WarUI());
            } else {
                War.war.badMsg(sender, "Use /war help for information.");
            }
            return true;
        } else {
            arguments = args;
        }

        AbstractWarCommand commandObj = null;
        try {
            switch (command) {
                case "zones":
                case "warzones":
                    commandObj = new WarzonesCommand(this, sender, arguments);
                    break;
                case "teams":
                    commandObj = new TeamsCommand(this, sender, arguments);
                    break;
                case "join":
                    commandObj = new JoinCommand(this, sender, arguments);
                    break;
                case "leave":
                    commandObj = new LeaveCommand(this, sender, arguments);
                    break;
                case "class":
                    commandObj = new ChooseClassCommand(this, sender, arguments);
                    break;
                case "team":
                    commandObj = new TeamCommand(this, sender, arguments);
                    break;
                case "setzone":
                    commandObj = new SetZoneCommand(this, sender, arguments);
                    break;
                case "deletezone":
                    commandObj = new DeleteZoneCommand(this, sender, arguments);
                    break;
                case "savezone":
                    commandObj = new SaveZoneCommand(this, sender, arguments);
                    break;
                case "resetzone":
                    commandObj = new ResetZoneCommand(this, sender, arguments);
                    break;
                case "nextbattle":
                    commandObj = new NextBattleCommand(this, sender, arguments);
                    break;
                case "renamezone":
                    commandObj = new RenameZoneCommand(this, sender, arguments);
                    break;
                case "setteam":
                    commandObj = new SetTeamCommand(this, sender, arguments);
                    break;
                case "deleteteam":
                    commandObj = new DeleteTeamCommand(this, sender, arguments);
                    break;
                case "setteamflag":
                    commandObj = new SetTeamFlagCommand(this, sender, arguments);
                    break;
                case "deleteteamflag":
                    commandObj = new DeleteTeamFlagCommand(this, sender, arguments);
                    break;
                case "setmonument":
                    commandObj = new SetMonumentCommand(this, sender, arguments);
                    break;
                case "deletemonument":
                    commandObj = new DeleteMonumentCommand(this, sender, arguments);
                    break;
                case "setcapturepoint":
                    commandObj = new SetCapturePointCommand(this, sender, arguments);
                    break;
                case "deletecapturepoint":
                    commandObj = new DeleteCapturePointCommand(this, sender, arguments);
                    break;
                case "setbomb":
                    commandObj = new SetBombCommand(this, sender, arguments);
                    break;
                case "deletebomb":
                    commandObj = new DeleteBombCommand(this, sender, arguments);
                    break;
                case "setcake":
                    commandObj = new SetCakeCommand(this, sender, arguments);
                    break;
                case "deletecake":
                    commandObj = new DeleteCakeCommand(this, sender, arguments);
                    break;
                case "setteamconfig":
                case "teamcfg":
                    commandObj = new SetTeamConfigCommand(this, sender, arguments);
                    break;
                case "setzoneconfig":
                case "zonecfg":
                    commandObj = new SetZoneConfigCommand(this, sender, arguments);
                    break;
                case "loadwar":
                    commandObj = new LoadWarCommand(this, sender, arguments);
                    break;
                case "unloadwar":
                    commandObj = new UnloadWarCommand(this, sender, arguments);
                    break;
                case "setwarconfig":
                case "warcfg":
                    commandObj = new SetWarConfigCommand(this, sender, arguments);
                    break;
                case "zonemaker":
                case "zm":
                    commandObj = new ZoneMakerCommand(this, sender, arguments);
                    break;
                case "classchest":
                    commandObj = new ClassChestCommand(this, sender, args);
                    break;
                case "setportal":
                    commandObj = new SetZonePortalCommand(this, sender, args);
                    break;
                case "deleteportal":
                    commandObj = new DeletePortalCommand(this, sender, args);
                    break;
            }
            // we are not responsible for any other command
        } catch (NotWarAdminException e) {
            War.war.badMsg(sender, "war.notadmin");
        } catch (NotZoneMakerException e) {
            War.war.badMsg(sender, "war.notzm");
        } catch (Exception e) {
            War.war.log("An error occured while handling command " + cmd.getName() + ". Exception:" + e.getClass().toString() + " " + e.getMessage(), Level.WARNING);
            e.printStackTrace();
        }

        if (commandObj != null) {
            boolean handled = commandObj.handle();
            if (!handled) {
                War.war.badMsg(sender, cmd.getUsage());
            }
        }

        return true;
    }
}
