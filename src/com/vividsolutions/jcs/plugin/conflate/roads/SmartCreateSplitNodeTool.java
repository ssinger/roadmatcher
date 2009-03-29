package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.WorkbenchContext;
public class SmartCreateSplitNodeTool extends CreateSplitNodeTool {
	public SmartCreateSplitNodeTool(boolean layer0, boolean layer1,
			String cursorImage, String buttonImage, Color color,
			WorkbenchContext context) {
		super(layer0, layer1, cursorImage, buttonImage, color, context);
	}
	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		if (!checkConflationSessionStarted(getContext())) {
			return;
		}
		try {
			if (new CreateSplitNodeNearEndpointOp().handleGesture(
					getModelDestination(), get_Big_BoxInModelCoordinates(),
					getBoxInModelCoordinates(), isForLayer0(), isForLayer1(),
					toolboxModel(), getPanel().getContext())) {
				return;
			}
			super.gestureFinished();
		} catch (CreateSplitNodeAtIntersectionOp.RoadSegmentsAdjustedException e) {
			getPanel().getContext().warnUser(
					ErrorMessages.createSplitNodeTool_adjusted);
			return;
		} catch (CreateSplitNodeOp.ShortSegmentException e) {
			//Eat it. The thrower has already notified the user. [Jon Aquino]
		}
	}
	private Envelope get_Big_BoxInModelCoordinates() {
		double buffer = 10 / getPanel().getViewport().getScale();
		return new Envelope(getModelDestination().x + buffer,
				getModelDestination().x - buffer, getModelDestination().y
						+ buffer, getModelDestination().y - buffer);
	}
}