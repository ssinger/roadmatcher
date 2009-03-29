package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class NodeConstraintStyle implements Style {
	public boolean isEnabled() {
		return enabled;
	}

	public void initialize(Layer layer) {
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.shouldNeverReachHere();

			return null;
		}
	}

	public void paint(Feature f, Graphics2D g, Viewport viewport)
			throws Exception {
		paintSourceFeature((SourceFeature) f, g, viewport);
	}

	private void paintSourceFeature(SourceFeature f, Graphics2D g,
			Viewport viewport) throws NoninvertibleTransformException {
		if (f.getRoadSegment().isStartNodeConstrained()) {
			paint(f.getRoadSegment().getApparentStartCoordinate(), g, viewport);
		}
		if (f.getRoadSegment().isEndNodeConstrained()) {
			paint(f.getRoadSegment().getApparentEndCoordinate(), g, viewport);
		}
	}

	private void paint(Coordinate c, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		if (!viewport.getEnvelopeInModelCoordinates().contains(c)) {
			return;
		}
		Point2D p = viewport.toViewPoint(c);
		g.drawImage(IMAGE, (int) p.getX() - 5, (int) p.getY() - 6, null);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean enabled = true;

	//com.vividsolutions.jcs.plugin.conflate.roads.NodeConstraintStyle.IMAGE = com.vividsolutions.jcs.plugin.conflate.roads.NodeConstraintStyle.SMALL_LOCK_IMAGE;
	//com.vividsolutions.jcs.plugin.conflate.roads.NodeConstraintStyle.IMAGE = com.vividsolutions.jcs.plugin.conflate.roads.NodeConstraintStyle.BIG_LOCK_IMAGE;
	public static final Image SMALL_LOCK_IMAGE = SpecifyRoadFeaturesTool
			.createIcon("small-lock.png").getImage();
	public static final Image BIG_LOCK_IMAGE = SpecifyRoadFeaturesTool
			.createIcon("big-lock.png").getImage();
	public static Image IMAGE = BIG_LOCK_IMAGE;

	public static boolean coordinatesEqual(Geometry geometry) {
		//Coordinates may be expensive to build (e.g. GeometryCollections) ,
		//so build it once. [Jon Aquino]
		Coordinate[] coordinates = geometry.getCoordinates();
		for (int i = 1; i < coordinates.length; i++) {
			if (!coordinates[i].equals(coordinates[0])) {
				return false;
			}
		}
		return true;
	}

}