package com.tommytony.war.spells;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.tommytony.war.Team;
import com.tommytony.war.WarPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class StealthTNT extends InstantSpell {

    private int radius;

    public StealthTNT(MagicConfig config, String spellName) {
        super(config, spellName);
        this.radius = getConfigInt("radius", 3);
    }

    @Override
    public PostCastAction castSpell(Player player, SpellCastState state, float v, String[] args) {
        if (state != SpellCastState.NORMAL) {
            return PostCastAction.HANDLE_NORMALLY;
        }

        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team playerTeam = warPlayer.getTeam();

        Location center = player.getLocation();
        List<Player> targets = new ArrayList<>();
        for (WarPlayer target : warPlayer.getZone().getPlayers()) {
            Team targetTeam = target.getTeam();
            if (playerTeam == targetTeam && !warPlayer.getUniqueId().equals(target.getUniqueId())) {
                // Don't affect teammates, but affect the caster
                continue;
            }
            targets.add(target.getPlayer());
        }

        if (targets.size() == 0) {
            // Minor optimization
            return PostCastAction.HANDLE_NORMALLY;
        }

        for (int x = center.getBlockX() - this.radius; x <= center.getBlockX() + this.radius; x++) {
            for (int y = center.getBlockY() - this.radius; y <= center.getBlockY() + this.radius; y++) {
                for (int z = center.getBlockZ() - this.radius; z <= center.getBlockZ() + this.radius; z++) {
                   Block target = center.getWorld().getBlockAt(x, y, z);
                   if (!target.getType().equals(Material.TNT)) {
                       continue;
                   }

                   MaterialData mdata = new MaterialData(this.getDisguiseMaterial(target));
                   for (Player p : targets) {
                       p.sendBlockChange(target.getLocation(), mdata.getItemType(), mdata.getData());
                   }
                }
            }
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    private Material getDisguiseMaterial(Block block) {
        Location loc = block.getLocation();
        for (int y = loc.getBlockY()-1; y > 0; y--) {
            loc.setY(y);
            Material material = loc.getBlock().getType();
            if (material.isSolid() && !material.equals(Material.TNT)) {
                return material;
            }
        }
        return Material.STONE;
    }
}
