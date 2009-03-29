package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Collections;

import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jcs.jump.FUTURE_LineMerger;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;

public class DeleteSplitNodeTool extends SpecifyClosestRoadFeatureTool {
	public DeleteSplitNodeTool(boolean layer0, boolean layer1,
			String cursorImage, String buttonImage, Color color,
			WorkbenchContext context) {
		super(layer0, layer1, cursorImage, buttonImage, color, context,
				GestureMode.POINT);
	}

	protected void gestureFinished(SourceFeature feature, Layer layer)
			throws Exception {
		execute(new DeleteSplitNodeOp().createUndoableCommand(feature, layer,
				toolboxModel(), getPanel().getContext(), getName()));
	}

	protected boolean includeInProximitySearch(SourceFeature feature,
			Point clickPoint) {
		if (!DeleteSplitNodeOp.deletable(feature.getRoadSegment(), false,
				new Block() {
					public Object yield() {
						setNoRoadSegmentsWarning(ErrorMessages.deleteSplitNodeTool_adjusted);
						return null;
					}
				})) {
			return false;
		}
		if (feature.getRoadSegment().getStartNode().getCoordinate().distance(
				getModelDestination()) > feature.getRoadSegment().getEndNode()
				.getCoordinate().distance(getModelDestination())) {
			return false;
		}
		try {
			if (!getBoxInModelCoordinates().contains(
					feature.getRoadSegment().getStartNode().getCoordinate())) {
				return false;
			}
		} catch (NoninvertibleTransformException e) {
			return false;
		}
		return true;
	}

	protected String getDefaultNoRoadSegmentsWarning() {
		return FUTURE_StringUtil.substitute(
				ErrorMessages.deleteSplitNodeTool_noRoadSegments,
				new Object[] { sourceLayerDescription() });
	}
}