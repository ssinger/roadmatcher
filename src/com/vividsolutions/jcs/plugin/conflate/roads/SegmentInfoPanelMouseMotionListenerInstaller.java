package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.issuelog.FeatureAtClickFinder;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;

public class SegmentInfoPanelMouseMotionListenerInstaller {

	public void install(final WorkbenchContext context,
			final SegmentInfoPanel segmentInfoPanel, final Block enableLogic) {
		for (Iterator i = Arrays.asList(
				context.getWorkbench().getFrame().getDesktopPane()
						.getAllFrames()).iterator(); i.hasNext();) {
			JInternalFrame internalFrame = (JInternalFrame) i.next();
			installMouseListenerIfNecessary(internalFrame, segmentInfoPanel,
					context, enableLogic);
		}
		GUIUtil.addInternalFrameListener(context.getWorkbench().getFrame()
				.getDesktopPane(), new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				installMouseListenerIfNecessary(e.getInternalFrame(),
						segmentInfoPanel, context, enableLogic);
			}
		});
	}

	private void installMouseListenerIfNecessary(JInternalFrame internalFrame,
			final SegmentInfoPanel segmentInfoPanel,
			final WorkbenchContext context, Block enableLogic) {
		LayerViewPanel layerViewPanel = (LayerViewPanel) GUIUtil
				.getDescendantOfClass(LayerViewPanel.class, internalFrame);
		if (layerViewPanel == null) {
			return;
		}
		String key = SegmentInfoPanelFactory.class.getName() + " - INSTALLED";
		if (layerViewPanel.getBlackboard().get(key, false)) {
			return;
		}
		install(layerViewPanel, segmentInfoPanel, context, enableLogic);
		layerViewPanel.getBlackboard().put(key, true);
	}

	private void install(final LayerViewPanel layerViewPanel,
			final SegmentInfoPanel segmentInfoPanel,
			final WorkbenchContext context, final Block enableLogic) {
		layerViewPanel.addMouseMotionListener(new MouseMotionAdapter() {
			private Timer timer = GUIUtil.createRestartableSingleEventTimer(50,
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (enableLogic.yield() != Boolean.TRUE) {
								return;
							}
							ConflationSession session = ToolboxModel.instance(
									layerViewPanel.getLayerManager(), context)
									.getSession();
							if (session == null) {
								segmentInfoPanel.indicateSegmentOutOfFocus();
								return;
							}
							SourceRoadSegment segment = segment(mouseLocation,
									layerViewPanel, context);
							if (segment == null) {
								segmentInfoPanel.indicateSegmentOutOfFocus();
								return;
							}
							segmentInfoPanel.updateText(segment, layerViewPanel
									.getLayerManager());
						}
					});

			private Point mouseLocation = new Point();

			public void mouseMoved(MouseEvent e) {
				mouseLocation = e.getPoint();
				timer.restart();
			}
		});
	}

	private SourceRoadSegment segment(Point2D mouseLocation,
			LayerViewPanel panel, WorkbenchContext context) {
		try {
			int PIXEL_BUFFER = 2;
			SourceFeature closestFeature = (SourceFeature) FeatureAtClickFinder
					.closestFeature(
							new GeometryFactory().createPoint(panel
									.getViewport().toModelCoordinate(
											mouseLocation)),
							CollectionUtil
									.concatenate(SpecifyFeaturesTool
											.layerToSpecifiedFeaturesMap(
													sourceLayers(
															panel
																	.getLayerManager(),
															context).iterator(),
													EnvelopeUtil
															.expand(
																	new Envelope(
																			panel
																					.getViewport()
																					.toModelCoordinate(
																							mouseLocation)),
																	PIXEL_BUFFER
																			/ panel
																					.getViewport()
																					.getScale()))
											.values()));
			return closestFeature != null ? closestFeature.getRoadSegment()
					: null;
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}

	private Collection sourceLayers(LayerManager layerManager,
			WorkbenchContext context) {
		return Arrays
				.asList(new Layer[] {
						ToolboxModel.instance(layerManager, context)
								.getSourceLayer(0),
						ToolboxModel.instance(layerManager, context)
								.getSourceLayer(1) });
	}

}