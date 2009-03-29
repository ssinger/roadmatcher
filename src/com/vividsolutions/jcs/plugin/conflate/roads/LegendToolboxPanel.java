package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadSegmentMatch;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import javax.swing.*;

public class LegendToolboxPanel extends JPanel {
	private static final int PANEL_HEIGHT = 50;

	private static final int PANEL_WIDTH = 87;

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JLabel unknownLabel0 = new JLabel();

	private JLabel unknownLabel1 = new JLabel();

	private JLabel pendingLabel0 = new JLabel();

	private JLabel pendingLabel1 = new JLabel();

	private JLabel standaloneLabel0 = new JLabel();

	private JLabel standaloneLabel1 = new JLabel();

	private JLabel integratedLabel0 = new JLabel();

	private JLabel integratedLabel1 = new JLabel();

	private JLabel retiredLabel0 = new JLabel();

	private JLabel splitNodesLabel0 = new JLabel();

	private JLabel splitNodesLabel1 = new JLabel();

	private JLabel matchedLabelA0 = new JLabel();

	private JLabel matchedLabelA1 = new JLabel();

	private JLabel matchedLabelB0 = new JLabel();

	private JLabel matchedLabelB1 = new JLabel();

	private JLabel integratedAdjustedLabel0 = new JLabel();

	private JLabel integratedAdjustedLabel1 = new JLabel();

	private JLabel unknownLabel = new JLabel();

	private JLabel pendingLabel = new JLabel();

	private JLabel standaloneLabel = new JLabel();

	private JLabel integratedLabel = new JLabel();

	private JLabel retiredLabel = new JLabel();

	private JLabel splitNodesLabel = new JLabel();

	private JLabel matchedLabel = new JLabel();

	private JLabel jLabel8 = new JLabel();

	private JLabel integratedAdjustedLabelA = new JLabel();

	private JLabel inconsistentLabel = new JLabel();

	private JLabel intersectingLabel = new JLabel();

	private JLabel retiredLabel1 = new JLabel();

	private JLabel integratedAdjustedLabelB = new JLabel();

	private JPanel pendingPanel = new JPanel();

	private JPanel standalonePanel = new JPanel();

	private JPanel integratedPanel = new JPanel();

	private JPanel retiredPanel = new JPanel();

	private JPanel splitNodesPanel = new JPanel();

	private JPanel matchedPanelA = new JPanel();

	private JPanel matchedPanelB = new JPanel();

	private JPanel integratedAdjustedPanel = new JPanel();

	private JPanel unknownPanel = new JPanel();

	private JPanel inconsistentPanel = new JPanel();

	private JPanel intersectingPanel = new JPanel();

	private WorkbenchContext context;

	private GeometryFactory factory = new GeometryFactory();

	private Map labelToOriginalTextMap = new HashMap();

	public void updateComponents() {
		for (int i = 0; i < getComponentCount(); i++) {
			if (!(getComponent(i) instanceof LayerViewPanel)) {
				continue;
			}
			ToolboxModel.instance(
					((LayerViewPanel) getComponent(i)).getLayerManager(),
					context).updateStyles();
			((LayerViewPanel) getComponent(i)).repaint();
		}
		updateText();
	}

	private void updateText() {
		if (!(context.getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerManagerProxy)) {
			return;
		}
		if (!toolboxModel().isInitialized()) {
			return;
		}
		for (Iterator i = labelToOriginalTextMap.keySet().iterator(); i
				.hasNext();) {
			JLabel label = (JLabel) i.next();
			if (originalText(label).indexOf("GISI") > -1) {
				label.setText(StringUtil.replaceAll(originalText(label),
						"GISI", toolboxModel().getSession().getSourceNetwork(0)
								.getName()));
			} else {
				label.setText(StringUtil.replaceAll(originalText(label),
						"TRIM", toolboxModel().getSession().getSourceNetwork(1)
								.getName()));
			}
		}
		SwingUtilities.windowForComponent(this).pack();
	}

	private ToolboxModel toolboxModel() {
		return ToolboxModel.instance(context.getLayerManager(), context);
	}

	private String originalText(JLabel label) {
		return (String) labelToOriginalTextMap.get(label);
	}

