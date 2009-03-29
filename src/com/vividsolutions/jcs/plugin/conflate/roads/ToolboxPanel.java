package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Iterator;
import java.util.Map;
import javax.swing.*;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ReferenceDatasetPrecedenceRuleEngine;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.jump.FUTURE_ColormapRotationFilter;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

public class ToolboxPanel extends JPanel {
	public ToolboxPanel(WorkbenchContext context) {
		this.context = context;
		this.viewPanel = new ViewPanel(context);
		//StatisticsPanel calls ToolboxPanel#getContext
		//[Jon Aquino 2004-02-19]
		statisticsPanel = new StatisticsPanel(this);
		QueryToolboxPlugIn.instance(context).getToolbox(context)
				.addComponentListener(new ComponentAdapter() {
					public void componentHidden(ComponentEvent e) {
						queryButton.setSelected(false);
					}

					public void componentShown(ComponentEvent e) {
						queryButton.setSelected(true);
					}
				});
		adjustPanel = new AdjustPanel(context);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assignPlugInsToButtons(context);
		findClosestRoadSegmentEnableCheck = FindClosestRoadSegmentPlugIn
				.createEnableCheck(context);
	}

	private void assignPlugInsToButtons(WorkbenchContext context) {
		assignPlugInToButton(new FindClosestRoadSegmentPlugIn(
				FindClosestRoadSegmentPlugIn.unknownCriterion,
				FindClosestRoadSegmentPlugIn.Mode.ZOOM),
				zoomToUnknownRoadSegmentButton, context);
		assignPlugInToButton(new FindClosestRoadSegmentPlugIn(
				FindClosestRoadSegmentPlugIn.unknownCriterion,
				FindClosestRoadSegmentPlugIn.Mode.PAN),
				panToUnknownRoadSegmentButton, context);
		assignPlugInToButton(new FindClosestRoadSegmentPlugIn(
				FindClosestRoadSegmentPlugIn.inconsistentCriterion,
				FindClosestRoadSegmentPlugIn.Mode.ZOOM),
				zoomToInconsistentRoadSegmentButton, context);
		assignPlugInToButton(new FindClosestRoadSegmentPlugIn(
				FindClosestRoadSegmentPlugIn.inconsistentCriterion,
				FindClosestRoadSegmentPlugIn.Mode.PAN),
				panToInconsistentRoadSegmentButton, context);
	}

	private void assignPlugInToButton(FindClosestRoadSegmentPlugIn plugIn,
			JButton button, WorkbenchContext context) {
		button.addActionListener(AbstractPlugIn.toActionListener(plugIn,
				context, null));
		button.setName(plugIn.getName());
	}

	public WorkbenchContext getContext() {
		return context;
	}

	public ToolboxModel getModelForCurrentTask() {
		return toolboxModel();
	}

	public JPanel getNorthPanel() {
		return northPanel;
	}

