package com.vividsolutions.jcs.plugin.conflate.roads.sourcematchconsistency;

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency.SourceMatchConsistencyRule;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.ConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.AutoAdjuster;
import com.vividsolutions.jcs.plugin.conflate.roads.PostponeInconsistencyHandler;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class SourceMatchConsistencyConfiguration implements
        ConsistencyConfiguration {

    public Style getStyle() {
        return style;
    }

    private SourceMatchConsistencyRule rule = new SourceMatchConsistencyRule();

    public ConsistencyRule getRule() {
        return rule;
    }

    private PostponeInconsistencyHandler postponeInconsistencyHandler = new PostponeInconsistencyHandler(
            SourceMatchConsistencyRule.START_NEIGHBOURHOOD_KEY,
            SourceMatchConsistencyRule.END_NEIGHBOURHOOD_KEY) {
        protected Collection roadSegments(Coordinate warningLocation,
                ConflationSession session) {
            ArrayList roadSegments = new ArrayList();
            addIncidentRoadSegments(warningLocation, 0, session, roadSegments);
            addIncidentRoadSegments(warningLocation, 1, session, roadSegments);
            return roadSegments;
        }

        private void addIncidentRoadSegments(Coordinate coordinate, int i,
                ConflationSession session, Collection roadSegments) {
            RoadNode node = (RoadNode) session.getSourceNetwork(i).getGraph()
                    .findNode(coordinate);
            if (node != null) {
                roadSegments.addAll(node.getIncidentRoadSegments());
            }
        }

        protected Collection roadSegments(Envelope envelope,
                ConflationSession session)
                throws NoninvertibleTransformException {
            return FUTURE_CollectionUtil.concatenate(session
                    .getSourceNetwork(0).roadSegmentsIntersecting(envelope),
                    session.getSourceNetwork(1).roadSegmentsIntersecting(
                            envelope));
        }
    };
    private Style style = new SourceMatchInconsistentEndpointStyle();

    public void setPostponedForInconsistenciesAt(Point point, boolean postponed, PlugInContext context)
            throws NoninvertibleTransformException {
        postponeInconsistencyHandler.setPostponedForInconsistenciesAt(point, postponed, context);
    }

    public AutoAdjuster getAutoAdjuster() {
        throw new UnsupportedOperationException();
    }

}
