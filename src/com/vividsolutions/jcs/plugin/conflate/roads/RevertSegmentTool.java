package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

public class RevertSegmentTool extends SpecifyClosestRoadFeatureTool {

	public RevertSegmentTool(WorkbenchContext context) {
		super(true, true, null, "reset-tool-button.png", Color.black, context,
				GestureMode.LINE);
	}

	protected void gestureFinished(SourceFeature feature, Layer layer)
			throws Exception {
		execute(RevertOp.createUndoableCommand(feature.getRoadSegment(), getName(),
				getPanel(), toolboxModel(), false));
	}

	protected boolean includeInProximitySearch(SourceFeature feature,
			Point clickPoint) {
		if (RevertOp.revertable(feature.getRoadSegment())) { return true; }		
		setNoRoadSegmentsWarning(ErrorMessages.revertRoadSegmentTool_alreadyUnknown);
		return false;
	}
}