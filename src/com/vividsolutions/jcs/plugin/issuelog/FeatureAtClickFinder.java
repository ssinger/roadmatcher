package com.vividsolutions.jcs.plugin.issuelog;

import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class FeatureAtClickFinder {
	public static Feature featureAtClick(Collection featureCollections,
			final PlugInContext context) throws NoninvertibleTransformException {
		return closestFeature(click(context), closeFeatures(featureCollections,
				context));
	}

	private static Collection closeFeatures(Collection featureCollections,
			final PlugInContext context) throws NoninvertibleTransformException {
		Collection closeFeatures = new ArrayList();
		Geometry click = EnvelopeUtil.toGeometry(clickBuffer(context));
		for (Iterator i = featureCollections.iterator(); i.hasNext();) {
			FeatureCollection featureCollection = (FeatureCollection) i.next();
			for (Iterator j = featureCollection.query(
					click.getEnvelopeInternal()).iterator(); j.hasNext();) {
				Feature feature = (Feature) j.next();
				if (feature.getGeometry().intersects(click)) {
					closeFeatures.add(feature);
				}
			}
		}
		return closeFeatures;
	}

	public static Feature closestFeature(Point target, Collection features) {
		Feature closestFeature = null;
		for (Iterator j = features.iterator(); j.hasNext();) {
			Feature feature = (Feature) j.next();
			if (closestFeature == null
					|| feature.getGeometry().distance(target) < closestFeature
							.getGeometry().distance(target)) {
				closestFeature = feature;
			}
		}
		return closestFeature;
	}

	public static Envelope clickBuffer(PlugInContext context)
			throws NoninvertibleTransformException {
		return EnvelopeUtil.expand(new Envelope(context.getLayerViewPanel()
				.getViewport().toModelCoordinate(
						context.getLayerViewPanel().getLastClickedPoint())),
				BUFFER / context.getLayerViewPanel().getViewport().getScale());
	}

	private static final int BUFFER = 3;

	private static Point click(PlugInContext context)
			throws NoninvertibleTransformException {
		return new GeometryFactory().createPoint(context.getLayerViewPanel()
				.getViewport().toModelCoordinate(
						context.getLayerViewPanel().getLastClickedPoint()));
	}

	public static Feature featureAtClick(FeatureCollection featureCollection,
			PlugInContext context) throws NoninvertibleTransformException {
		return featureAtClick(Collections.singleton(featureCollection), context);
	}
}