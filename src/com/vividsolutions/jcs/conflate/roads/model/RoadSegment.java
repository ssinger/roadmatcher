package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.graph.Edge;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;

public abstract class RoadSegment extends Edge {    

    /**
	 * Don't forget to call #setFeature
	 *
	 * @param line
	 *                   may be different from sourceFeature's geometry
	 */
    public RoadSegment(LineString line, RoadNetwork network) {
        this.line = line;        
        this.network = network;
    }

    public RoadNode getEndNode() {
        return endNode;
    }

    public Feature getFeature() {
        return feature;
    }

    public LineString getLine() {
        return line;
    }   

    public String getName() {
        return name;
    }

    public RoadNetwork getNetwork() {
        return network;
    }
    public RoadNode getStartNode() {
        return startNode;
    }
    public void setEndNode(RoadNode endNode) {
        this.endNode = endNode;
    }
    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    public void setStartNode(RoadNode startNode) {
        this.startNode = startNode;
    }
    public String toString() {
        return getName();
    }
    transient private RoadNode endNode;
    private Feature feature;
    private LineString line;
    private String name;
    private RoadNetwork network;
    transient private RoadNode startNode;

    public class RoadSegmentComparator implements Comparator, Serializable
    {

      public int compare(Object o1, Object o2)
      {
        return ((RoadSegment) o1).getLine().compareTo(((RoadSegment) o2).getLine());
      }
    }
    
    private boolean inNetwork = false;


    public Collection getNeighbours() {
        return FUTURE_CollectionUtil.concatenate(neighbours(getStartNode()), neighbours(getEndNode()));
    }
    private Collection neighbours(RoadNode node) {
        ArrayList neighbours = new ArrayList();
        for (Iterator i = node.getOutEdges().iterator(); i.hasNext(); ) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            if (directedEdge.getEdge() != this) neighbours.add(directedEdge.getEdge());
        }
        return neighbours;
    }
    public void enteringNetwork() {
        Assert.isTrue(!inNetwork);        
    }
    public void enteredNetwork() {
        Assert.isTrue(!inNetwork);
        inNetwork = true;
    }        
    public void exitingNetwork() {
        Assert.isTrue(inNetwork);        
    }
    public void exitedNetwork() {
        Assert.isTrue(inNetwork);
        inNetwork = false;
    }    
    public boolean isInNetwork() {
        return inNetwork;
    }
    public int getNetworkID() {
        return network.getID();
    }    
    
}