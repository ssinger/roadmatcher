package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public abstract class AbstractInconsistentEndpointStyle implements Style {
	private String endAssociatedDataKey;

	private String startAssociatedDataKey;

	private Set endpointsPainted;

	public AbstractInconsistentEndpointStyle(String startAssociatedDataKey,
			String endAssociatedDataKey) {
		this.startAssociatedDataKey = startAssociatedDataKey;
		this.endAssociatedDataKey = endAssociatedDataKey;
	}

	protected Color getInconsistentColour(Viewport viewport) {
		return HighlightManager.instance(
				RoadStyleUtil.instance().context(viewport)).getColourScheme()
				.getInconsistentColour();
	}
	
	protected Color getPostponedInconsistentColour(Viewport viewport) {
		return HighlightManager.instance(
				RoadStyleUtil.instance().context(viewport)).getColourScheme()
				.getPostponedInconsistentColour();
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.shouldNeverReachHere();

			return null;
		}
	}

	public void initialize(Layer layer) {
		endpointsPainted = new HashSet();
	}

	public void paint(Feature f, Graphics2D g, Viewport viewport)
			throws Exception {
		if (((SourceFeature) f).getRoadSegment().getResultState() != ResultState.INCONSISTENT) {
			return;
		}
		paint(startCoordinate(((SourceFeature) f).getRoadSegment()),
				((SourceFeature) f).getRoadSegment(), startAssociatedDataKey,
				g, viewport);
		paint(endCoordinate(((SourceFeature) f).getRoadSegment()),
				((SourceFeature) f).getRoadSegment(), endAssociatedDataKey, g,
				viewport);
	}

	protected Coordinate endCoordinate(SourceRoadSegment roadSegment) {
		return roadSegment.getApparentEndCoordinate();
	}

	protected Coordinate startCoordinate(SourceRoadSegment roadSegment) {
		return roadSegment.getApparentStartCoordinate();
	}

	private void paint(Coordinate endpoint, SourceRoadSegment roadSegment,
			String associatedDataKey, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		if (endpointsPainted.contains(endpoint)) {
			return;
		}
		if (roadSegment.getResultStateDescription().get(associatedDataKey) == null) {
			return;
		}
		if (!viewport.getEnvelopeInModelCoordinates().contains(endpoint)) {
			return;
		}
		paint(endpoint, roadSegment.getResultStateDescription().get(
				associatedDataKey), g, viewport);
		endpointsPainted.add(endpoint);
	}

	protected abstract void paint(Coordinate endpoint, Object associatedData,
			Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

}