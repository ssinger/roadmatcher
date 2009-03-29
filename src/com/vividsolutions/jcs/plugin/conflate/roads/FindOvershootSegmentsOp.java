package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

public class FindOvershootSegmentsOp {
	public FeatureCollection findOvershoots(RoadNetwork network) {
		// Don't use a Set -- we don't want any segments eliminated.
		// [Jon Aquino 2004-09-27]
		List overshootFeatures = new ArrayList();
		for (Iterator i = network.getGraph().getEdges().iterator(); i.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (segment.getStartNode().getDegree() == 1
					&& segment.getEndNode().getDegree() >= 3) {
				overshootFeatures.add(overshootFeature(segment));
			}
			if (segment.getEndNode().getDegree() == 1
					&& segment.getStartNode().getDegree() >= 3) {
				overshootFeatures.add(overshootFeature(segment));
			}
		}
		Collections.sort(overshootFeatures, new Comparator() {
			public int compare(Object a, Object b) {
				double x = ((Feature) a).getGeometry().getLength()
						- ((Feature) b).getGeometry().getLength();
				return x > 0 ? 1 : x < 0 ? -1 : 0;
			}
		});
		return new FeatureDataset(overshootFeatures, SCHEMA);
	}

	private Feature overshootFeature(SourceRoadSegment segment) {
		Feature feature = new BasicFeature(SCHEMA);
		feature.setAttribute("GEOMETRY", segment.getApparentLine().clone());
		feature.setAttribute("LENGTH", new Double(segment.getApparentLine()
				.getLength()));
		return feature;
	}

	private static final FeatureSchema SCHEMA = new FeatureSchema() {
		{
			addAttribute("GEOMETRY", AttributeType.GEOMETRY);
			addAttribute("LENGTH", AttributeType.DOUBLE);
		}
	};
}