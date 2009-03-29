package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.ImageIcon;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.*;

/**
 * If the user clicks on the endpoint of a road segment, this wrapper delegates
 * to another CursorTool (first setting that CursorTool's road segment).
 */
public abstract class RoadSegmentEndpointGrabberTool extends DelegatingTool {
	private CursorTool roadSegmentTool;

	private Block roadSegmentSetter;

	public RoadSegmentEndpointGrabberTool(CursorTool roadSegmentTool,
			Block roadSegmentSetter) {
		super(new DummyTool());
		this.roadSegmentTool = roadSegmentTool;
		this.roadSegmentSetter = roadSegmentSetter;
	}

	private WorkbenchContext context;

	private CursorTool noEndpointsHereTool = new DummyTool() {
		public void mousePressed(MouseEvent e) {
			getContext().getWorkbench().getFrame().warnUser(
					ErrorMessages.oneDragAdjustEndpointTool_noEndpoints);
		}
	};

	private LayerViewPanel panel;

	public Cursor getCursor() {
		return GUIUtil.createCursorFromIcon(((ImageIcon) getIcon()).getImage());
	}

	public void activate(LayerViewPanel layerViewPanel) {
		super.activate(layerViewPanel);
		this.panel = layerViewPanel;
	}

	public void mousePressed(final MouseEvent e) {
		try {
			if (!SpecifyRoadFeaturesTool
					.checkConflationSessionStarted(getContext())) {
				return;
			}
			SourceRoadSegment roadSegment = roadSegmentWithClosestEndpoint(getContext()
					.getLayerViewPanel().getViewport().toModelCoordinate(
							e.getPoint()));
			if (roadSegment == null) {
				setDelegate(getNoEndpointsHereTool());
			} else {
				setDelegate(roadSegmentTool);
				roadSegmentSetter.yield(roadSegmentTool, roadSegment);
			}
			super.mousePressed(e);
		} catch (Exception x) {
			getContext().getErrorHandler().handleThrowable(x);
		}
	}

	private Collection onlyAdjustedIfAny(Collection features) {
		return onlyConformistsIfAny(features, new Block() {
			public Object yield(Object feature) {
				return Boolean.valueOf(((SourceFeature) feature)
						.getRoadSegment().isAdjusted());
			}
		});
	}

	private Collection onlyTheseIfAny(final SourceState state,
			Collection features) {
		return onlyTheseIfAny(new SourceState[] { state }, features);
	}

	private Collection onlyTheseIfAny(final SourceState states[],
			Collection features) {
		return onlyConformistsIfAny(features, new Block() {
			public Object yield(Object feature) {
				return Boolean.valueOf(((SourceFeature) feature)
						.getRoadSegment().getState().indicates(states));
			}
		});
	}

	private Collection ifEmpty(Collection x, Collection alternative) {
		return !x.isEmpty() ? x : alternative;
	}

	private Collection onlyConformistsIfAny(Collection features, Block criterion) {
		return ifEmpty(CollectionUtil.select(features, criterion), features);
	}

	protected SourceRoadSegment roadSegmentWithClosestEndpoint(Coordinate click)
			throws NoninvertibleTransformException {
		SourceRoadSegment roadSegmentWithClosestEndpoint = null;
		// Before passing nearby features to the filter, remove
		// features without a nearby endpoint. Otherwise you get
		// the following situation: user tries to grab an endpoint but
		// gets a "no nodes here" message because the filter preferred
		// a nearby segment without any nearby endpoints. [Jon Aquino
		// 2004-08-06]
		for (Iterator i = filter(
				featuresHavingClosestEndpointWithinTolerance(
						nearbyFeatures(click), click), click).iterator(); i
				.hasNext();) {
			SourceFeature candidateFeature = (SourceFeature) i.next();
			if (closestEndpointWithinTolerance(candidateFeature
					.getRoadSegment(), click) == null) {
				continue;
			}
			if (roadSegmentWithClosestEndpoint == null
					|| closestEndpointWithinTolerance(
							candidateFeature.getRoadSegment(), click).distance(
							click) < closestEndpointWithinTolerance(
							roadSegmentWithClosestEndpoint, click).distance(
							click)) {
				roadSegmentWithClosestEndpoint = candidateFeature
						.getRoadSegment();
			}
		}
		return roadSegmentWithClosestEndpoint;
	}

	private Collection featuresHavingClosestEndpointWithinTolerance(
			Collection features, final Coordinate click) {
		return CollectionUtil.select(features, new Block() {
			public Object yield(Object feature) {
				return Boolean
						.valueOf(closestEndpointWithinTolerance(
								((SourceFeature) feature).getRoadSegment(),
								click) != null);
			}
		});
	}

	protected Collection filter(Collection features, Coordinate click) {
		return onlyTheseIfAny(SourceState.RETIRED, onlyTheseIfAny(
				SourceState.MATCHED_NON_REFERENCE,
				onlyTheseIfAny(SourceState.INCLUDED, onlyTheseIfAny(
						SourceState.UNKNOWN,
						onlyAdjustedIfAny(onlyEditableIfAny(onlySelectedIfAny(
								features, click)))))));
	}

	private Collection onlyEditableIfAny(Collection features) {
		return onlyConformistsIfAny(features, new Block() {
			public Object yield(Object feature) {
				return Boolean.valueOf(((SourceFeature) feature)
						.getRoadSegment().getNetwork().isEditable());
			}
		});
	}

	private Collection onlySelectedIfAny(Collection features,
			final Coordinate click) {
		return onlyConformistsIfAny(features, new Block() {
			public Object yield(Object feature) {
				return Boolean.valueOf(getContext().getLayerViewPanel()
						.getSelectionManager().getFeaturesWithSelectedItems()
						.contains(feature));
			}
		});
	}

	private Collection nearbyFeatures(Coordinate click)
			throws NoninvertibleTransformException {
		return CollectionUtil.concatenate(SpecifyFeaturesTool
				.layerToSpecifiedFeaturesMap(
						FUTURE_CollectionUtil.list(
								toolboxModel().getSourceLayer(0),
								toolboxModel().getSourceLayer(1)).iterator(),
						EnvelopeUtil.expand(new Envelope(click),
								SimpleAdjustEndpointTool.TOLERANCE
										/ getContext().getLayerViewPanel()
												.getViewport().getScale()))
				.values());
	}

	protected Coordinate closestEndpointWithinTolerance(
			SourceRoadSegment roadSegment, Coordinate c) {
		return AdjustEndpointOperation.closestEndpointWithinTolerance(
				roadSegment, c, SimpleAdjustEndpointTool.TOLERANCE
						/ getContext().getLayerViewPanel().getViewport()
								.getScale());
	}

	private ToolboxModel toolboxModel() {
		return ToolboxModel.instance(getContext().getLayerManager(),
				getContext());
	}

	protected WorkbenchContext getContext() {
		return AbstractCursorTool.workbench(panel).getContext();
	}

	protected CursorTool getNoEndpointsHereTool() {
		return noEndpointsHereTool;
	}
}