	public LegendToolboxPanel(LayerViewPanelContext layerViewPanelContext,
			WorkbenchContext context) {
		this.context = context;
		initLayerViewPanels(layerViewPanelContext);
		disableLayerViewPanels();
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		saveOriginalLabelText();
		initFonts();
	}

	private void disableLayerViewPanels() {
		for (int i = 0; i < getComponentCount(); i++) {
			if (!(getComponent(i) instanceof LayerViewPanel)) {
				continue;
			}
			getComponent(i).setEnabled(false);
		}
	}

	private void saveOriginalLabelText() {
		for (int i = 0; i < getComponentCount(); i++) {
			if (!(getComponent(i) instanceof JLabel)) {
				continue;
			}
			labelToOriginalTextMap.put(getComponent(i),
					((JLabel) getComponent(i)).getText());
		}
	}

	private void initFonts() {
		for (Iterator i = labelToOriginalTextMap.keySet().iterator(); i
				.hasNext();) {
			JLabel label = (JLabel) i.next();
			String originalText = originalText(label);
			if (originalText.indexOf("GISI") > -1
					|| originalText.indexOf("TRIM") > -1) {
				label.setFont(label.getFont().deriveFont(10f));
			} else {
				label.setFont(label.getFont().deriveFont(Font.BOLD));
			}
		}
	}

