package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

public class SetUnmatchedStateTool extends SpecifyRoadFeaturesTool {

    public SetUnmatchedStateTool(SourceState state, boolean layer0,
            boolean layer1, String cursorImage, String buttonImage,
            Color color, WorkbenchContext context, GestureMode gestureMode) {
        super(layer0, layer1, cursorImage, buttonImage, color, context,
                gestureMode);
        this.state = state;
    }

    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!checkConflationSessionStarted(getContext())) { return; }
        noRoadSegmentsWarning = FUTURE_StringUtil.substitute(
                ErrorMessages.setUnmatchedStateTool_noRoadSegments,
                new Object[] { sourceLayerDescription()});
        Map layerToSpecifiedFeaturesMap = layerToSpecifiedFeaturesMap();
        if (layerToSpecifiedFeaturesMap.isEmpty()) {
            warnUser(noRoadSegmentsWarning, getContext());
            return;
        }
        execute(createUndoableCommand(getName(), state,
                layerToSpecifiedFeaturesMap, toolboxModel(), getPanel()
                        .getContext()));
    }

    protected String noRoadSegmentsWarning;

    private SourceState state;

    public static UndoableCommand createUndoableCommand(String name,
            SourceState state, Map layerToSpecifiedFeaturesMap,
            ToolboxModel toolboxModel, ErrorHandler errorHandler) {
        final Transaction transaction = new Transaction(toolboxModel,
                errorHandler);
        for (Iterator i = CollectionUtil.concatenate(
                layerToSpecifiedFeaturesMap.values()).iterator(); i.hasNext();) {
            SourceFeature feature = (SourceFeature) i.next();
            transaction.severMatch(feature.getRoadSegment());
            transaction.setState(feature.getRoadSegment(), state, null);
        }
        return new UndoableCommand(name) {

            public void execute() {
                transaction.execute();
            }

            public void unexecute() {
                transaction.unexecute();
            }
        };
    }

}