	public StatisticsPanel getStatisticsPanel() {
		return statisticsPanel;
	}

	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		statsPanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		statsPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		statsPanel.setBorder(null);
		matchPrecedencePanel.setLayout(gridBagLayout4);
		leftReferenceLabel.setText("Match Precedence:");
		leftReferenceLabel
				.setFont(leftReferenceLabel.getFont().deriveFont(10f));
		queryButton.setFont(new JButton().getFont().deriveFont(10f));
		matchPrecedenceLabel.setFont(matchPrecedenceLabel.getFont().deriveFont(
				10f));
		matchPrecedenceLabel.setText(" ");
		queryButton.setMargin(new Insets(2, 2, 2, 2));
		queryButton.setText("Query...");
		queryButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tablesButton_actionPerformed(e);
			}
		});
		findPanel2.setLayout(gridBagLayout10);
		unknownLabel.setFont(unknownLabel.getFont().deriveFont(10f));
		unknownLabel.setText("Unknown");
		inconsistentLabel.setFont(inconsistentLabel.getFont().deriveFont(10f));
		inconsistentLabel.setText("Inconsistent");
		inconsistentFindPanel.setLayout(gridBagLayout12);
		unknownFindPanel.setLayout(gridBagLayout13);
		panToUnknownRoadSegmentButton.setMargin(new Insets(0, 0, 0, 0));
		panToUnknownRoadSegmentButton.setIcon(GUIUtil.toSmallIcon(IconLoader
				.icon("BigHand.gif")));
		zoomToUnknownRoadSegmentButton.setMargin(new Insets(0, 0, 0, 0));
		zoomToUnknownRoadSegmentButton.setIcon(GUIUtil.toSmallIcon(IconLoader
				.icon("Magnify.gif")));
		zoomToInconsistentRoadSegmentButton.setMargin(new Insets(0, 0, 0, 0));
		zoomToInconsistentRoadSegmentButton.setIcon(GUIUtil
				.toSmallIcon(IconLoader.icon("Magnify.gif")));
		panToInconsistentRoadSegmentButton.setMargin(new Insets(0, 0, 0, 0));
		panToInconsistentRoadSegmentButton.setIcon(GUIUtil
				.toSmallIcon(IconLoader.icon("BigHand.gif")));
		inconsistentColourPanel.setBackground(HighlightManager
				.instance(context).getColourScheme().getInconsistentColour());
		inconsistentColourPanel.setPreferredSize(new Dimension(2, 2));
		inconsistentColourPanel.setLayout(gridBagLayout6);
		unknownColourPanel.setBackground(HighlightManager.instance(context)
				.getColourScheme().getUnknownColour1());
		unknownColourPanel.setPreferredSize(new Dimension(2, 2));
		unknownColourPanel.setLayout(gridBagLayout5);
		northPanel.setLayout(gridBagLayout8);
		this.add(tabbedPane, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		tabbedPane.add(statsPanel, "Stats");
		tabbedPane.add(viewPanel, "View");
		tabbedPane.add(adjustPanel, "Adjust");
		this.add(findPanel2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(matchPrecedencePanel, new GridBagConstraints(1, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		statsPanel.getViewport().add(statisticsPanel);
		matchPrecedencePanel.add(leftReferenceLabel, new GridBagConstraints(1,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
		matchPrecedencePanel.add(matchPrecedenceColorPanel,
				new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 4), 0, 0));
		matchPrecedencePanel.add(matchPrecedenceLabel, new GridBagConstraints(
				3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(northPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		findPanel2.add(inconsistentLabel, new GridBagConstraints(1, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 4, 0, 4), 0, 0));
		findPanel2.add(unknownLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		findPanel2.add(unknownFindPanel, new GridBagConstraints(0, 1, 1, 2,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		findPanel2.add(inconsistentFindPanel, new GridBagConstraints(1, 2, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		inconsistentFindPanel.add(zoomToInconsistentRoadSegmentButton,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		inconsistentFindPanel.add(panToInconsistentRoadSegmentButton,
				new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		inconsistentFindPanel.add(inconsistentColourPanel,
				new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
						0, 0));
		unknownFindPanel.add(zoomToUnknownRoadSegmentButton,
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		unknownFindPanel.add(panToUnknownRoadSegmentButton,
				new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		findPanel2.add(queryButton, new GridBagConstraints(3, 0, 1, 4, 0.0,
				0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(0, 4, 0, 0), 0, 0));
		unknownFindPanel.add(unknownColourPanel, new GridBagConstraints(0, 1,
				2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

	private ConflationSession session() {
		return toolboxModel().getSession();
	}

	public void setButtonToIconMaps(Map[] buttonToIconMaps) {
		this.buttonToIconMaps = buttonToIconMaps;
	}

	void tablesButton_actionPerformed(ActionEvent e) {
		AbstractPlugIn.toActionListener(QueryToolboxPlugIn.instance(context),
				context, null).actionPerformed(null);
	}

	private ToolboxModel toolboxModel() {
		return ToolboxModel.instance(context.getLayerManager(), context);
	}

	private void updateButtons() {
		updateButtons(309 / 360f, 387 / 360f, HighlightManager
				.instance(context).getColourScheme().getDefaultColour0(),
				buttonToIconMaps[0]);
		updateButtons(191 / 360f, 261 / 360f, HighlightManager
				.instance(context).getColourScheme().getDefaultColour1(),
				buttonToIconMaps[1]);
	}

	private void updateButtons(float sourceStart, float sourceEnd,
			Color destination, Map buttonToIconMap) {
		float hue = Color.RGBtoHSB(destination.getRed(),
				destination.getGreen(), destination.getBlue(), null)[0];
		for (Iterator i = buttonToIconMap.keySet().iterator(); i.hasNext();) {
			AbstractButton button = (AbstractButton) i.next();
			button.setIcon(new ImageIcon(FUTURE_ColormapRotationFilter.filter(
					((ImageIcon) buttonToIconMap.get(button)).getImage(),
					sourceStart, sourceEnd, hue - 10 / 360f, hue + 10 / 360f)));
		}
	}

	public void updateComponents() {
		updateButtons();
		updateEnabledStates();
		updateZoomPanToolTips();
		inconsistentColourPanel.setBackground(HighlightManager
				.instance(context).getColourScheme().getInconsistentColour());
		unknownColourPanel.setBackground(HighlightManager.instance(context)
				.getColourScheme().getUnknownColour1());
		if (context.getLayerManager() != null && toolboxModel().isInitialized()) {
			leftReferenceLabel.setEnabled(true);
			if (getReferenceDatasetNetwork() != null) {
				matchPrecedenceLabel.setText(getReferenceDatasetNetwork()
						.getName());
				matchPrecedenceColorPanel.setFillColor(toolboxModel()
						.getSourceLayer(getReferenceDatasetNetwork().getID())
						.getBasicStyle().getFillColor());
				matchPrecedenceColorPanel.setLineColor(toolboxModel()
						.getSourceLayer(getReferenceDatasetNetwork().getID())
						.getBasicStyle().getFillColor());
				matchPrecedenceColorPanel.setVisible(true);
			} else {
				matchPrecedenceLabel.setText("Scripted");
				matchPrecedenceColorPanel.setVisible(false);
			}
			statisticsPanel.setEnabled(true);
			statisticsPanel.update(session().getStatistics());
		} else {
			leftReferenceLabel.setEnabled(false);
			matchPrecedenceLabel.setText(" ");
			matchPrecedenceColorPanel.setFillColor(getBackground());
			matchPrecedenceColorPanel.setLineColor(getBackground());
			statisticsPanel.setEnabled(false);
		}
	}

	private RoadNetwork getReferenceDatasetNetwork() {
		return session().getPrecedenceRuleEngine() instanceof ReferenceDatasetPrecedenceRuleEngine ? session()
				.getSourceNetwork(
						((ReferenceDatasetPrecedenceRuleEngine) session()
								.getPrecedenceRuleEngine())
								.getReferenceDatasetName()
								.equals(session().getSourceNetwork(0).getName()) ? 0
								: 1)
				: null;
	}

	private void updateEnabledStates() {
		inconsistentLabel.setEnabled(findClosestRoadSegmentEnableCheck
				.check(null) == null);
		unknownLabel
				.setEnabled(findClosestRoadSegmentEnableCheck.check(null) == null);
		zoomToUnknownRoadSegmentButton
				.setEnabled(findClosestRoadSegmentEnableCheck.check(null) == null);
		panToUnknownRoadSegmentButton
				.setEnabled(findClosestRoadSegmentEnableCheck.check(null) == null);
		zoomToInconsistentRoadSegmentButton
				.setEnabled(findClosestRoadSegmentEnableCheck.check(null) == null);
		panToInconsistentRoadSegmentButton
				.setEnabled(findClosestRoadSegmentEnableCheck.check(null) == null);
	}

	private void updateZoomPanToolTips() {
		zoomToUnknownRoadSegmentButton
				.setToolTipText(zoomToUnknownRoadSegmentButton.isEnabled() ? zoomToUnknownRoadSegmentButton
						.getName()
						: findClosestRoadSegmentEnableCheck.check(null));
		panToUnknownRoadSegmentButton
				.setToolTipText(panToUnknownRoadSegmentButton.isEnabled() ? panToUnknownRoadSegmentButton
						.getName()
						: findClosestRoadSegmentEnableCheck.check(null));
		zoomToInconsistentRoadSegmentButton
				.setToolTipText(zoomToInconsistentRoadSegmentButton.isEnabled() ? zoomToInconsistentRoadSegmentButton
						.getName()
						: findClosestRoadSegmentEnableCheck.check(null));
		panToInconsistentRoadSegmentButton
				.setToolTipText(panToInconsistentRoadSegmentButton.isEnabled() ? panToInconsistentRoadSegmentButton
						.getName()
						: findClosestRoadSegmentEnableCheck.check(null));
	}

	private AdjustPanel adjustPanel;

	private Map[] buttonToIconMaps;

	private WorkbenchContext context;

	private EnableCheck findClosestRoadSegmentEnableCheck;

	private JPanel findPanel2 = new JPanel();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private GridBagLayout gridBagLayout10 = new GridBagLayout();

	private GridBagLayout gridBagLayout11 = new GridBagLayout();

	private GridBagLayout gridBagLayout12 = new GridBagLayout();

	private GridBagLayout gridBagLayout13 = new GridBagLayout();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	private GridBagLayout gridBagLayout5 = new GridBagLayout();

	private GridBagLayout gridBagLayout6 = new GridBagLayout();

	private GridBagLayout gridBagLayout7 = new GridBagLayout();

	private GridBagLayout gridBagLayout8 = new GridBagLayout();

	private JPanel inconsistentColourPanel = new JPanel();

	private JPanel inconsistentFindPanel = new JPanel();

	private JLabel inconsistentLabel = new JLabel();

	private JLabel leftReferenceLabel = new JLabel();

	private JPanel northPanel = new JPanel();

	private JButton panToInconsistentRoadSegmentButton = new JButton();

	private JButton panToUnknownRoadSegmentButton = new JButton();

	private JToggleButton queryButton = new JToggleButton();

	private ColorPanel matchPrecedenceColorPanel = new ColorPanel();

	private JLabel matchPrecedenceLabel = new JLabel();

	private JPanel matchPrecedencePanel = new JPanel();

	private StatisticsPanel statisticsPanel;

	private JScrollPane statsPanel = new JScrollPane();

	private JTabbedPane tabbedPane = new JTabbedPane();

	private JPanel unknownColourPanel = new JPanel();

	private JPanel unknownFindPanel = new JPanel();

	private JLabel unknownLabel = new JLabel();

	private ViewPanel viewPanel;

	private JButton zoomToInconsistentRoadSegmentButton = new JButton();

	private JButton zoomToUnknownRoadSegmentButton = new JButton();

	public AdjustPanel getAdjustPanel() {
		return adjustPanel;
	}
}