package com.tommytony.war.runnable;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class DeferredBlockResetsJob implements Runnable {

    List<BlockState> deferred = new ArrayList<>();

    public DeferredBlockResetsJob() {
    }

    public void add(BlockState pleaseResetLater) {
        this.deferred.add(pleaseResetLater);
    }

    public boolean isEmpty() {
        return this.deferred.isEmpty();
    }

    public void run() {
        for (BlockState reset : this.deferred) {
            reset.update(true, false);
            for (Entity ent : reset.getWorld().getEntities()) {
                if (ent instanceof Item && ent.getLocation().distance(reset.getLocation()) < 2) {
                    ent.remove();
                }
            }
        }
    }
}
