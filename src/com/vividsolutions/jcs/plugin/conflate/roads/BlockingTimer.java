package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jts.util.Assert;

public class BlockingTimer {

    public static interface Listener {
        public boolean tick();
    }
    
    public BlockingTimer(int millisecondDelay, Listener listener) {
        this.millisecondDelay = millisecondDelay;
        this.listener = listener;
    }

    private long millisecondsUntilNextEvent() {
        lastEventTime += millisecondDelay;
        //Will return 0 if computer is slow [Jon Aquino 12/9/2003]
        return Math.max(0, lastEventTime - System.currentTimeMillis());
    }

    public void start() {
        lastEventTime = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(millisecondsUntilNextEvent());
            } catch (InterruptedException e) {
                Assert.shouldNeverReachHere();
            }
        }
        while (listener.tick());
    }

    private long lastEventTime;
    private Listener listener;
    private int millisecondDelay;
    
}