package com.vividsolutions.jcs.conflate.roads.match;

import java.io.Serializable;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;

//Things to keep up to date: #clone and .java2xml
//[Jon Aquino 2004-03-03]
public class RoadMatchOptions implements Serializable, Cloneable {

    public RoadMatchOptions() {
    }

    public Object clone() {
        try {
            RoadMatchOptions clone = (RoadMatchOptions) super.clone();
            clone.standaloneOptions = (StandaloneOptions) standaloneOptions
                    .clone();
            clone.edgeMatchOptions = (EdgeMatchOptions) edgeMatchOptions
                    .clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere(StringUtil.stackTrace(e));
            return null;
        }
    }

    public EdgeMatchOptions getEdgeMatchOptions() {
        return edgeMatchOptions;
    }

    public StandaloneOptions getStandaloneOptions() {
        return standaloneOptions;
    }

    public boolean isEdgeMatchEnabled() {
        return edgeMatchEnabled;
    }

    public boolean isStandaloneEnabled() {
        return standaloneEnabled;
    }

    public void setEdgeMatchEnabled(boolean edgeMatchEnabled) {
        this.edgeMatchEnabled = edgeMatchEnabled;
    }

    public void setStandaloneEnabled(boolean standaloneEnabled) {
        this.standaloneEnabled = standaloneEnabled;
    }

    private boolean edgeMatchEnabled = true;

    private EdgeMatchOptions edgeMatchOptions = new EdgeMatchOptions();

    private boolean standaloneEnabled = true;

    private StandaloneOptions standaloneOptions = new StandaloneOptions();


    public void setEdgeMatchOptions(EdgeMatchOptions edgeMatchOptions) {
        this.edgeMatchOptions = edgeMatchOptions;
    }
    public void setStandaloneOptions(StandaloneOptions standaloneOptions) {
        this.standaloneOptions = standaloneOptions;
    }
}