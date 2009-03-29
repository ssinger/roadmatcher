package com.vividsolutions.jcs.conflate.roads.pathmatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jcs.conflate.linearpathmatch.match.PathMatch;
import com.vividsolutions.jcs.conflate.linearpathmatch.match.PathMatchBuilder;
import com.vividsolutions.jcs.conflate.roads.model.RoadGraph;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.RoadNodeMatchValueComparator;
import com.vividsolutions.jcs.conflate.roads.nodematch.MatchEdge;
import com.vividsolutions.jcs.conflate.roads.nodematch.NodeMatching;
import com.vividsolutions.jcs.debug.Debug;
import com.vividsolutions.jcs.debug.DebugFeature;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.graph.PlanarGraph;
import com.vividsolutions.jump.task.TaskMonitor;

/**
 * Matches paths through a {@link RoadNetwork}.
 */

public class RoadNetworkPathMatcher {
	private static boolean isInGraph(DirectedEdge de, PlanarGraph graph) {
		List dirEdges = graph.getDirectedEdges();
		return dirEdges.contains(de);
	}

	private int pathMatchCount = 0;

	private int splitMatchCount = 0;

	private static String MATCH = "MATCH";

	private static String SPLIT = "SPLIT";

	private double distanceTolerance = 0.0;

	private double roadSegmentLengthTolerance = 0.0;

	private RoadNetwork[] source = new RoadNetwork[2];

	private RoadGraph[] sourceGraph = new RoadGraph[2];

	public RoadNetworkPathMatcher(RoadNetwork source0, RoadNetwork source1) {
		source[0] = source0;
		source[1] = source1;

		sourceGraph[0] = source[0].getGraph();
		sourceGraph[1] = source[1].getGraph();
	}

	public void setDistanceTolerance(double distanceTolerance) {
		this.distanceTolerance = distanceTolerance;
	}

	public void setRoadSegmentLengthTolerance(double roadSegmentLengthTolerance) {
		this.roadSegmentLengthTolerance = roadSegmentLengthTolerance;
	}

	public void match(TaskMonitor monitor) {
		matchOnlyCurrentGraphState(monitor);
	}

	private List getMatchedNodesOrdered() {
		List matchedNodes = new ArrayList();
		for (Iterator nodeIt = source[0].getGraph().nodeIterator(); nodeIt
				.hasNext();) {
			RoadNode node = (RoadNode) nodeIt.next();
			if (node.hasMatch())
				matchedNodes.add(node);
		}
		// process best matches first
		Collections.sort(matchedNodes, new RoadNodeMatchValueComparator());
		Collections.reverse(matchedNodes);
		return matchedNodes;
	}

	class NodeMatchValueComparator {

	}

	/**
	 * This method uses the graph objects which are reffed by the node matching
	 * but checks to see that they are still in the graph. (The graph will
	 * change as a result of path splitting.)
	 */
	private void matchOnlyCurrentGraphState(TaskMonitor monitor) {
		// make a new list of the matched nodes, to avoid concurrent
		// modification problems
		List matchedNodes = getMatchedNodesOrdered();

		int total = matchedNodes.size();
		int count = 0;

		for (Iterator nodeIt = matchedNodes.iterator(); nodeIt.hasNext();) {
			count++;
			monitor.report(count, total, "Paths tested");
			if (monitor.isCancelRequested())
				break;

			RoadNode subNode = (RoadNode) nodeIt.next();
			if (subNode.isMatched()) {
				RoadNode matchNode = subNode.getMatch();
				NodeMatching matching = subNode.getMatching();
				matchPathsFromNodes(matching);
			}
		}
		DebugFeature.saveFeatures(MATCH,
				"Y:\\jcs\\testUnit\\roads\\pathMatches.jml");
		DebugFeature.saveFeatures(SPLIT,
				"Y:\\jcs\\testUnit\\roads\\pathSplits.jml");
		Debug.println("paths matched = " + pathMatchCount);
		Debug.println("splits matched = " + splitMatchCount);
	}

	private void matchPathsFromNodes(NodeMatching matching) {
		List matchEdges = matching.getEdgeMatches();
		for (Iterator matchEdgeIt = matchEdges.iterator(); matchEdgeIt
				.hasNext();) {
			MatchEdge me = (MatchEdge) matchEdgeIt.next();
			if (isInGraph(me.getDirectedEdge(), source[0].getGraph())
					&& isInGraph(me.getMatch().getDirectedEdge(), source[1]
							.getGraph())) {

				matchPath(me.getDirectedEdge(), me.getMatch()
				.getDirectedEdge());
			}
			//return; // testing only - only do one matching
		}
	}

	private void matchPath(DirectedEdge de0, DirectedEdge de1) {
		PathMatchBuilder pmb = new PathMatchBuilder(
				new RoadPathTracer(de0), new RoadPathTracer(de1));
		pmb.setDistanceTolerance(distanceTolerance);
		if (!pmb.hasMatch())
			return;

		PathMatch pm = pmb.getMatch();

		DebugFeature.add(MATCH, pm.getGeometry(), "");
		RoadPathZipper zipper = new RoadPathZipper(pm);
		zipper.setDistanceTolerance(distanceTolerance);
		zipper.setSegmentLengthTolerance(roadSegmentLengthTolerance);
		zipper.zipper(true);
		DebugFeature.add(SPLIT, zipper.getGeometry(), "");
		//splitMatchCount += rsp0.matchSplits(rsp1);
		pathMatchCount++;
	}

}