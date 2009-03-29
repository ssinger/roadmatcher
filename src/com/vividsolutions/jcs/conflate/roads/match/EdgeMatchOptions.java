package com.vividsolutions.jcs.conflate.roads.match;

import java.io.Serializable;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;

//Things to keep up to date: #clone and .java2xml
//[Jon Aquino 2004-03-03]
public class EdgeMatchOptions implements Serializable, Cloneable {

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere(StringUtil.stackTrace(e));
            return null;
        }
    }

    public double getDistanceTolerance() {
        return distanceTolerance;
    }

    public double getLengthDifferenceTolerance() {
        return lengthDifferenceTolerance;
    }

    public double getOverlapDifference() {
        return overlapDifference;
    }

    public double getOverlapRatioTolerance() {
        return overlapRatioTolerance;
    }

    public double getLineSegmentLengthTolerance() {
        return lineSegmentLengthTolerance;
    }

    public void setDistanceTolerance(double distanceTolerance) {
        this.distanceTolerance = distanceTolerance;
    }

    public void setLengthDifferenceTolerance(double lengthDifferenceTolerance) {
        this.lengthDifferenceTolerance = lengthDifferenceTolerance;
    }

    public void setOverlapDifference(double overlapDifference) {
        this.overlapDifference = overlapDifference;
    }

    public void setOverlapRatioTolerance(double overlapRatioTolerance) {
        this.overlapRatioTolerance = overlapRatioTolerance;
    }

    public void setLineSegmentLengthTolerance(double lineSegmentLengthTolerance) {
        this.lineSegmentLengthTolerance = lineSegmentLengthTolerance;
    }

    private double distanceTolerance = 10.0;

    private double lengthDifferenceTolerance = .5;

    private double overlapDifference = 2 * distanceTolerance;

    private double overlapRatioTolerance = .3;

    private double lineSegmentLengthTolerance = 1.0;
    
    private double nearnessTolerance = 10;

	public double getNearnessTolerance() {
		return nearnessTolerance;
	}
	public void setNearnessTolerance(double nearnessTolerance) {
		this.nearnessTolerance = nearnessTolerance;
	}
}