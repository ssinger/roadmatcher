package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Cursor;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public abstract class SimpleAdjustmentMethod {
    public static abstract class Terminal {
        private Terminal() {}
        public static Terminal START = new Terminal() {
            public int index(LineString line) {
                return 0;
            }
            public Terminal other() { return END; }
        };

        public static Terminal END = new Terminal() {
            public int index(LineString line) {
                return line.getNumPoints() - 1;
            }
            public Terminal other() { return START; }
        };
        public Coordinate coordinate(LineString line) {
            return line.getCoordinateN(index(line));
        }
        public abstract Terminal other();
        public abstract int index(LineString line);             
    }
    
    /**
     * Don't simply modify the Coordinate, as it may be being used by a Node.
     */
    public abstract LineString adjust(LineString line, SourceRoadSegment segment, Terminal terminal,
            Coordinate newTerminalLocation, LayerViewPanel panel);
    
    public Cursor getCursor() {
        return null;
    }
}
