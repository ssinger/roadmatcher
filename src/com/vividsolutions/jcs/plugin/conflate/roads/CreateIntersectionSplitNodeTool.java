package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import com.vividsolutions.jump.workbench.WorkbenchContext;
public class CreateIntersectionSplitNodeTool extends SpecifyRoadFeaturesTool {
    private WorkbenchContext context;
    public CreateIntersectionSplitNodeTool(WorkbenchContext context) {
        super(true, true, null, "create-split-intersection-tool-button.gif",
                Color.black, context, GestureMode.POINT);
        this.context = context;
        setViewClickBuffer(10);
    }
    public String getName() {
		return "Create Intersection SplitNode";
	}
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!SpecifyRoadFeaturesTool.checkConflationSessionStarted(context)) {
            return;
        }
        try {
            if (!new CreateSplitNodeAtIntersectionOp().handleGesture(
                    getModelDestination(), getBoxInModelCoordinates(), true,
                    true, toolboxModel(), getPanel().getContext())) {
                getPanel().getContext().warnUser(
                        ErrorMessages.createIntersectionSplitNodeTool_noIntersections);
            }
        } catch (CreateSplitNodeAtIntersectionOp.RoadSegmentsAdjustedException e) {
            getPanel().getContext().warnUser(
                    ErrorMessages.createSplitNodeTool_adjusted);
            return;
        } catch (CreateSplitNodeOp.ShortSegmentException e) {
            //Eat it. The thrower has already notified the user. [Jon Aquino]
        }
    }
}