	private void initLayerViewPanels(LayerViewPanelContext layerViewPanelContext) {
		unknownPanel = new LayerViewPanel(createLayerManager(new Updater(),
				false), layerViewPanelContext);
		pendingPanel = new LayerViewPanel(createLayerManager(pendingUpdater(),
				false), layerViewPanelContext);
		standalonePanel = new LayerViewPanel(createLayerManager(
				pendingUpdater(), true), layerViewPanelContext);
		integratedPanel = new LayerViewPanel(createLayerManager(new Updater() {
			protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
				s0.setState(SourceState.STANDALONE, null);
				s0.setResultState(new ResultState.Description(
						ResultState.INTEGRATED));
				s1.setState(SourceState.STANDALONE, null);
				s1.setResultState(new ResultState.Description(
						ResultState.INTEGRATED));
			}
		}, false), layerViewPanelContext);
		retiredPanel = new LayerViewPanel(createLayerManager(new Updater() {
			protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
				s0.setState(SourceState.RETIRED, null);
				s1.setState(SourceState.RETIRED, null);
			}
		}, false), layerViewPanelContext);
		splitNodesPanel = new LayerViewPanel(createLayerManager(new Updater() {
			protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
				split(s0);
				split(s1);
			}

			private void split(SourceRoadSegment s) {
				s.split(new LineString[] {
						factory
								.createLineString(new Coordinate[] {
										s.getLine().getCoordinateN(0),
										CoordUtil.average(s.getLine()
												.getCoordinateN(0), s.getLine()
												.getCoordinateN(1)) }),
						factory.createLineString(new Coordinate[] {
								CoordUtil.average(
										s.getLine().getCoordinateN(0), s
												.getLine().getCoordinateN(1)),
								s.getLine().getCoordinateN(1) }) });
			}
		}, false), layerViewPanelContext);
		matchedPanelA = new LayerViewPanel(createLayerManager(new Updater() {
			protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
				RoadSegmentMatch match = new RoadSegmentMatch(s0, s1);
				s0.setState(SourceState.MATCHED_REFERENCE, match);
				s1.setState(SourceState.MATCHED_NON_REFERENCE, match);
				s0.setResultState(new ResultState.Description(
						ResultState.PENDING));
			}
		}, false), layerViewPanelContext);
		matchedPanelB = new LayerViewPanel(createLayerManager(new Updater() {
			protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
				RoadSegmentMatch match = new RoadSegmentMatch(s0, s1);
				s0.setState(SourceState.MATCHED_NON_REFERENCE, match);
				s1.setState(SourceState.MATCHED_REFERENCE, match);
				s1.setResultState(new ResultState.Description(
						ResultState.PENDING));
			}
		}, false), layerViewPanelContext);
		integratedAdjustedPanel = new LayerViewPanel(createLayerManager(
				new Updater() {
					protected void update(SourceRoadSegment s0,
							SourceRoadSegment s1) {
						s0.setState(SourceState.STANDALONE, null);
						s0.setResultState(new ResultState.Description(
								ResultState.INTEGRATED));
						s0.setApparentLine(modifySlightly(s0.getLine()));
						s1.setState(SourceState.STANDALONE, null);
						s1.setResultState(new ResultState.Description(
								ResultState.INTEGRATED));
						s1.setApparentLine(modifySlightly(s1.getLine()));
					}

					private LineString modifySlightly(LineString line) {
						return factory.createLineString(new Coordinate[] {
								line.getCoordinateN(0),
								modifySlightly(line.getCoordinateN(1)) });
					}

					private Coordinate modifySlightly(Coordinate coordinate) {
						return new Coordinate(coordinate.x - 1, coordinate.y);
					}
				}, false), layerViewPanelContext);
		inconsistentPanel = new LayerViewPanel(
				createInconsistentLayerManager(new Updater() {
					protected void update(SourceRoadSegment s0,
							SourceRoadSegment s1) {
						RoadSegmentMatch leftMatch = new RoadSegmentMatch(
								left(s0), left(s1));
						RoadSegmentMatch rightMatch = new RoadSegmentMatch(
								right(s0), right(s1));
						left(s0).setState(SourceState.MATCHED_NON_REFERENCE,
								leftMatch);
						left(s1).setState(SourceState.MATCHED_REFERENCE,
								leftMatch);
						right(s0).setState(SourceState.MATCHED_NON_REFERENCE,
								rightMatch);
						right(s1).setState(SourceState.MATCHED_REFERENCE,
								rightMatch);
					}

					private SourceRoadSegment left(SourceRoadSegment s) {
						return centre(roadSegments(s)[0]).x < centre(roadSegments(s)[1]).x ? roadSegments(s)[0]
								: roadSegments(s)[1];
					}

					private Coordinate centre(SourceRoadSegment segment) {
						return CoordUtil.average(segment.getLine()
								.getCoordinateN(0), segment.getLine()
								.getCoordinateN(1));
					}

					private SourceRoadSegment[] roadSegments(SourceRoadSegment s) {
						return (SourceRoadSegment[]) s.getNetwork().getGraph()
								.getEdges().toArray(new SourceRoadSegment[2]);
					}

					private SourceRoadSegment right(SourceRoadSegment s) {
						return left(s) == roadSegments(s)[0] ? roadSegments(s)[1]
								: roadSegments(s)[0];
					}
				}), layerViewPanelContext);
		intersectingPanel = new LayerViewPanel(
				updateResultStates(createLayerManager("LINESTRING("
						+ LEFT_MARGIN + " "
						+ (verticalCentre() + verticalDisplacement()) + ", "
						+ (PANEL_WIDTH - LEFT_MARGIN) + " "
						+ (verticalCentre() - verticalDisplacement()) + ")",
						"LINESTRING(" + LEFT_MARGIN + " "
								+ (verticalCentre() - verticalDisplacement())
								+ ", " + (PANEL_WIDTH - LEFT_MARGIN) + " "
								+ (verticalCentre() + verticalDisplacement())
								+ ")", pendingUpdater(), false)),
				layerViewPanelContext);
	}

	private Updater pendingUpdater() {
		return new Updater() {
			protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
				s0.setState(SourceState.STANDALONE, null);
				s0.setResultState(new ResultState.Description(
						ResultState.PENDING));
				s1.setState(SourceState.STANDALONE, null);
				s1.setResultState(new ResultState.Description(
						ResultState.PENDING));
			}
		};
	}

	private WKTReader wktReader = new WKTReader();

	private static class Updater {
		public void update(ToolboxModel model) {
			update(roadSegment(0, model.getSession()), roadSegment(1, model
					.getSession()));
		}

		protected void update(SourceRoadSegment s0, SourceRoadSegment s1) {
		}

		private SourceRoadSegment roadSegment(int i, ConflationSession session) {
			return (SourceRoadSegment) session.getSourceNetwork(i).getGraph()
					.getEdges().iterator().next();
		}
	}

	private LayerManager createLayerManager(Updater updater,
			boolean standaloneLinePatternEnabled) {
		return createLayerManager("LINESTRING(" + LEFT_MARGIN + " "
				+ (verticalCentre() + verticalDisplacement()) + ", "
				+ (PANEL_WIDTH - LEFT_MARGIN) + " "
				+ (verticalCentre() + verticalDisplacement()) + ")",
				"LINESTRING(" + LEFT_MARGIN + " "
						+ (verticalCentre() - verticalDisplacement()) + ", "
						+ (PANEL_WIDTH - LEFT_MARGIN) + " "
						+ (verticalCentre() - verticalDisplacement()) + ")",
				updater, standaloneLinePatternEnabled);
	}

	private LayerManager createInconsistentLayerManager(Updater updater) {
		int shortSegmentLength = 20;
		String features0 = "LINESTRING(" + LEFT_MARGIN + " "
				+ (verticalCentre() + verticalDisplacement()) + ", "
				+ (PANEL_WIDTH / 2) + " "
				+ (verticalCentre() + verticalDisplacement()) + ")";
		features0 += "LINESTRING(" + (PANEL_WIDTH / 2) + " "
				+ (verticalCentre() + verticalDisplacement()) + ", "
				+ (PANEL_WIDTH - LEFT_MARGIN) + " "
				+ (verticalCentre() + verticalDisplacement()) + ")";
		String features1 = "LINESTRING(" + LEFT_MARGIN + " "
				+ (verticalCentre() - verticalDisplacement()) + ", "
				+ (LEFT_MARGIN + shortSegmentLength) + " "
				+ (verticalCentre() - verticalDisplacement()) + ")";
		features1 += "LINESTRING("
				+ (PANEL_WIDTH - LEFT_MARGIN - shortSegmentLength) + " "
				+ (verticalCentre() - verticalDisplacement()) + ", "
				+ (PANEL_WIDTH - LEFT_MARGIN) + " "
				+ (verticalCentre() - verticalDisplacement()) + ")";
		return updateResultStates(createLayerManager(features0, features1,
				updater, false));
	}

	private LayerManager updateResultStates(LayerManager layerManager) {
		ToolboxModel.instance(layerManager, context).getSession()
				.setConsistencyRule(new AdjustedMatchConsistencyRule())
				.updateResultStates(new DummyTaskMonitor());
		return layerManager;
	}

	private int verticalDisplacement() {
		return SEPARATION / 2;
	}

	private int verticalCentre() {
		return PANEL_HEIGHT / 2;
	}

	private static final int LEFT_MARGIN = 5;

	private static final int SEPARATION = 14;

	private LayerManager createLayerManager(String features0, String features1,
			Updater updater, boolean standaloneLinePatternEnabled) {
		final int SMALL_DASH = 1;
		int bigDash = (int) (((PANEL_WIDTH - LEFT_MARGIN - LEFT_MARGIN - SMALL_DASH*3)/2)/ToolboxModel.INCLUDED_INNER_LINE_WIDTH);
		try {
			LayerManager layerManager = new LayerManager();
			updater
					.update(ToolboxModel.instance(layerManager, context)
							.setStandaloneLinePatternEnabled(
									standaloneLinePatternEnabled)
							.setStandaloneLinePattern(bigDash + ",1,1,1")
							.initialize(
									new ConflationSession(wktReader
											.read(new StringReader(features0)),
											wktReader.read(new StringReader(
													features1)))));
			return layerManager;
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Assert.shouldNeverReachHere();
			return null;
		}
	}

	void jbInit() throws Exception {
		unknownLabel0.setText("GISI");
		this.setLayout(gridBagLayout1);
		unknownLabel1.setText("TRIM");
		pendingLabel0.setText("GISI");
		pendingLabel1.setText("TRIM");
		standaloneLabel0.setText("GISI");
		standaloneLabel1.setText("TRIM");
		integratedLabel0.setText("GISI");
		integratedLabel1.setText("TRIM");
		retiredLabel0.setText("GISI");
		splitNodesLabel0.setText("GISI");
		splitNodesLabel1.setText("TRIM");
		matchedLabelA0.setText("GISI Ref");
		matchedLabelA1.setText("TRIM Non-Ref");
		matchedLabelB0.setText("GISI Non-Ref");
		matchedLabelB1.setText("TRIM Ref");
		integratedAdjustedLabel0.setText("GISI");
		integratedAdjustedLabel1.setText("TRIM");
		unknownLabel.setText("Unknown");
		pendingLabel.setText("Pending");
		standaloneLabel.setText("Standalone");
		integratedLabel.setText("Integrated");
		retiredLabel.setText("Retired");
		splitNodesLabel.setText("Split Nodes");
		matchedLabel.setText("Matched");
		integratedAdjustedLabelA.setToolTipText("");
		integratedAdjustedLabelA.setText("Integrated /");
		inconsistentLabel.setText("Inconsistent");
		intersectingLabel.setText("Intersecting");
		retiredLabel1.setText("TRIM");
		integratedAdjustedLabelB.setText("Adjusted");
		this.setBackground(Color.white);
		pendingPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		standalonePanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				PANEL_HEIGHT));
		integratedPanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				PANEL_HEIGHT));
		retiredPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		splitNodesPanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				PANEL_HEIGHT));
		matchedPanelA
				.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		matchedPanelB
				.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		integratedAdjustedPanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				PANEL_HEIGHT));
		unknownPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		inconsistentPanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				PANEL_HEIGHT));
		intersectingPanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				PANEL_HEIGHT));
		this.add(unknownLabel0, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(unknownLabel1, new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(pendingLabel0, new GridBagConstraints(2, 2, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(pendingLabel1, new GridBagConstraints(2, 3, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(standaloneLabel0, new GridBagConstraints(2, 4, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(standaloneLabel1, new GridBagConstraints(2, 5, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(integratedLabel0, new GridBagConstraints(2, 6, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(integratedLabel1, new GridBagConstraints(2, 7, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(retiredLabel0, new GridBagConstraints(2, 8, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(splitNodesLabel0, new GridBagConstraints(2, 10, 1, 1, 0.0,
				1.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(splitNodesLabel1, new GridBagConstraints(2, 11, 1, 1, 0.0,
				1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(matchedLabelA0, new GridBagConstraints(2, 12, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(matchedLabelA1, new GridBagConstraints(2, 13, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(matchedLabelB0, new GridBagConstraints(2, 14, 1, 1, 0.0, 1.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(matchedLabelB1, new GridBagConstraints(2, 15, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(integratedAdjustedLabel0, new GridBagConstraints(2, 16, 1, 1,
				0.0, 1.0, GridBagConstraints.SOUTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(integratedAdjustedLabel1, new GridBagConstraints(2, 17, 1, 1,
				0.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(unknownLabel, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(pendingLabel, new GridBagConstraints(0, 2, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(standaloneLabel, new GridBagConstraints(0, 4, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(integratedLabel, new GridBagConstraints(0, 6, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(retiredLabel, new GridBagConstraints(0, 8, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(inconsistentLabel, new GridBagConstraints(0, 18, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(intersectingLabel, new GridBagConstraints(0, 19, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(retiredLabel1, new GridBagConstraints(2, 9, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(splitNodesLabel, new GridBagConstraints(0, 10, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(matchedLabel, new GridBagConstraints(0, 12, 1, 4, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(jLabel8, new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(integratedAdjustedLabelA, new GridBagConstraints(0, 16, 1, 1,
				0.0, 0.0, GridBagConstraints.SOUTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(integratedAdjustedLabelB, new GridBagConstraints(0, 17, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(pendingPanel, new GridBagConstraints(1, 2, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(standalonePanel, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(integratedPanel, new GridBagConstraints(1, 6, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(retiredPanel, new GridBagConstraints(1, 8, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(splitNodesPanel, new GridBagConstraints(1, 10, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(matchedPanelA, new GridBagConstraints(1, 12, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(matchedPanelB, new GridBagConstraints(1, 14, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(integratedAdjustedPanel, new GridBagConstraints(1, 16, 1, 2,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(unknownPanel, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(inconsistentPanel, new GridBagConstraints(1, 18, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(intersectingPanel, new GridBagConstraints(1, 19, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(
				new LegendToolboxPanel(new LayerViewPanelContext() {
					public void setStatusMessage(String message) {
						System.out.println(message);
					}

					public void warnUser(String warning) {
						System.out.println(warning);
					}

					public void handleThrowable(Throwable t) {
						t.printStackTrace(System.err);
					}
				}, new WorkbenchContext() {
					private Blackboard blackboard = new Blackboard();

					public Blackboard getBlackboard() {
						return blackboard;
					}

					public ErrorHandler getErrorHandler() {
						return new ErrorHandler() {
							public void handleThrowable(Throwable t) {
								t.printStackTrace(System.err);
							}
						};
					}
				}));
		frame.pack();
		frame.setVisible(true);
	}
}