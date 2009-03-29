package com.vividsolutions.jcs.jump;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public class FUTURE_FeatureCollectionUtil {
	public static FeatureDataset clone(final boolean cloneGeometries,
			FeatureCollection featureCollection) {
		return new FeatureDataset(CollectionUtil.collect(featureCollection
				.getFeatures(), new Block() {
			public Object yield(Object feature) {
				return ((Feature) feature).clone(cloneGeometries);
			}
		}), featureCollection.getFeatureSchema());
	}
}