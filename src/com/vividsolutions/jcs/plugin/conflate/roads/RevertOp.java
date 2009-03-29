package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.Collections;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.jump.FUTURE_CompositeUndoableCommand;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class RevertOp {

	public static UndoableCommand createUndoableCommand(
			SourceRoadSegment roadSegment, String name, LayerViewPanel panel,
			ToolboxModel toolboxModel, boolean full) {
		return full ? unadjustAndUncommit(roadSegment, name, panel,
				toolboxModel) : roadSegment.isAdjusted() ? unadjust(
				roadSegment, name, panel, toolboxModel.getContext())
				: uncommit(roadSegment, name, panel, toolboxModel);
	}

	private static UndoableCommand unadjustAndUncommit(
			SourceRoadSegment roadSegment, String name, LayerViewPanel panel,
			ToolboxModel toolboxModel) {
		return new FUTURE_CompositeUndoableCommand(name).add(
				unadjust(roadSegment, name, panel, toolboxModel.getContext()))
				.add(uncommit(roadSegment, name, panel, toolboxModel));
	}

	private static UndoableCommand uncommit(SourceRoadSegment roadSegment,
			String name, LayerViewPanel panel, ToolboxModel toolboxModel) {
		return SetUnmatchedStateTool.createUndoableCommand(name,
				SourceState.UNKNOWN, Collections.singletonMap(toolboxModel
						.getSourceLayer(roadSegment.getNetworkID()),
						Collections.singletonList(roadSegment.getFeature())),
				toolboxModel, panel.getContext());
	}

	private static UndoableCommand unadjust(SourceRoadSegment roadSegment,
			String name, LayerViewPanel panel, WorkbenchContext context) {
		return AdjustEndpointOperation.createUndoableCommand(name, roadSegment,
				roadSegment.getApparentLine(), roadSegment.getLine(), panel
						.getLayerManager(), context);
	}

	public static boolean revertable(SourceRoadSegment roadSegment) {
		return roadSegment.isAdjusted()
				|| roadSegment.getState() != SourceState.UNKNOWN;
	}

}