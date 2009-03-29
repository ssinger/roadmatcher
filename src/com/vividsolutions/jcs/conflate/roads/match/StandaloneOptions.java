package com.vividsolutions.jcs.conflate.roads.match;

import java.io.Serializable;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;

//Things to keep up to date: #clone and .java2xml
//[Jon Aquino 2004-03-03]
public class StandaloneOptions implements Serializable, Cloneable {

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

    public void setDistanceTolerance(double distanceTolerance) {
        this.distanceTolerance = distanceTolerance;
    }

    private double distanceTolerance = 10.0;


}