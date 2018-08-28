package com.tommytony.war.structure;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.utility.Direction;
import com.tommytony.war.volume.Volume;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.text.MessageFormat;

public class ZonePortal {

    private String name;
    private Warzone zone;
    private Location location;
    private Volume volume;

    private Block base;
    private BlockFace orientation;

    private BlockFace left;
    private BlockFace back;

    public ZonePortal(String name, Warzone zone, Location location) {
        this.name = name;
        this.zone = zone;
        this.location = location;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        base = zone.getWorld().getBlockAt(x, y, z);

        this.orientation = getOrientation();
        this.setDirections();
        this.initialize();
    }

    public void initialize() {
        // Setup volume
        if (volume == null) {
            volume = new Volume(String.format("portal-%s", name), zone.getWorld());
        }
        volume.setCornerOne(base.getRelative(left).getRelative(back));
        volume.setCornerTwo(base.getRelative(left.getOppositeFace()).getRelative(BlockFace.UP, 2));
        volume.saveBlocks();

        this.reset();
    }

    public Warzone getZone() {
        return zone;
    }

    public Location getLocation() {
        return location;
    }

    private BlockFace getOrientation() {
        float yaw = location.getYaw();
        if ((yaw >= 0 && yaw < 45) || (yaw >= 315 && yaw <= 360)) {
            return Direction.WEST();
        } else if (yaw >= 45 && yaw < 135) {
            return Direction.NORTH();
        } else if (yaw >= 135 && yaw < 225) {
            return Direction.EAST();
        } else if (yaw >= 225 && yaw < 315) {
            return Direction.SOUTH();
        }
        return Direction.SOUTH();
    }

    public String getName() {
        return name;
    }

    public Volume getVolume() {
        return volume;
    }

    public void setVolume(Volume volume) {
        this.volume = volume;
    }

    public void reset() {
        this.setDirections();

        this.volume.resetBlocks();

        // Sign
        base.getRelative(BlockFace.UP, 2).getRelative(back, 1).setType(Material.WALL_SIGN);
        org.bukkit.block.Sign block = (org.bukkit.block.Sign) base.getRelative(BlockFace.UP, 2).getRelative(back, 1).getState();
        org.bukkit.material.Sign data = (org.bukkit.material.Sign) block.getData();
        data.setFacingDirection(orientation.getOppositeFace());
        block.setData(data);

        int zoneCap = 0;
        int zonePlayers = 0;
        for (Team t : zone.getTeams()) {
            zonePlayers += t.getPlayers().size();
            zoneCap += t.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE);
        }

        String[] lines = MessageFormat.format(War.war.getString("sign.warzone"), zone.getName(), zonePlayers, zoneCap, zone.getTeams().size()).split("\n");
        for (int i = 0; i < 4; i++) {
            block.setLine(i, lines[i]);
        }
        block.update(true);

        if (zonePlayers > 0) {
            // add redstone blocks and torches to gate if there are players in it (to highlight active zones)
            base.getRelative(BlockFace.UP, 2).getRelative(left).setType(Material.REDSTONE_BLOCK);
            base.getRelative(BlockFace.UP, 2).getRelative(left.getOppositeFace()).setType(Material.REDSTONE_BLOCK);
            base.getRelative(BlockFace.UP, 2).getRelative(left).getRelative(back, 1).setType(Material.AIR);
            base.getRelative(BlockFace.UP, 2).getRelative(left.getOppositeFace()).getRelative(back, 1).setType(Material.AIR);
        }
    }

    private  void setDirections() {
        if (orientation == Direction.SOUTH()) {
            left = Direction.EAST();
            back = Direction.NORTH();
        } else if (orientation == Direction.NORTH()) {
            left = Direction.WEST();
            back = Direction.SOUTH();
        } else if (orientation == Direction.EAST()) {
            left = Direction.NORTH();
            back = Direction.WEST();
        } else {
            left = Direction.SOUTH();
            back = Direction.EAST();
        }
    }
}
