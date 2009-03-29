package com.vividsolutions.jcs.jump;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.LayerManager;

public class FUTURE_LayerManager {
    public static Block createBlockToDisableEventsTemporarily(final LayerManager layerManager, final Block block) {
        return new Block() {
            public Object yield() {
                boolean originallyFiringEvents = layerManager.isFiringEvents();
                try {
                    layerManager.setFiringEvents(false);
                    block.yield();
                }
                finally {
                    layerManager.setFiringEvents(originallyFiringEvents);
                }
                return null;
            }
        };
    }
}
