package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;
public class MoveSplitNodeTool extends RoadSegmentEndpointGrabberTool {
    public MoveSplitNodeTool() {
        super(new DoMoveSplitNodeTool(), new Block() {
            public Object yield(Object doMoveSplitNodeTool,
                    Object roadSegmentSplitAtStart) {
                ((DoMoveSplitNodeTool) doMoveSplitNodeTool)
                        .setRoadSegmentSplitAtStart((SplitRoadSegment) roadSegmentSplitAtStart);
                return null;
            }
        });
    }
    public String getName() {
        return AbstractCursorTool.name(this);
    }
    protected CursorTool getNoEndpointsHereTool() {
        return noEndpointsHereTool;
    }
    private CursorTool noEndpointsHereTool = new DummyTool() {
        public void mousePressed(MouseEvent e) {
            getContext().getWorkbench().getFrame().warnUser(
                    ErrorMessages.moveSplitNodeTool_noEndpoints);
        }
    };
    protected Collection filter(Collection features,
            final Coordinate click) {
        return CollectionUtil.select(features, new Block() {
            public Object yield(Object feature) {
                return Boolean.valueOf(accept(((SourceFeature) feature)
                        .getRoadSegment()));
            }
            private boolean accept(SourceRoadSegment roadSegment) {
                return !roadSegment.isAdjusted()
                        && roadSegment instanceof SplitRoadSegment
                        && ((SplitRoadSegment) roadSegment).isSplitAtStart()
                        && closestEndpointWithinTolerance(roadSegment, click) != null
                        && closestEndpointWithinTolerance(roadSegment, click)
                                .equals(
                                        roadSegment.getStartNode()
                                                .getCoordinate());
            }
        });
    }
    public Icon getIcon() {
        return icon;
    }
    public Cursor getCursor() {
        return cursor;
    }
    private ImageIcon icon = SpecifyRoadFeaturesTool
            .createIcon("move-split-node-tool-button.gif");
    private Cursor cursor = FUTURE_GUIUtil
            .createCursorFromIcon("move-split-node-tool-button.gif");
}