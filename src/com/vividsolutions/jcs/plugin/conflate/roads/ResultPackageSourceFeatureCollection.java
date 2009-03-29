package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.jump.FUTURE_AttributeMapping;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;

public class ResultPackageSourceFeatureCollection implements FeatureCollection {
	private FeatureSchema featureSchema = new FeatureSchema();

	private HashMap indexMap = new HashMap();

	private List features;

	private FeatureCollection featureCollection;

	private boolean apparentLines;

	public ResultPackageSourceFeatureCollection(
			FeatureCollection featureCollection,
			Collection attributesToInclude, boolean apparentLines) {
		this.featureCollection = featureCollection;
		this.apparentLines = apparentLines;
		//Handle unlikely scenario of attribute name matching
		//conflation attribute name [Jon Aquino 2004-05-04]
		//Unfortunately, we are not handling this for the result dataset
		//[Jonathan Aquino 2004-07-06]
		for (int i = 0; i < featureCollection.getFeatureSchema()
				.getAttributeCount(); i++) {
			if (!FUTURE_CollectionUtil.concatenate(
					resultPackageSourceConflationAttributeNames(),
					attributesToInclude).contains(
					featureCollection.getFeatureSchema().getAttributeName(i))) {
				continue;
			}
			featureSchema.addAttribute(FUTURE_AttributeMapping.ensureUnique(
					shapefileName(featureCollection.getFeatureSchema()
							.getAttributeName(i)),
					attributeNames(featureSchema)), featureCollection
					.getFeatureSchema().getAttributeType(i));
			indexMap.put(new Integer(featureSchema.getAttributeCount() - 1),
					new Integer(i));
		}
	}

	private String shapefileName(String name) {
		if (!resultPackageSourceConflationAttributeNames().contains(name)) {
			return name;
		}
		//Slight risk: a non-conflation attribute with the same name as a
		//conflation attribute will get here. Anyway, nothing bad will happen
		// to it -- if it is over 10 characters it will get renamed to a shorter
		// shapefile-safe name, which is probably a good thing anyway
		//[Jonathan Aquino 2004-07-06]
		return conflationAttribute(name).getShapefileName();
	}

	private SourceFeature.ConflationAttribute conflationAttribute(String name) {
		for (Iterator i = SourceFeature.CONFLATION_ATTRIBUTES.iterator(); i
				.hasNext();) {
			SourceFeature.ConflationAttribute conflationAttribute = (SourceFeature.ConflationAttribute) i
					.next();
			if (conflationAttribute.getName().equals(name)) {
				return conflationAttribute;
			}
		}
		Assert.shouldNeverReachHere();
		return null;
	}

	private Collection resultPackageSourceConflationAttributeNames() {
		return GenerateResultLayerPlugIn.conflationAttributeNames(false);
	}

	private Collection attributeNames(FeatureSchema myFeatureSchema) {
		Collection attributeNames = new ArrayList();
		for (int i = 0; i < myFeatureSchema.getAttributeCount(); i++) {
			attributeNames.add(myFeatureSchema.getAttributeName(i));
		}
		return attributeNames;
	}

	public FeatureSchema getFeatureSchema() {
		return featureSchema;
	}

	public List getFeatures() {
		if (features == null) {
			features = (List) CollectionUtil.collect(featureCollection
					.getFeatures(), new Block() {
				public Object yield(Object feature) {
					return new ResultPackageSourceFeature(
							(SourceFeature) feature);
				}
			});
		}
		return features;
	}

	public List query(Envelope envelope) {
		throw new UnsupportedOperationException();
	}

	private class ResultPackageSourceFeature implements Feature {
		private SourceFeature feature;

		public ResultPackageSourceFeature(SourceFeature feature) {
			this.feature = feature;
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
			return feature
					.getAttribute(((Integer) indexMap.get(new Integer(i)))
							.intValue());
		}

		public Object getAttribute(String name) {
			throw new UnsupportedOperationException();
		}

		public String getString(int attributeIndex) {
			return LangUtil.ifNull(getAttribute(attributeIndex), "").toString();
		}

		public int getInteger(int attributeIndex) {
			throw new UnsupportedOperationException();
		}

		public double getDouble(int attributeIndex) {
			throw new UnsupportedOperationException();
		}

		public String getString(String attributeName) {
			throw new UnsupportedOperationException();
		}

		public Geometry getGeometry() {
			return apparentLines ? feature.getRoadSegment().getApparentLine()
					: feature.getRoadSegment().getLine();
		}

		public FeatureSchema getSchema() {
			return featureSchema;
		}

		public Object clone() {
			throw new UnsupportedOperationException();
		}

		public Feature clone(boolean deep) {
			return AbstractBasicFeature.clone(this, deep);
		}

		public Object[] getAttributes() {
			throw new UnsupportedOperationException();
		}

		public int compareTo(Object o) {
			throw new UnsupportedOperationException();
		}
	}

	public Envelope getEnvelope() {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return featureCollection.size();
	}

	public boolean isEmpty() {
		return featureCollection.isEmpty();
	}

	public Iterator iterator() {
		return getFeatures().iterator();
	}

	public void add(Feature feature) {
		throw new UnsupportedOperationException();
	}

	public void addAll(Collection features) {
		throw new UnsupportedOperationException();
	}

	public void removeAll(Collection features) {
		throw new UnsupportedOperationException();
	}

	public void remove(Feature feature) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Collection remove(Envelope env) {
		throw new UnsupportedOperationException();
	}
}