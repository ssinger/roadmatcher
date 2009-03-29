package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadsListener;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.plugin.issuelog.IssueLogPanel;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.util.Fmt;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;

public class ConflationSessionInitializer {
	public void initialize(final ConflationSession session, Task task,
			final WorkbenchContext context) {
		showScaleBar(context);
		showMapToolTips(context);
		configureSourceDatasetsInvalidLabel(session, context
				.getLayerViewPanel());
		configureScaleLabel(context.getLayerViewPanel());
		task.setName(session.getName());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RoadMatcherToolboxPlugIn.instance(context).getToolbox(context)
						.setVisible(true);
				// Moved the listeners after #getToolbox because they
				// assume that RoadMatcherToolboxPlugIn#getToolboxPanel is not
				// null, but now that we've pushed the Toolbox menu into a
				// sub-menu, that is no longer true (the EnableCheck isn't fired
				// right away).
				session.getRoadsEventFirer().addListener(
						RoadMatcherToolboxPlugIn.instance(context)
								.getToolboxPanel().getStatisticsPanel()
								.getListener());
				session.getRoadsEventFirer().addListener(
						createGeometryModifiedExternallyListener(context));
				((ToolboxPanel) GUIUtil.getDescendantOfClass(
						ToolboxPanel.class, RoadMatcherToolboxPlugIn.instance(
								context).getToolbox(context)))
						.updateComponents();
				((QueryToolboxPanel) GUIUtil.getDescendantOfClass(
						QueryToolboxPanel.class, QueryToolboxPlugIn.instance(
								context).getToolbox(context)))
						.updateComponents();
				((LegendToolboxPanel) GUIUtil.getDescendantOfClass(
						LegendToolboxPanel.class, LegendToolboxPlugIn.instance(
								context).getToolbox(context)))
						.updateComponents();
			}
		});
	}

	private String format(double x) {
		if (1 <= x && x <= 1000) {
			return new DecimalFormat("#").format(x);
		}
		if (.0001 <= x && x <= 1) {
			return new DecimalFormat("0.0000").format(x);
		}
		return new DecimalFormat("0.#E0").format(x);
	}

	private void configureScaleLabel(final LayerViewPanel panel) {
		String key = getClass().getName() + " - SCALE LABEL PANEL";
		if (panel.getBlackboard().get(key) == null) {
			JPanel labelPanel = new JPanel(new GridBagLayout());
			labelPanel.add(new JLabel("", SwingConstants.RIGHT) {
				{
					panel.getViewport().addListener(new ViewportListener() {
						public void zoomChanged(Envelope modelEnvelope) {
							setText(format(1d / panel.getViewport().getScale()));
						}
					});
					setToolTipText("Scale (ground units per pixel)");
				}
			}, new GridBagConstraints(0, 0, 1, 1, 1, 0,
					GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(4, 4, 4, 4), 0, 0));
			labelPanel.setOpaque(false);
			if (!(panel.getLayout() instanceof BorderLayout)) {
				panel.setLayout(new BorderLayout());
			}
			panel.add(labelPanel, BorderLayout.SOUTH);
			panel.getBlackboard().put(key, labelPanel);
		}
	}

	private void configureSourceDatasetsInvalidLabel(
			final ConflationSession session, final LayerViewPanel panel) {
		String key = getClass().getName()
				+ " - SOURCE DATASETS INVALID LABEL PANEL";
		if (panel.getBlackboard().get(key) == null) {
			JPanel labelPanel = new JPanel(new GridBagLayout());
			labelPanel.add(new JLabel("Warning: Source datasets are invalid",
					SwingConstants.RIGHT) {
				{
					setBackground(Color.yellow);
					setOpaque(true);
					setToolTipText("<html>"
							+ StringUtil
									.replaceAll(
											StringUtil
													.split(
															"The original datasets have coincident segments or illegal geometries. Please correct the errors in the original datasets then create a new conflation session.",
															80), "\n", "<br>")
							+ "</html>");
				}
			}, new GridBagConstraints(0, 0, 1, 1, 1, 0,
					GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(4, 4, 4, 4), 0, 0));
			labelPanel.setOpaque(false);
			if (!(panel.getLayout() instanceof BorderLayout)) {
				panel.setLayout(new BorderLayout());
			}
			panel.add(labelPanel, BorderLayout.NORTH);
			panel.getBlackboard().put(key, labelPanel);
		}
		//Don't show the warning for mere node-constraint mismatches
		//[Jon Aquino 2004-06-17]
		((JPanel) panel.getBlackboard().get(key)).setVisible(session
				.getCoincidentSegments()[0].size() > 0
				|| session.getCoincidentSegments()[1].size() > 0
				|| session.getIllegalGeometries()[0].size() > 0
				|| session.getIllegalGeometries()[1].size() > 0);
	}

	private RoadsListener createGeometryModifiedExternallyListener(
			final WorkbenchContext context) {
		return new RoadsListener() {
			public void roadSegmentAdded(SourceRoadSegment roadSegment) {
			}

			public void roadSegmentRemoved(SourceRoadSegment roadSegment) {
			}

			public void resultStateChanged(ResultState oldResultState,
					SourceRoadSegment roadSegment) {
			}

			public void stateChanged(SourceState oldState,
					SourceRoadSegment roadSegment) {
			}

			public void geometryModifiedExternally(SourceRoadSegment roadSegment) {
				new Transaction(ToolboxModel.instance(
						context.getLayerManager(), context), context
						.getErrorHandler()).markAsModified(roadSegment)
						.execute();
			}

			public void roadSegmentsChanged() {
			}
		};
	}

	private void showMapToolTips(WorkbenchContext context) {
		context.getLayerViewPanel().getToolTipWriter().setEnabled(true);
	}

	private void showScaleBar(WorkbenchContext context) {
		ScaleBarRenderer.setEnabled(true, context.getLayerViewPanel());
		context.getLayerViewPanel().getRenderingManager().render(
				ScaleBarRenderer.CONTENT_ID);
	}
}