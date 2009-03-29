package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.ResultStateRules;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.NeighbourhoodList;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_ZoomToSelectedItemsPlugIn;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class FindClosestRoadSegmentPlugIn extends AbstractPlugIn {
	private Mode mode;
	public String getName() {
		return mode.getName() + " " + criterion.getName() + " Road Segment";
	}
	public static interface Criterion {
		public boolean satisfiedBy(SourceRoadSegment roadSegment);
		public Envelope areaOfInterest(SourceRoadSegment roadSegment);
		public String getName();
	}

	public static Criterion inconsistentCriterion = new Criterion() {
		public boolean satisfiedBy(SourceRoadSegment roadSegment) {
			return roadSegment.getResultState() == ResultState.INCONSISTENT
					&& (intersection(roadSegment) != null || inconsistentNeighbourhood(roadSegment) != null);
		}
		private Collection inconsistentNeighbourhood(
				SourceRoadSegment roadSegment) {
			for (Iterator i = roadSegment.getResultStateDescription()
					.getBlackboard().getProperties().values().iterator(); i
					.hasNext();) {
				Object value = (Object) i.next();
				//HACK: Assume that inconsistent neighbourhoods are the only
				//Collections of Coordinates in the ResultStateDescription.
				//Hack is safe because it will become obvious if the assumption
				//becomes invalid in the future (we'll start getting
				//ClassCastExceptions). [Jon Aquino 2004-06-07]
				//Slightly cleaner alternative would be to have Neighbourhood
				//objects [Jon Aquino 2004-06-07]
				if (!(value instanceof Collection)) {
					continue;
				}
				if (NeighbourhoodList.postponedInconsistentNeighbourhoods(
						roadSegment.getNetwork().getSession()).contains(
						new TreeSet((Collection) value))) {
					continue;
				};
				return (Collection) value;
			}
			return null;
		}
		public String getName() {
			return "Inconsistent";
		}
		public Envelope areaOfInterest(SourceRoadSegment roadSegment) {
			if (null != roadSegment.getResultStateDescription().getBlackboard()
					.get(ResultStateRules.INTERSECTION_KEY)) {
				return new Envelope(intersection(roadSegment));
			}
			return envelope(inconsistentNeighbourhood(roadSegment));
		}

		private Coordinate intersection(SourceRoadSegment roadSegment) {
			return (Coordinate) roadSegment.getResultStateDescription()
					.getBlackboard().get(ResultStateRules.INTERSECTION_KEY);
		}
		private Envelope envelope(Collection coordinates) {
			Envelope envelope = new Envelope();
			for (Iterator i = coordinates.iterator(); i.hasNext();) {
				Coordinate coordinate = (Coordinate) i.next();
				envelope.expandToInclude(coordinate);
			}
			return envelope;
		}
	};

	public static Criterion unknownCriterion = new Criterion() {
		public boolean satisfiedBy(SourceRoadSegment roadSegment) {
			return roadSegment.getState() == SourceState.UNKNOWN;
		}
		public String getName() {
			return "Unknown";
		}
		public Envelope areaOfInterest(SourceRoadSegment roadSegment) {
			return roadSegment.getApparentLine().getEnvelopeInternal();
		}
	};
	public static Criterion adjustedCriterion = new Criterion() {
		public boolean satisfiedBy(SourceRoadSegment roadSegment) {
			return roadSegment.isAdjusted();
		}
		public String getName() {
			return "Adjusted";
		}
		public Envelope areaOfInterest(SourceRoadSegment roadSegment) {
			return roadSegment.getApparentLine().getEnvelopeInternal();
		}
	};

	public FindClosestRoadSegmentPlugIn(Criterion criterion, Mode mode) {
		this.criterion = criterion;
		this.mode = mode;
	}

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		Point centre = new GeometryFactory().createPoint(EnvelopeUtil
				.centre(context.getLayerViewPanel().getViewport()
						.getEnvelopeInModelCoordinates()));
		SourceRoadSegment closestRoadSegment = null;
		double closestDistance = -1;
		for (Iterator i = FUTURE_CollectionUtil.concatenate(ToolboxModel
				.instance(context).getSession().getSourceNetwork(0).getGraph()
				.getEdges().iterator(), ToolboxModel.instance(context)
				.getSession().getSourceNetwork(1).getGraph().getEdges()
				.iterator()); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			if (!criterion.satisfiedBy(roadSegment)) {
				continue;
			}
			if (closestRoadSegment == null
					|| roadSegment.getApparentLine().isWithinDistance(centre,
							closestDistance)) {
				closestRoadSegment = roadSegment;
				closestDistance = centre
						.distance(roadSegment.getApparentLine());
			}
		}
		if (closestRoadSegment == null) {
			context.getWorkbenchFrame().warnUser(
					ErrorMessages.findClosestRoadSegmentPlugIn_noRoadSegments);
			return false;
		}
		mode.find(closestRoadSegment, this, context);
		new FUTURE_ZoomToSelectedItemsPlugIn().flash(Collections
				.singleton(closestRoadSegment.getApparentLine()), context
				.getLayerViewPanel());
		return true;
	}

	public static abstract class Mode {
		private Mode() {
		}
		public abstract String getName();
		public abstract void find(SourceRoadSegment roadSegment,
				FindClosestRoadSegmentPlugIn plugIn, PlugInContext context)
				throws NoninvertibleTransformException;
		public static Mode PAN = new Mode() {
			public String getName() {
				return "Pan To";
			}
			public void find(SourceRoadSegment roadSegment,
					FindClosestRoadSegmentPlugIn plugIn, PlugInContext context)
					throws NoninvertibleTransformException {
				FindClosestRoadSegmentPlugIn.panTo(EnvelopeUtil
						.centre(plugIn.criterion.areaOfInterest(roadSegment)),
						context.getLayerViewPanel());
			}
		};
		public static Mode ZOOM = new Mode() {
			public String getName() {
				return "Zoom To";
			}
			public void find(SourceRoadSegment roadSegment,
					FindClosestRoadSegmentPlugIn plugIn, PlugInContext context)
					throws NoninvertibleTransformException {
				//ZoomToSelectedItemsPlugIn#zoom nicely handles zero-dimension
				//destinations (points). [Jon Aquino 2004-01-28]
				new FUTURE_ZoomToSelectedItemsPlugIn().zoomWithoutFlashing(
						Collections.singleton(EnvelopeUtil
								.toGeometry(plugIn.criterion
										.areaOfInterest(roadSegment))), context
								.getLayerViewPanel());
			}
		};
	}

	public static void panTo(Coordinate destination, LayerViewPanel panel)
			throws NoninvertibleTransformException {
		if (!FUTURE_ZoomToSelectedItemsPlugIn.panSufficientlyGreat(destination,
				panel)) {
			return;
		}
		Envelope envelope = panel.getViewport().getEnvelopeInModelCoordinates();
		FUTURE_ZoomToSelectedItemsPlugIn.animatePan(EnvelopeUtil
				.centre(envelope), destination, panel);
		EnvelopeUtil.translate(envelope, CoordUtil.subtract(destination,
				EnvelopeUtil.centre(envelope)));
		panel.getViewport().zoom(envelope);
	}

	private Criterion criterion;

	public static MultiEnableCheck createEnableCheck(WorkbenchContext context) {
		EnableCheckFactory checkFactory = new EnableCheckFactory(context);
		return new MultiEnableCheck()
				.add(
						checkFactory
								.createWindowWithLayerViewPanelMustBeActiveCheck())
				.add(
						SpecifyRoadFeaturesTool
								.createConflationSessionMustBeStartedCheck(context));
	}
	private static Image createImageWithMargins(LayerViewPanel panel) {
		Image image = panel.createBlankPanelImage();
		panel.paint(image.getGraphics());
		Image imageWithMargins = new BufferedImage(panel.getWidth() * 3, panel
				.getHeight() * 3, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) imageWithMargins.getGraphics();
		graphics.setColor(Color.white);
		graphics.fill(new Rectangle2D.Double(0, 0, panel.getWidth() * 3, panel
				.getHeight() * 3));
		graphics.drawImage(image, panel.getWidth(), panel.getHeight(), panel);
		return imageWithMargins;
	}
}