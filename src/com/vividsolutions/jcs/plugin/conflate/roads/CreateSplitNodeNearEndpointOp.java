package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.ShortSegmentException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;


public class CreateSplitNodeNearEndpointOp {

    private class ApparentEndpoint {
    
        public ApparentEndpoint(Coordinate coordinate, RoadNetwork network) {
            this.coordinate = coordinate;
            this.network = network;
        }
    
        public Coordinate coordinate;
    
        public RoadNetwork network;
    }

    public boolean handleGesture(Coordinate clickCoordinate,
            Envelope roadSegmentSearchEnvelope,
            Envelope vertexSnapSearchEnvelope, boolean layer0, boolean layer1,
            final ToolboxModel toolboxModel, ErrorHandler errorHandler)
            throws NoninvertibleTransformException, ShortSegmentException {
        ApparentEndpoint closestEndpoint = closestApparentEndpoint(
                clickCoordinate, roadSegmentSearchEnvelope, layer0, layer1,
                toolboxModel);
        if (closestEndpoint == null) { return false; }
        //Find the closest apparent line -- yes, even among those that have
        //been adjusted. If the closest line has been adjusted, we will
        //reject it (see below). [Jon Aquino 12/29/2003]
        final SourceRoadSegment roadSegment = roadSegmentWithClosestApparentLine(
                closestEndpoint.network.getOther(), clickCoordinate,
                roadSegmentSearchEnvelope);
        if (roadSegment == null) { return false; }
        if (roadSegment.isAdjusted()) { return false; }
        if (CreateSplitNodeTool.closestPointIsNode(new GeometryFactory()
                .createPoint(closestEndpoint.coordinate), roadSegment)) { return false; }
        AbstractPlugIn.execute(CreateSplitNodeOp.createUndoableCommand(
                (SourceFeature) roadSegment.getFeature(), CreateSplitNodeTool.snapToExistingVertex(
                        closestEndpoint.coordinate, roadSegment.getLine(),
                        vertexSnapSearchEnvelope), false, true, true, toolboxModel, errorHandler),
                new LayerManagerProxy() {
    
                    public LayerManager getLayerManager() {
                        return layer(roadSegment, toolboxModel)
                                .getLayerManager();
                    }
                });
        return true;
    }

    private ApparentEndpoint closestApparentEndpoint(Coordinate c,
            Envelope envelope, boolean layer0, boolean layer1,
            ToolboxModel toolboxModel) {
        ArrayList apparentEndpoints = new ArrayList();
        //Yes, we're searching in the *other* network [Jon Aquino 12/2/2003]
        if (layer0) {
            apparentEndpoints.addAll(apparentEndpointsIn(envelope, toolboxModel
                    .getSession().getSourceNetwork(1)));
        }
        if (layer1) {
            apparentEndpoints.addAll(apparentEndpointsIn(envelope, toolboxModel
                    .getSession().getSourceNetwork(0)));
        }
        if (apparentEndpoints.isEmpty()) { return null; }
        ApparentEndpoint closestApparentEndpoint = (ApparentEndpoint) apparentEndpoints
                .iterator().next();
        for (Iterator i = apparentEndpoints.iterator(); i.hasNext();) {
            ApparentEndpoint apparentEndpoint = (ApparentEndpoint) i.next();
            if (c.distance(apparentEndpoint.coordinate) < c
                    .distance(closestApparentEndpoint.coordinate)) {
                closestApparentEndpoint = apparentEndpoint;
            }
        }
        return closestApparentEndpoint;
    }

    private Collection apparentEndpointsIn(Envelope envelope,
            final RoadNetwork network) {
        return CollectionUtil.collect(network.apparentEndpointsIn(envelope),
                new Block() {
    
                    public Object yield(Object coordinate) {
                        return new ApparentEndpoint((Coordinate) coordinate,
                                network);
                    }
                });
    }

    private SourceRoadSegment roadSegmentWithClosestApparentLine(
            RoadNetwork network, Coordinate c,
            Envelope roadSegmentSearchEnvelope) {
        Point p = new GeometryFactory().createPoint(c);
        Collection roadSegments = network
                .roadSegmentsApparentlyIntersecting(roadSegmentSearchEnvelope);
        if (roadSegments.isEmpty()) { return null; }
        SourceRoadSegment closestRoadSegment = (SourceRoadSegment) roadSegments
                .iterator().next();
        for (Iterator i = roadSegments.iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            if (roadSegment.getApparentLine().distance(p) < closestRoadSegment
                    .getApparentLine().distance(p)) {
                closestRoadSegment = roadSegment;
            }
        }
        return closestRoadSegment;
    }

    public static Layer layer(SourceRoadSegment roadSegment,
            ToolboxModel toolboxModel) {
        RoadNetwork network = roadSegment.getNetwork();
        return network == network.getSession().getSourceNetwork(0) ? toolboxModel
                .getSourceLayer(0)
                : toolboxModel.getSourceLayer(1);
    }

}
