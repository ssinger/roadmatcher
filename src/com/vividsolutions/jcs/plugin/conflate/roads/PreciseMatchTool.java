package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import com.vividsolutions.jcs.conflate.roads.match.RoadSegmentsMutualBestMatcher;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.*;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.CommitTool.Mode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;

public class PreciseMatchTool extends NClickTool {
	private static final int TOLERANCE_IN_PIXELS = 4;

	private Shape firstShape = null;

	private Mode mode;

	private static interface State {
		void addRoadSegment(SourceRoadSegment roadSegment, PreciseMatchTool tool);

		boolean canSearch(Layer layer, PreciseMatchTool tool);

		String getNoFeatureHereWarning(PreciseMatchTool tool);
	}

	public PreciseMatchTool(CommitTool.Mode mode, WorkbenchContext context) {
		super(2);
		this.mode = mode;
		setStroke(new BasicStroke(5));
		this.context = context;
		cursor = FUTURE_GUIUtil
				.createCursorFromIcon("precise-match-tool-button.png");
		icon = SpecifyRoadFeaturesTool
				.createIcon("precise-match-tool-button.png");
	}

	protected void add(Coordinate c) {
		if (!SpecifyRoadFeaturesTool.checkConflationSessionStarted(context)) {
			cancelGesture();
			return;
		}
		SourceRoadSegment closestRoadSegment = closestRoadSegment(c);
		if (closestRoadSegment == null) {
			SpecifyRoadFeaturesTool.warnUser(getState()
					.getNoFeatureHereWarning(this), context);
			cancelGesture();
			return;
		}
		getState().addRoadSegment(closestRoadSegment, this);
		super.add(c);
	}

	protected Shape getShape() throws NoninvertibleTransformException {
		return firstShape;
	}

	public void cancelGesture() {
		firstShape = null;
		super.cancelGesture();
	}

	protected void finishGesture() throws Exception {
		firstShape = null;
		super.finishGesture();
	}

	private SourceRoadSegment closestRoadSegment(Coordinate c) {
		Point p = new GeometryFactory().createPoint(c);
		SourceRoadSegment closestRoadSegment = null;
		double buffer = TOLERANCE_IN_PIXELS
				/ getPanel().getViewport().getScale();
		Map layerToSpecifiedFeaturesMap;
		try {
			layerToSpecifiedFeaturesMap = SpecifyFeaturesTool
					.layerToSpecifiedFeaturesMap(
							FUTURE_CollectionUtil.list(
									modelForCurrentTask().getSourceLayer(0),
									modelForCurrentTask().getSourceLayer(1))
									.iterator(), new Envelope(c.x - buffer, c.x
									+ buffer, c.y - buffer, c.y + buffer));
		} catch (NoninvertibleTransformException e) {
			SpecifyRoadFeaturesTool.warnUser(e.toString(), context);
			return null;
		}
		for (Iterator i = layerToSpecifiedFeaturesMap.keySet().iterator(); i
				.hasNext();) {
			Layer layer = (Layer) i.next();
			if (!getState().canSearch(layer, this)) {
				continue;
			}
			for (Iterator j = ((Collection) layerToSpecifiedFeaturesMap
					.get(layer)).iterator(); j.hasNext();) {
				SourceFeature sourceFeature = (SourceFeature) j.next();
				if (closestRoadSegment == null
						|| sourceFeature.getRoadSegment().getApparentLine()
								.distance(p) < closestRoadSegment
								.getApparentLine().distance(p)) {
					closestRoadSegment = sourceFeature.getRoadSegment();
				}
			}
		}
		return closestRoadSegment;
	}

	private ToolboxModel modelForCurrentTask() {
		return ToolboxModel.instance(context.getLayerManager(), context);
	}

	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		Assert.isTrue(getState() == FULLY_SPECIFIED);
		CommitTool.matchUndoably(mode.reference(first, second), first == mode
				.reference(first, second) ? second : first, getClass()
				+ " - DO NOT SHOW AGAIN", context, getName());
	}

	public Cursor getCursor() {
		return cursor;
	}

	public Icon getIcon() {
		return icon;
	}

	private State getState() {
		return getCoordinates().size() == 0 ? WAITING_FOR_FIRST
				: getCoordinates().size() == 1 ? WAITING_FOR_SECOND
						: getCoordinates().size() == 2 ? FULLY_SPECIFIED
								: (State) FUTURE_Assert.throwAssertionFailure();
	}

	private Cursor cursor;

	private SourceRoadSegment first;

	private ImageIcon icon;

	private SourceRoadSegment second;

	private WorkbenchContext context;

	private static final State FULLY_SPECIFIED = new State() {
		public void addRoadSegment(SourceRoadSegment roadSegment,
				PreciseMatchTool tool) {
			Assert.shouldNeverReachHere();
		}

		public boolean canSearch(Layer layer, PreciseMatchTool tool) {
			Assert.shouldNeverReachHere();
			return false;
		}

		public String getNoFeatureHereWarning(PreciseMatchTool tool) {
			return (String) FUTURE_Assert.throwAssertionFailure();
		}
	};

	private static final State WAITING_FOR_FIRST = new State() {
		public void addRoadSegment(SourceRoadSegment closestRoadSegment,
				PreciseMatchTool tool) {
			tool.first = closestRoadSegment;
			try {
				tool.firstShape = FUTURE_StyleUtil._toShape(tool.first
						.getApparentLine(), tool.getPanel().getViewport());
			} catch (NoninvertibleTransformException e) {
				//Not critical. Eat it. [Jon Aquino 12/4/2003]
			}
		}

		public boolean canSearch(Layer layer, PreciseMatchTool tool) {
			Assert
					.isTrue(layer.getFeatureCollectionWrapper()
							.getUltimateWrappee() instanceof RoadNetworkFeatureCollection);
			return true;
		}

		public String getNoFeatureHereWarning(PreciseMatchTool tool) {
			return FUTURE_StringUtil.substitute(
					ErrorMessages.preciseMatchTool_waitingForFirst_noFeatures,
					new Object[] {
							tool.modelForCurrentTask().getSourceLayer(0)
									.getName(),
							tool.modelForCurrentTask().getSourceLayer(1)
									.getName() });
		}
	};

	private static final State WAITING_FOR_SECOND = new State() {
		public void addRoadSegment(SourceRoadSegment roadSegment,
				PreciseMatchTool tool) {
			tool.second = roadSegment;
		}

		public boolean canSearch(Layer layer, PreciseMatchTool tool) {
			return ((RoadNetworkFeatureCollection) layer
					.getFeatureCollectionWrapper().getUltimateWrappee())
					.getNetwork() != tool.first.getNetwork();
		}

		public String getNoFeatureHereWarning(PreciseMatchTool tool) {
			return FUTURE_StringUtil.substitute(
					ErrorMessages.preciseMatchTool_waitingForSecond_noFeatures,
					new Object[] { tool.modelForCurrentTask().getSourceLayer(
							1 - tool.first.getNetworkID()).getName() });
		}
	};
}