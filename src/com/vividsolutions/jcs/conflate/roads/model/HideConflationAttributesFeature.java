package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

public class HideConflationAttributesFeature implements Feature, Serializable {

	private Feature feature;

	private Map newIndexToOldIndexMap;

	private FeatureSchema schema;

	public HideConflationAttributesFeature(Feature feature,
			Map newIndexToOldIndexMap, FeatureSchema schema) {
		this.feature = feature;
		this.newIndexToOldIndexMap = newIndexToOldIndexMap;
		this.schema = schema;
	}

	public void setAttributes(Object[] attributes) {
		throw new UnsupportedOperationException();
	}

	public void setSchema(FeatureSchema schema) {
		throw new UnsupportedOperationException();
	}

	public int getID() {
		throw new UnsupportedOperationException();
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
		return feature.getAttribute(newIndexToOldIndex(i));
	}

	public Object getAttribute(String name) {
		return getAttribute(schema.getAttributeIndex(name));
	}

	public String getString(int attributeIndex) {
		return feature.getString(newIndexToOldIndex(attributeIndex));
	}

	private int newIndexToOldIndex(int newIndex) {
		return ((Integer) newIndexToOldIndexMap.get(new Integer(newIndex)))
				.intValue();
	}

	public int getInteger(int attributeIndex) {
		return feature.getInteger(newIndexToOldIndex(attributeIndex));
	}

	public double getDouble(int attributeIndex) {
		return feature.getDouble(newIndexToOldIndex(attributeIndex));
	}

	public String getString(String attributeName) {
		return getString(schema.getAttributeIndex(attributeName));
	}

	public Geometry getGeometry() {
		return feature.getGeometry();
	}

	public FeatureSchema getSchema() {
		return schema;
	}

	public Object clone() {
		throw new UnsupportedOperationException();
	}

	public Feature clone(boolean deep) {
		throw new UnsupportedOperationException();
	}

	public Object[] getAttributes() {
		throw new UnsupportedOperationException();
	}

	public int compareTo(Object o) {
		throw new UnsupportedOperationException();
	}

}