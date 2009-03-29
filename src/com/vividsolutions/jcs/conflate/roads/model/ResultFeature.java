package com.vividsolutions.jcs.conflate.roads.model;
import com.vividsolutions.jcs.jump.FUTURE_AbstractBasicFeature;
import com.vividsolutions.jcs.jump.FUTURE_AttributeMapping;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
/**
 * @deprecated however, this class has useful logic for attribute transfer
 */
public class ResultFeature implements Feature {
    private ResultRoadSegment roadSegment;
    public ResultFeature(ResultRoadSegment roadSegment) {
        this.roadSegment = roadSegment;
    }
    public void setAttributes(Object[] attributes) {
        throw new UnsupportedOperationException();
    }
    public void setSchema(FeatureSchema schema) {
        throw new UnsupportedOperationException();
    }
    private int id = FeatureUtil.nextID();
    public int getID() {
        return id;
    }
    public void setAttribute(int attributeIndex, Object newAttribute) {
        throw new UnsupportedOperationException();
    }
    public void setAttribute(String attributeName, Object newAttribute) {
        throw new UnsupportedOperationException();
    }
    public void setGeometry(Geometry geometry) {
        throw new UnsupportedOperationException();
    }
    public Object getAttribute(int i) {
        return schema().isFromA(i) ? getOriginalFeature0().getAttribute(
                schema().toAOldAttributeIndex(i)) : getOriginalFeature1()
                .getAttribute(schema().toBOldAttributeIndex(i));
    }
    public Object getAttribute(String name) {
        return getAttribute(schema().getAttributeIndex(name));
    }
    public String getString(int i) {
        return schema().isFromA(i) ? getOriginalFeature0().getString(
                schema().toAOldAttributeIndex(i)) : getOriginalFeature1()
                .getString(schema().toBOldAttributeIndex(i));
    }
    public int getInteger(int i) {
        return schema().isFromA(i) ? getOriginalFeature0().getInteger(
                schema().toAOldAttributeIndex(i)) : getOriginalFeature1()
                .getInteger(schema().toBOldAttributeIndex(i));
    }
    public double getDouble(int i) {
        return schema().isFromA(i) ? getOriginalFeature0().getDouble(
                schema().toAOldAttributeIndex(i)) : getOriginalFeature1()
                .getDouble(schema().toBOldAttributeIndex(i));
    }
    public String getString(String name) {
        return getString(schema().getAttributeIndex(name));
    }
    public Geometry getGeometry() {
        return roadSegment.getLine();
    }
    public FeatureSchema getSchema() {
        return schema();
    }
    private FUTURE_AttributeMapping.CombinedSchema schema() {
        return (FUTURE_AttributeMapping.CombinedSchema) roadSegment
                .getNetwork().getFeatureCollection().getFeatureSchema();
    }
    public Object clone() {
        return clone(true);
    }
    public Object[] getAttributes() {
        Object[] attributes = new Object[getSchema().getAttributeCount()];
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = getAttribute(i);
        }
        return attributes;
    }
    public int compareTo(Object o) {
        return FUTURE_AbstractBasicFeature.compare(this, (Feature) o);
    }
    public ResultRoadSegment getRoadSegment() {
        return roadSegment;
    }
    public Feature clone(boolean deep) {
        return FUTURE_AbstractBasicFeature.clone(this, true);
    }
    public static FUTURE_AttributeMapping.CombinedSchema createSchema(
            FeatureSchema schema0, FeatureSchema schema1) {
        return new FUTURE_AttributeMapping(schema0, schema1).createSchema(
                schema0, schema1, schema0.getAttributeName(schema0
                        .getGeometryIndex()));
    }
    private Feature getOriginalFeature0() {
        throw new UnsupportedOperationException();
    }
    private Feature getOriginalFeature1() {
        throw new UnsupportedOperationException();
    }
}