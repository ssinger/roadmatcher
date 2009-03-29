package com.vividsolutions.jcs.plugin.conflate.roads;
import java.io.Serializable;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.AbstractVertexTransferOp;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.ClosestPointVertexTransferOp;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
public class VertexTransferProperties implements Serializable {

    public AbstractVertexTransferOp getVertexTransferOp() {
        if (!getVertexTransferOpClass().isInstance(vertexTransferOp)) {
            try {
                vertexTransferOp = (AbstractVertexTransferOp) getVertexTransferOpClass()
                        .newInstance();
            } catch (InstantiationException e) {
                FUTURE_Assert.shouldNeverReachHere(e);
                return null;
            } catch (IllegalAccessException e) {
                FUTURE_Assert.shouldNeverReachHere(e);
                return null;
            }
        }
        return vertexTransferOp;
    }
    private Class vertexTransferOpClass = ClosestPointVertexTransferOp.class;
    public Class getVertexTransferOpClass() {
        return vertexTransferOpClass;
    }
    private boolean transferringVertices = false;
    public boolean isTransferringVertices() {
        return transferringVertices;
    }
    private boolean transferringVerticesFrom0To1 = false;
    public boolean isTransferringVerticesFrom0To1() {
        return transferringVerticesFrom0To1;
    }
    private boolean transferringVerticesFrom1To0 = false;
    public boolean isTransferringVerticesFrom1To0() {
        return transferringVerticesFrom1To0;
    }
    public boolean isTransferringVerticesTo(SourceRoadSegment roadSegment) {
        if (roadSegment.getMatchingRoadSegment() == null) {
            return false;
        }
        if (!isTransferringVertices()) {
            return false;
        }
        if (roadSegment.getNetworkID() == 0
                && !isTransferringVerticesFrom1To0()) {
            return false;
        }
        if (roadSegment.getNetworkID() == 1
                && !isTransferringVerticesFrom0To1()) {
            return false;
        }
        return true;
    }
    public void setTransferringVertices(boolean transferringVertices) {
        this.transferringVertices = transferringVertices;
    }
    public void setTransferringVerticesFrom0To1(
            boolean transferringVerticesFrom0To1) {
        this.transferringVerticesFrom0To1 = transferringVerticesFrom0To1;
    }
    public void setTransferringVerticesFrom1To0(
            boolean transferringVerticesFrom1To0) {
        this.transferringVerticesFrom1To0 = transferringVerticesFrom1To0;
    }
    public void setVertexTransferOpClass(Class vertexTransferOpClass) {
        this.vertexTransferOpClass = vertexTransferOpClass;
    }
    private AbstractVertexTransferOp vertexTransferOp;
}