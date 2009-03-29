package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;

public class FUTURE_AbstractBasicFeature {
    public static int compare(Feature a, Feature b) {
        int geometryComparison = a.getGeometry().compareTo(((Feature) b).getGeometry());
        if (geometryComparison != 0) { return geometryComparison; }
        if (a == b) { return 0; }
        //The features do not refer to the same object, so try to return something consistent. [Jon Aquino]
        if (a.getID() != ((Feature) b).getID()) { return a.getID() - ((Feature) b).getID(); }
        //The ID is hosed. Last gasp: hope the hash codes are different. [Jon Aquino]
        if (a.hashCode() != b.hashCode()) { return a.hashCode() - b.hashCode(); }
        Assert.shouldNeverReachHere();
        return -1;        
    }
    public static BasicFeature clone(Feature feature, boolean deep) {
        BasicFeature clone = new BasicFeature(feature.getSchema());
        for (int i = 0; i < feature.getSchema().getAttributeCount(); i++) {
            if (feature.getSchema().getAttributeType(i) == AttributeType.GEOMETRY) {
                clone.setAttribute(i, deep ? feature.getGeometry().clone() : feature.getGeometry());
            } else {
                clone.setAttribute(i, feature.getAttribute(i));
            }
        }
        return clone;        
    }    
}
