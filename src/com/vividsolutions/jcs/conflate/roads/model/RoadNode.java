package com.vividsolutions.jcs.conflate.roads.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.match.Matches;
import com.vividsolutions.jcs.conflate.roads.nodematch.NodeMatching;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.graph.DirectedEdgeStar;
import com.vividsolutions.jcs.graph.Node;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public class RoadNode extends Node {

    private RoadNode matchNode = null;

    private double matchValue = Double.MIN_VALUE;

    private double maxAdjacentNodeDistance = -1;// cache

    private NodeMatching nodeMatching;

    private Matches matchList = new Matches(new RoadNodeComparator());

    private RoadGraph graph;

    private static class MyDirectedEdgeStar extends DirectedEdgeStar {

        private RoadNode node;

        public void setRoadNode(RoadNode node) {
            this.node = node;
        }

        public void remove(DirectedEdge de) {
            super.remove(de);
            if (node.getOutEdges().getDegree() == 0) {
                node.graph.remove(node);
            }
        }
    }

    public RoadNode(Coordinate point, RoadGraph graph) {
        super(point, new MyDirectedEdgeStar());
        this.graph = graph;
        ((MyDirectedEdgeStar) getOutEdges()).setRoadNode(this);
    }

    public SourceRoadSegment firstRoadSegment(SourceState state,
            SourceRoadSegment ignore) {
        for (Iterator i = getOutEdges().iterator(); i.hasNext(); ) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            if ((SourceRoadSegment) directedEdge.getEdge() == ignore) {
                continue;
            }
            if (((SourceRoadSegment) directedEdge.getEdge()).getState() == state) { return (SourceRoadSegment) directedEdge
                    .getEdge(); }
        }
        return null;
    }

    public SourceRoadSegment firstRoadSegmentNot(SourceState[] states,
            SourceRoadSegment ignore) {
        outer: for (Iterator i = getOutEdges().iterator(); i.hasNext(); ) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            if ((SourceRoadSegment) directedEdge.getEdge() == ignore) {
                continue;
            }
            for (int j = 0; j < states.length; j++) {
                if (((SourceRoadSegment) directedEdge.getEdge()).getState() == states[j]) {
                    continue outer;
                }
            }
            return (SourceRoadSegment) directedEdge.getEdge();
        }
        return null;
    }

    public boolean hasMatch() {
        return matchNode != null;
    }

    public RoadNode getMatch() {
        return matchNode;
    }

    public void setMatch(RoadNode matchNode) {
        this.matchNode = matchNode;
        this.matchValue = Double.NEGATIVE_INFINITY;
    }

    public void setMatch(RoadNode matchNode, double matchValue) {
        this.matchNode = matchNode;
        this.matchValue = matchValue;
    }

    public void addMatch(RoadNode matchNode, double matchValue) {
        matchList.setValue(matchNode, matchValue);
    }

    public Matches getMatches() {
        return matchList;
    }

    public void clearMatch() {
        this.matchNode = null;
        this.matchValue = Double.NEGATIVE_INFINITY;
    }

    public void setMatchMaximum(RoadNode matchNode, double matchValue) {
        if (matchNode == null || matchValue > this.matchValue) {
            this.matchNode = matchNode;
            this.matchValue = matchValue;
        }
    }

    public NodeMatching getMatching() {
        return nodeMatching;
    }

    public void setMatching(NodeMatching nodeMatching) {
        this.nodeMatching = nodeMatching;
    }

    public double getMatchValue() {
        return matchValue;
    }

    public boolean isMatched() {
        return matchNode != null;
    }

    public class RoadNodeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((RoadNode) o1).getCoordinate().compareTo(
                    ((RoadNode) o2).getCoordinate());
        }
    }

    public Collection getIncidentRoadSegments() {
        return CollectionUtil.collect(getOutEdges().getEdges(), new Block() {
            public Object yield(Object directedEdge) {
                return ((DirectedEdge) directedEdge).getEdge();
            }
        });
    }

    public RoadGraph getGraph() {
        return graph;
    }
}