package com.tommytony.war.command.zonemaker;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.command.ZoneSetter;
import org.bukkit.Location;
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
            if (War.war.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                WorldEditPlugin worldEdit = (WorldEditPlugin) War.war.getServer().getPluginManager().getPlugin("WorldEdit");
                if (worldEdit == null) {
                    return true;
                }
                try {
                    Region selection = worldEdit.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
                    if (selection instanceof CuboidRegion) {
                        BlockVector3 minBlockVector = selection.getMinimumPoint();
                        BlockVector3 maxBlockVector = selection.getMaximumPoint();
                        Location min = new Location(player.getWorld(), minBlockVector.getBlockX(), minBlockVector.getBlockY(), minBlockVector.getBlockZ());
                        Location max = new Location(player.getWorld(), maxBlockVector.getBlockX(), maxBlockVector.getBlockY(), maxBlockVector.getBlockZ());
                        ZoneSetter setter = new ZoneSetter(player, this.args[0]);
                        setter.placeCorner1(min.getBlock());
                        setter.placeCorner2(max.getBlock());
                        return true;
                    }
                } catch (IncompleteRegionException ignored) { }
            }
            War.war.addWandBearer(player, this.args[0]);
        } else if (this.args.length == 2) {
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
        } else {
            return false;
        }

        return true;
    }
}
