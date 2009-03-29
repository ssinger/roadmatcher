package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class ShiftAdjustmentMethod extends SimpleAdjustmentMethod {

    public LineString adjust(LineString line, SourceRoadSegment segment, Terminal terminal,
            Coordinate newTerminalLocation, LayerViewPanel panel) {
        Coordinate displacement = CoordUtil.subtract(newTerminalLocation,
                terminal.coordinate(line));
        Coordinate[] newCoordinates = new Coordinate[line.getNumPoints()];
        for (int i = 0; i < line.getNumPoints(); i++) {
            newCoordinates[i] = CoordUtil.add(line.getCoordinateN(i),
                    displacement);
        }
        //Set the terminal coordinate exactly [Jon Aquino 12/16/2003]
        newCoordinates[terminal.index(line)] = (Coordinate) newTerminalLocation
                .clone();
        return line.getFactory().createLineString(newCoordinates);
    }

}
