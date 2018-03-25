package com.tommytony.war.runnable;

import com.tommytony.war.volume.Volume;

public class BlockResetJob implements Runnable {

    private final Volume volume;

    public BlockResetJob(Volume volume) {
        this.volume = volume;
    }

    public void run() {
        this.volume.resetBlocks();
    }

}
