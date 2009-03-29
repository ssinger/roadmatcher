package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collections;
import java.util.Map;
import com.vividsolutions.jcs.conflate.linearpathmatch.LinearPath;
import com.vividsolutions.jcs.conflate.linearpathmatch.match.PathMatchBuilder;
import com.vividsolutions.jcs.conflate.roads.model.AbstractNodeConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.pathmatch.RoadPathTracer;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.graph.Node;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.CreateSplitNodeUndoableCommand;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.ShortSegmentException;
import com.vividsolutions.jcs.plugin.conflate.roads.MatchPathsOperation.MyUndoableCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
public class PathMatchTool extends SpecifyNClosestRoadFeaturesTool {
    private static class FeaturesAndSplitCommand {
        public FeaturesAndSplitCommand(
                SourceFeature featureA,
                SourceFeature featureB,
                CreateSplitNodeOp.CreateSplitNodeUndoableCommand splitCommand) {
            this.featureA = featureA;
            this.featureB = featureB;
            this.splitCommand = splitCommand;
        }
        SourceFeature featureA;
        SourceFeature featureB;
        CreateSplitNodeOp.CreateSplitNodeUndoableCommand splitCommand;
    }
    public PathMatchTool(WorkbenchContext context) {
        super(2, true, true, null, "path-match-tool-button.png", Color.blue,
                context, GestureMode.LINE);
    }
    private PathMatchBuilder createPathMatchBuilder(
            DirectedEdge[] directedEdges) {
        final PathMatchBuilder pathMatchBuilder = new PathMatchBuilder(
                new RoadPathTracer(directedEdges[0]), new RoadPathTracer(
                        directedEdges[1]));
        pathMatchBuilder
                .setDistanceTolerance(toolboxModel().getSession()
                        .getMatchOptions().getEdgeMatchOptions()
                        .getDistanceTolerance());
        return pathMatchBuilder;
    }
    private DirectedEdge[] directedEdgesWithClosestStartNodes(
            SourceFeature feature0, SourceFeature feature1) {
        DirectedEdge[] directedEdges = new DirectedEdge[]{
                feature0.getRoadSegment().getDirEdge(0),
                AbstractNodeConsistencyRule.moreSimilarDirectedEdge(feature0
                        .getRoadSegment().getDirEdge(0), feature1
                        .getRoadSegment())};
        if (directedEdges[0].getFromNode().getCoordinate().distance(
                directedEdges[1].getFromNode().getCoordinate()) > directedEdges[0]
                .getToNode().getCoordinate().distance(
                        directedEdges[1].getToNode().getCoordinate())) {
            directedEdges[0] = directedEdges[0].getSym();
            directedEdges[1] = directedEdges[1].getSym();
        }
        return directedEdges;
    }
    private LinearPath directedEdgeToLinearPath(DirectedEdge directedEdge) {
        return new LinearPath(Collections.singleton(RoadPathTracer
                .toLinearEdge(directedEdge)));
    }
    private LineString directedEdgeToLineString(DirectedEdge edge) {
        return edge.getEdgeDirection() ? ((SourceRoadSegment) edge.getEdge())
                .getLine() : LineStringUtil.reverse(((SourceRoadSegment) edge
                .getEdge()).getLine());
    }
    private SourceFeature feature(DirectedEdge directedEdge) {
        return (SourceFeature) ((SourceRoadSegment) directedEdge.getEdge())
                .getFeature();
    }
    private SourceFeature featureIntersectingGesture(SourceFeature a,
            SourceFeature b) throws NoninvertibleTransformException {
        Geometry gesture = lineGesture(this);
        if (gesture.intersects(a.getRoadSegment().getLine())) {
            return a;
        }
        if (gesture.intersects(b.getRoadSegment().getLine())) {
            return b;
        }
        Assert.shouldNeverReachHere(a.getRoadSegment().getLine() + " "
                + b.getRoadSegment().getLine() + " " + gesture);
        return null;
    }
    private SourceFeature[] featuresSplittingIfNecessary(
            Map layerToSpecifiedFeaturesMap)
            throws NoninvertibleTransformException, ShortSegmentException {
        SourceFeature[] features = (SourceFeature[]) CollectionUtil
                .concatenate(layerToSpecifiedFeaturesMap.values()).toArray(
                        new SourceFeature[2]);
        if (!startPointsTooFarApart(directedEdgesWithClosestStartNodes(
                features[0], features[1]))) {
            return new SourceFeature[]{features[0], features[1]};
        }
        SourceFeature[] featuresAfterSplitting;
        if ((featuresAfterSplitting = featuresSplittingIfPossible(features[1],
                features[0], features[0].getRoadSegment().getStartNode()
                        .getCoordinate())) != null) {
            return featuresAfterSplitting;
        }
        if ((featuresAfterSplitting = featuresSplittingIfPossible(features[1],
                features[0], features[0].getRoadSegment().getEndNode()
                        .getCoordinate())) != null) {
            return featuresAfterSplitting;
        }
        if ((featuresAfterSplitting = featuresSplittingIfPossible(features[0],
                features[1], features[1].getRoadSegment().getStartNode()
                        .getCoordinate())) != null) {
            return featuresAfterSplitting;
        }
        if ((featuresAfterSplitting = featuresSplittingIfPossible(features[0],
                features[1], features[1].getRoadSegment().getEndNode()
                        .getCoordinate())) != null) {
            return featuresAfterSplitting;
        }
        return new SourceFeature[]{features[0], features[1]};
    }
    private SourceFeature[] featuresSplittingIfPossible(
            SourceFeature featureToSplit, SourceFeature otherFeature,
            Coordinate endpointOfOtherFeature)
            throws NoninvertibleTransformException, ShortSegmentException {
        Coordinate splitCoordinate = new DistanceOp(featureToSplit
                .getRoadSegment().getLine(), featureToSplit.getGeometry()
                .getFactory().createPoint(endpointOfOtherFeature))
                .closestPoints()[0];
        if (splitCoordinate.distance(endpointOfOtherFeature) > toolboxModel()
                .getSession().getMatchOptions().getEdgeMatchOptions()
                .getDistanceTolerance()) {
            return null;
        }
        CreateSplitNodeUndoableCommand splitCommand = CreateSplitNodeOp
                .createUndoableCommand(featureToSplit, splitCoordinate, false, true,
                        true, toolboxModel(), getPanel().getContext());
        execute(splitCommand);
        return new SourceFeature[]{
                otherFeature,
                featureIntersectingGesture(splitCommand.getNewFeatureA(),
                        splitCommand.getNewFeatureB())};
    }
    protected void gestureFinished(Map layerToSpecifiedFeaturesMap)
            throws Exception {
        try {
            SourceFeature[] features = featuresSplittingIfNecessary(layerToSpecifiedFeaturesMap);
            DirectedEdge[] directedEdges = directedEdgesWithClosestStartNodes(
                    features[0], features[1]);
            match(directedEdges);
            DirectedEdge[] oppositeDirectedEdges = new DirectedEdge[]{
                    oppositeDirectedEdgeIfUnknown(directedEdges[0]),
                    oppositeDirectedEdgeIfUnknown(directedEdges[1])};
            if (oppositeDirectedEdges[0] != null
                    && oppositeDirectedEdges[1] != null) {
                match(oppositeDirectedEdges);
            }
        } catch (CreateSplitNodeOp.ShortSegmentException e) {
            //Eat it. The thrower has already notified the user. [Jon Aquino]
        }
    }
    protected String getDefaultNoRoadSegmentsWarning() {
        return FUTURE_StringUtil.substitute(
                ErrorMessages.pathMatchTool_noRoadSegments,
                new Object[]{sourceLayerDescription()});
    }
    protected boolean includeInProximitySearch(SourceFeature feature,
            Point clickPoint) {
        if (feature.getRoadSegment().getState() != SourceState.UNKNOWN) {
            //Get here when, for example, user selects Matched road segment
            //[Jon Aquino 2004-02-13]
            setNoRoadSegmentsWarning(ErrorMessages.pathMatchTool_invalidInput);
        }
        return feature.getRoadSegment().getState() == SourceState.UNKNOWN;
    }
    protected Map layerToSpecifiedFeaturesMap()
            throws NoninvertibleTransformException {
        Map layerToSpecifiedFeaturesMap = super.layerToSpecifiedFeaturesMap();
        if (layerToSpecifiedFeaturesMap.size() == 1) {
            //Get here when, for example, user selects two road segments
            //from the same network [Jon Aquino 2004-02-13]
            setNoRoadSegmentsWarning(ErrorMessages.pathMatchTool_invalidInput);
            return Collections.EMPTY_MAP;
        }
        return layerToSpecifiedFeaturesMap;
    }
    private void match(DirectedEdge[] directedEdges) {
        if (startPointsTooFarApart(directedEdges)) {
            warnEndpointsTooFarApart(directedEdges);
            return;
        }
        final PathMatchBuilder pathMatchBuilder = createPathMatchBuilder(directedEdges);
        if (!pathMatchBuilder.hasMatch()) {
            getContext().getWorkbench().getFrame().warnUser(
                    ErrorMessages.pathMatchTool_pathMatchBuilder_noMatch);
            return;
        }
        MyUndoableCommand command = new MatchPathsOperation()
                .createUndoableCommand(pathMatchBuilder.getMatch(), getPanel()
                        .getContext(), toolboxModel());
        execute(command);
        if (command.isExecutionSuccessful()) {
            new AutoAdjustAfterManualCommitOp().autoAdjust(command,
                    getContext());
        }
    }
    private DirectedEdge oppositeDirectedEdgeIfUnknown(DirectedEdge directedEdge) {
        //If edge was replaced by a couple of SplitRoadSegments, if
        //its start node was degree 1, it will have been removed from
        //the network and replaced by a new Node. We want the new one
        //(the old one will now be degree 0 and will result in an
        //assertion failure in #findNearlyStraightCandidate). This was
        //happening when I was using PathMatchTool on the
        //conflate_networkA, B datasets [Jon Aquino 2004-02-12]
        Node startNode = ((SourceRoadSegment) directedEdge.getEdge())
                .getNetwork().getGraph().findNode(
                        directedEdge.getFromNode().getCoordinate());
        DirectedEdge oppositeDirectedEdge = RoadPathTracer
                .findNearlyStraightCandidate(startNode, Angle
                        .normalize(directedEdge.getAngle() + Math.PI));
        if (oppositeDirectedEdge != null
                && feature(oppositeDirectedEdge).getRoadSegment().getState() != SourceState.UNKNOWN) {
            return null;
        }
        return oppositeDirectedEdge;
    }
    private boolean startPointsTooFarApart(DirectedEdge[] directedEdges) {
        return directedEdges[0].getFromNode().getCoordinate().distance(
                directedEdges[1].getFromNode().getCoordinate()) > toolboxModel()
                .getSession().getMatchOptions().getEdgeMatchOptions()
                .getDistanceTolerance();
    }
    private void warnEndpointsTooFarApart(DirectedEdge[] directedEdges) {
        getContext().getWorkbench().getFrame().warnUser(
                FUTURE_StringUtil.substitute(
                        ErrorMessages.pathMatchTool_endpointsTooFarApart,
                        new Object[]{
                                toolboxModel().getSession().getMatchOptions()
                                        .getEdgeMatchOptions()
                                        .getDistanceTolerance()
                                        + "",
                                directedEdges[0].getFromNode().getCoordinate()
                                        .distance(
                                                directedEdges[1].getFromNode()
                                                        .getCoordinate())
                                        + ""}));
    }
}