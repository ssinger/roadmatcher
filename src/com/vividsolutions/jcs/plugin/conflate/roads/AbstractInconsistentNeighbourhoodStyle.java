package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.NeighbourhoodList;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;

public abstract class AbstractInconsistentNeighbourhoodStyle extends
		AbstractInconsistentEndpointStyle {

	private ConflationSession session;

	public AbstractInconsistentNeighbourhoodStyle(
			String startAssociatedDataKey, String endAssociatedDataKey) {
		super(startAssociatedDataKey, endAssociatedDataKey);
	}

	protected void paint(Coordinate endpoint, Object neighbourhood,
			final Graphics2D g, final Viewport viewport)
			throws NoninvertibleTransformException {
		Color colour = NeighbourhoodList.postponedInconsistentNeighbourhoods(
				session).contains(new TreeSet((Set) neighbourhood)) ? getPostponedInconsistentColour(viewport)
				: getInconsistentColour(viewport);
		RoadStyleUtil.instance().paintNeighbourhood(null, (Set) neighbourhood,
				colour, g, viewport);
		for (Iterator i = ((Set) neighbourhood).iterator(); i.hasNext();) {
			Coordinate neighbourhoodEndpoint = (Coordinate) i.next();
			RoadStyleUtil.instance().paintInconsistentEndpoint(
					neighbourhoodEndpoint, colour, g, viewport);
		}
	}

	public void initialize(Layer layer) {
		session = ((RoadNetworkFeatureCollection) layer
				.getFeatureCollectionWrapper().getUltimateWrappee())
				.getNetwork().getSession();
		super.initialize(layer);
	}
}