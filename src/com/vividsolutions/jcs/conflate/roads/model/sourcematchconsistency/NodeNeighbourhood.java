package com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.AbstractNodeConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

import java.util.*;


public class NodeNeighbourhood {
    /**
     * Some nodes may be at the same location if they are from different networks [Jon Aquino 12/18/2003]
     */
    private Set nodes = new HashSet();
    private Envelope envelope = null;
    private ConflationSession session;

    public NodeNeighbourhood(RoadNode node) {
        session = node.getGraph().getNetwork().getSession();
        nodes.add(node);
        nodes.addAll(
            startNodes(
                matchingDirectedEdges(directedEdges(node.getCoordinate()))));
        //The following loop ensures that we don't forget the match of the neighbour
        //of the match of an incident road segment. [Jon Aquino 12/18/2003]
        for (Iterator i = new ArrayList(nodes).iterator(); i.hasNext();) {
            RoadNode other = (RoadNode) i.next();
            nodes.addAll(
                startNodes(
                    matchingDirectedEdges(directedEdges(other.getCoordinate()))));
        }
    }

    private Collection startNodes(Collection directedEdges) {
        return CollectionUtil.collect(
            directedEdges,
            new Block() {
                public Object yield(Object directedEdge) {
                    return ((DirectedEdge) directedEdge).getFromNode();
                }
            });
    }

    private Set directedEdges(Coordinate c) {
        return (Set) FUTURE_CollectionUtil.injectInto(
            nodes(c), new HashSet(),
            new Block() {
                public Object yield(Object directedEdges, Object node) {
                    ((Set) directedEdges).addAll(
                        directedEdges((RoadNode) node));

                    return null;
                }
            });
    }

    private Set nodes(Coordinate c) {
        HashSet myNodes = new HashSet();
        for (int i = 0; i < 2; i++) {
            RoadNode node =
                (RoadNode) session.getSourceNetwork(i).getGraph().findNode(c);
            if (node != null) {
                myNodes.add(node);
            }
        }

        return myNodes;
    }

    /**
     * For each of the given directed edges, returns the more similar of the two
     * directed edges of the matching road segment of the road segment.
     */
    private Collection matchingDirectedEdges(Collection directedEdges) {
        ArrayList matchingDirectedEdges = new ArrayList();
        for (Iterator i = directedEdges.iterator(); i.hasNext();) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            SourceRoadSegment roadSegment =
                (SourceRoadSegment) directedEdge.getEdge();
            if (roadSegment.getMatchingRoadSegment() == null) {
                continue;
            }
            matchingDirectedEdges.add(
                AbstractNodeConsistencyRule.moreSimilarDirectedEdge(
                    directedEdge, roadSegment.getMatchingRoadSegment()));
        }

        return matchingDirectedEdges;
    }

    public Collection getIncludedRoadSegments() {
        return (Set) FUTURE_CollectionUtil.injectInto(
            nodes, new HashSet(),
            new Block() {
                public Object yield(Object includedRoadSegments, Object node) {
                    ((Set) includedRoadSegments).addAll(
                        CollectionUtil.select(
                                ((RoadNode) node).getIncidentRoadSegments(),
                            new Block() {
                            public Object yield(Object roadSegment) {
                                return Boolean.valueOf(
                                        ((SourceRoadSegment) roadSegment).getState()
                                         .indicates(SourceState.INCLUDED));
                            }
                        }));

                    return null;
                }
            });
    }

    private Collection includedNodes() {
        return CollectionUtil.select(nodes, new Block() {
            private boolean included(RoadNode node) {
                for (Iterator i = node.getIncidentRoadSegments().iterator(); i
                .hasNext(); ) {
                    SourceRoadSegment roadSegment = (SourceRoadSegment) i
                    .next();
                    if (roadSegment.getState().indicates(SourceState.INCLUDED)) {
                        return true;
                    }
                }
                return false;
            }
            public Object yield(Object arg) {
                return Boolean.valueOf(included((RoadNode) arg));
            }
        });
    }

    public SortedSet includedNodeLocations() {
        //Find set of node coordinates rather than set of nodes, because two nodes
        //may have the same coordinate if they are from different networks. [Jon Aquino 12/17/2003]
        return new TreeSet(coordinatesOf(includedNodes()));
    }

    private Collection coordinatesOf(Collection nodes) {
        return CollectionUtil.collect(
            nodes,
            new Block() {
                public Object yield(Object node) {
                    return ((RoadNode) node).getCoordinate();
                }
            });
    }

    private List directedEdges(RoadNode node) {
        return node.getOutEdges().getEdges();
    }

}
