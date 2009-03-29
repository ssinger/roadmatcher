package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.Serializable;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

import com.vividsolutions.jump.warp.AffineTransform;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class WarpAdjustmentMethod extends SimpleAdjustmentMethod implements Serializable {
	public LineString adjust(LineString line, SourceRoadSegment segment, Terminal terminal,
			Coordinate newTerminalLocation, LayerViewPanel panel) {
		Coordinate[] newCoordinates = new Coordinate[line.getNumPoints()];
		//Handle cul-de-sacs [Jon Aquino 2004-07-28]
		AffineTransform warp = FUTURE_LineString.first(line).equals(
				FUTURE_LineString.last(line)) ? new AffineTransform(terminal
				.coordinate(line), newTerminalLocation) : new AffineTransform(
				terminal.other().coordinate(line), terminal.other().coordinate(
						line), terminal.coordinate(line), newTerminalLocation);
		for (int i = 0; i < line.getNumPoints(); i++) {
			//Set the terminal coordinates exactly [Jon Aquino 12/16/2003]
			newCoordinates[i] = i == terminal.index(line) ? (Coordinate) newTerminalLocation
					.clone()
					: i == terminal.other().index(line) ? (Coordinate) line
							.getCoordinateN(i).clone() : warp.transform(line
							.getCoordinateN(i));
		}
		//Handle cul-de-sacs [Jon Aquino 2004-07-28]
		if (FUTURE_LineString.first(line).equals(FUTURE_LineString.last(line))) {
			newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
		}
		return line.getFactory().createLineString(newCoordinates);
	}

}