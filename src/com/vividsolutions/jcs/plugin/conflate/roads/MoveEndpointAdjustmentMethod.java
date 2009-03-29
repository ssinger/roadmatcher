package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class MoveEndpointAdjustmentMethod extends SimpleAdjustmentMethod {

	public LineString adjust(LineString line, SourceRoadSegment segment,
			Terminal terminal, Coordinate newTerminalLocation,
			LayerViewPanel panel) {
		LineString newLine = (LineString) line.clone();
		terminal.coordinate(newLine).setCoordinate(newTerminalLocation);
        newLine.geometryChanged();
		return newLine;
	}

}