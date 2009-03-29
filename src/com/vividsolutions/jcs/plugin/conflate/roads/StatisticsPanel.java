package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.*;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.event.*;
public class StatisticsPanel extends JPanel {
	private static abstract class ValueDisplay {
		public ValueDisplay(String description) {
			this.description = description;
		}
		public abstract String displayText(String fieldName,
				NetworkStatistics statistics);
		public String toString() {
			return description;
		}
		private String description;
	}
	public StatisticsPanel(ToolboxPanel toolboxPanel) {
		this.toolboxPanel = toolboxPanel;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		totalLabel0.setToolTipText(totalLabel.getToolTipText());
		totalLabel1.setToolTipText(totalLabel.getToolTipText());
		installModel(unitsComboBox);
		unitsComboBox.setBackground(getBackground());
	}
	private String displayText(String name, int network, Statistics statistics) {
		return ((ValueDisplay) unitsComboBox.getSelectedItem()).displayText(
				Statistics.normalize(name), statistics.get(network));
	}
	/**
	 * Don't want the column to be too wide on account of the name;
	 */
	private String firstFewCharacters(String string) {
		return string.substring(0, Math.min(4, string.length()));
	}
	private double get(NetworkStatistics statistics, String fieldName) {
		String getterName = "get" + Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
		try {
			Object object;
			Method method;
			try {
				object = statistics;
				method = object.getClass().getDeclaredMethod(getterName,
						new Class[]{});
			} catch (NoSuchMethodException e1) {
				object = statistics.getResultStatistics();
				method = object.getClass().getDeclaredMethod(getterName,
						new Class[]{});
			}
			return ((Number) method.invoke(object, new Object[]{}))
					.doubleValue();
		} catch (InvocationTargetException e) {
			Assert.shouldNeverReachHere();
		} catch (SecurityException e) {
			Assert.shouldNeverReachHere();
		} catch (NoSuchMethodException e) {
			Assert.shouldNeverReachHere(e.toString());
		} catch (IllegalArgumentException e) {
			Assert.shouldNeverReachHere();
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere();
		}
		return -1;
	}
	protected RoadsListener getListener() {
		return listener;
	}
	private void installModel(JComboBox comboBox) {
		comboBox.setModel(new DefaultComboBoxModel(new Object[]{
				new ValueDisplay("Number of Road Segments") {
					public String displayText(String fieldName,
							NetworkStatistics statistics) {
						return ""
								+ (int) Math.round(get(statistics, fieldName
										+ "Count"));
					}
				}, new ValueDisplay("% of Number of Road Segments") {
					public String displayText(String fieldName,
							NetworkStatistics statistics) {
						return (int) Math.round(100
								* get(statistics, fieldName + "Count")
								/ get(statistics, "totalCount"))
								+ "%";
					}
				}, new ValueDisplay("Length of Road Segments") {
					public String displayText(String fieldName,
							NetworkStatistics statistics) {
						return ""
								+ (int) Math.round(get(statistics, fieldName
										+ "Length"));
					}
				}, new ValueDisplay("% of Length of Road Segments") {
					public String displayText(String fieldName,
							NetworkStatistics statistics) {
						return (int) Math.round(100
								* get(statistics, fieldName + "Length")
								/ get(statistics, "totalLength"))
								+ "%";
					}
				}}));
	}
	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		unknownLabel.setText("Unknown");
		matchedNonReferenceLabel.setText("Matched (Non-Ref)");
		matchedReferenceLabel.setForeground(new Color(0, 0, 128));
        matchedReferenceLabel.setText("Matched (Ref)");
		standaloneLabel.setForeground(new Color(0, 0, 128));
        standaloneLabel.setText("Standalone");
		retiredLabel.setText("Retired");
		unknownLabel0.setText("0");
		resultStatesLabel.setFont(resultStatesLabel.getFont().deriveFont(
				Font.BOLD));
        resultStatesLabel.setForeground(new Color(0, 0, 128));
		unknownLabel1.setText("0");
		matchedNonReferenceLabel0.setText("0");
		matchedNonReferenceLabel1.setText("0");
		matchedReferenceLabel0.setText("0");
		matchedReferenceLabel1.setText("0");
		standaloneLabel0.setText("0");
		standaloneLabel1.setText("0");
		retiredLabel0.setText("0");
		retiredLabel1.setText("0");
		pendingLabel.setText("Pending");
		inconsistentLabel.setText("Inconsistent");
		integratedLabel
				.setFont(integratedLabel.getFont().deriveFont(Font.BOLD));
		integratedLabel.setText("Integrated");
		pendingLabel0.setText("0");
		pendingLabel1.setText("0");
		inconsistentLabel0.setText("0");
		inconsistentLabel1.setText("0");
		integratedLabel0.setFont(integratedLabel0.getFont().deriveFont(
				Font.BOLD));
		integratedLabel0.setText("0");
		integratedLabel1.setFont(integratedLabel1.getFont().deriveFont(
				Font.BOLD));
		integratedLabel1.setText("0");
		totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
		totalLabel.setText("Total");
		totalLabel0.setFont(totalLabel0.getFont().deriveFont(Font.BOLD));
		totalLabel0.setText("0");
		totalLabel1.setFont(totalLabel1.getFont().deriveFont(Font.BOLD));
		totalLabel1.setText("0");
		fillerPanel2.setPreferredSize(new Dimension(12, 12));
		//Set text to " " rather than "" so that preferred height is typical
		// before #pack is called [Jon Aquino 12/3/2003]
		layerLabel0.setText(" ");
		layerLabel1.setText(" ");
		fillerPanel3.setPreferredSize(new Dimension(12, 12));
		refreshButton.setFont(new java.awt.Font("Dialog", 0, 10));
		refreshButton
				.setToolTipText("Recomputes the statistics. Normally not necessary.");
		refreshButton.setMargin(new Insets(2,2,2,2));
		refreshButton.setText("Refresh");
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshButton_actionPerformed(e);
			}
		});
		integratedLabel.setForeground(new ColourScheme().getIntegratedColour()
				.darker());
		inconsistentLabel.setForeground(new ColourScheme()
				.getInconsistentColour());
		fillerPanel5.setPreferredSize(new Dimension(12, 12));
		fillerPanel1.setPreferredSize(new Dimension(1, 1));
		resultStatesLabel.setToolTipText("");
		resultStatesLabel.setText("Result States:");
		unitsPanel.setLayout(gridBagLayout3);
		unitsComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unitsComboBox_actionPerformed(e);
			}
		});
		unitsComboBox.setBackground(UIManager.getColor("text"));
		unitsLabel.setText("Units: ");
		this.add(unknownLabel, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(retiredLabel, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(unknownLabel0, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(unknownLabel1, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(retiredLabel0, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(retiredLabel1, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(matchedNonReferenceLabel0, new GridBagConstraints(1, 7, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 8), 0, 0));
		this.add(matchedNonReferenceLabel1, new GridBagConstraints(2, 7, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 8), 0, 0));
		this.add(matchedReferenceLabel0, new GridBagConstraints(1, 6, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 8), 0, 0));
		this.add(matchedNonReferenceLabel, new GridBagConstraints(3, 7, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(matchedReferenceLabel, new GridBagConstraints(3, 6, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(matchedReferenceLabel1, new GridBagConstraints(2, 6, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 8), 0, 0));
		this.add(totalLabel, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(totalLabel0, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(totalLabel1, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(4, 16, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						12, 12, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(3, 14, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(refreshButton, new GridBagConstraints(1, 15, 5, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(fillerPanel5, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(layerLabel0, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(layerLabel1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(standaloneLabel0, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(standaloneLabel1, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(standaloneLabel, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(pendingLabel0, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(pendingLabel1, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(pendingLabel, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(inconsistentLabel0, new GridBagConstraints(1, 12, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(inconsistentLabel1, new GridBagConstraints(2, 12, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(inconsistentLabel, new GridBagConstraints(3, 12, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(integratedLabel0, new GridBagConstraints(1, 13, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(integratedLabel1, new GridBagConstraints(2, 13, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 8), 0, 0));
		this.add(integratedLabel, new GridBagConstraints(3, 13, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 8), 0, 0));
		this.add(resultStatesLabel, new GridBagConstraints(1, 10, 4, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(unitsPanel, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		unitsPanel.add(unitsComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		unitsPanel.add(unitsLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
	}
	void unitsComboBox_actionPerformed(ActionEvent e) {
		update(lastStatistics);
	}
	void refreshButton_actionPerformed(ActionEvent e) {
        lastStatistics.refresh();
        update(lastStatistics);
	}

    public void setEnabled(boolean enabled) {
		refreshButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	public void update(Statistics statistics) {
		if (statistics == null) {
			return;
		}
		if (toolboxPanel.getContext().getLayerManager() == null) {
			return;
		}
		if (!toolboxPanel.getModelForCurrentTask().isInitialized()) {
			return;
		}
		layerLabel0.setText(firstFewCharacters(toolboxPanel
				.getModelForCurrentTask().getSession().getSourceNetwork(0)
				.getName()));
		layerLabel1.setText(firstFewCharacters(toolboxPanel
				.getModelForCurrentTask().getSession().getSourceNetwork(1)
				.getName()));
		layerLabel0.setForeground(HighlightManager.instance(
				toolboxPanel.getContext()).getColourScheme()
				.getDefaultColour0());
		layerLabel1.setForeground(HighlightManager.instance(
				toolboxPanel.getContext()).getColourScheme()
				.getDefaultColour1());
		updateText(totalLabel0, displayText("total", 0, statistics));
		updateText(totalLabel1, displayText("total", 1, statistics));
		updateRedGreenText(unknownLabel0, displayText(SourceState.UNKNOWN
				.getName(), 0, statistics));
		updateRedGreenText(unknownLabel1, displayText(SourceState.UNKNOWN
				.getName(), 1, statistics));
		updateText(matchedNonReferenceLabel0, displayText(
				SourceState.MATCHED_NON_REFERENCE.getName(), 0, statistics));
		updateText(matchedNonReferenceLabel1, displayText(
				SourceState.MATCHED_NON_REFERENCE.getName(), 1, statistics));
		updateText(matchedReferenceLabel0, displayText(
				SourceState.MATCHED_REFERENCE.getName(), 0, statistics));
		updateText(matchedReferenceLabel1, displayText(
				SourceState.MATCHED_REFERENCE.getName(), 1, statistics));
		updateText(standaloneLabel0, displayText(SourceState.STANDALONE
				.getName(), 0, statistics));
		updateText(standaloneLabel1, displayText(SourceState.STANDALONE
				.getName(), 1, statistics));
		updateText(retiredLabel0, displayText(SourceState.RETIRED.getName(), 0,
				statistics));
		updateText(retiredLabel1, displayText(SourceState.RETIRED.getName(), 1,
				statistics));
		updateText(pendingLabel0, displayText(ResultState.PENDING
				.getName(), 0, statistics));
		updateText(pendingLabel1, displayText(ResultState.PENDING
				.getName(), 1, statistics));
		updateRedGreenText(inconsistentLabel0, displayText(
				ResultState.INCONSISTENT.getName(), 0, statistics));
		updateRedGreenText(inconsistentLabel1, displayText(
				ResultState.INCONSISTENT.getName(), 1, statistics));
		updateText(integratedLabel0, displayText(ResultState.INTEGRATED
				.getName(), 0, statistics));
		updateText(integratedLabel1, displayText(ResultState.INTEGRATED
				.getName(), 1, statistics));
		lastStatistics = statistics;
	}
	private void updateRedGreenText(JLabel label, String value) {
		updateText(label, value);
		label.setForeground(value.equals("0") ? ZERO_COLOUR : NON_ZERO_COLOUR);
		label.setFont(label.getFont().deriveFont(
				value.equals("0") ? Font.PLAIN : Font.BOLD));
	}
	private void updateText(JLabel label, String value) {
		label.setText(value);
	}
	private JPanel fillerPanel1 = new JPanel();
	private JPanel fillerPanel2 = new JPanel();
	private JPanel fillerPanel3 = new JPanel();
	private JPanel fillerPanel5 = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JLabel inconsistentLabel = new JLabel();
	private JLabel inconsistentLabel0 = new JLabel();
	private JLabel inconsistentLabel1 = new JLabel();
	private JLabel integratedLabel = new JLabel();
	private JLabel integratedLabel0 = new JLabel();
	private JLabel integratedLabel1 = new JLabel();
	private Statistics lastStatistics = null;
	private JLabel layerLabel0 = new JLabel();
	private JLabel layerLabel1 = new JLabel();
	private RoadsListener listener = new RoadsListener() {
		public void roadSegmentAdded(SourceRoadSegment roadSegment) {
			update(roadSegment.getNetwork().getSession().getStatistics());
		}
		public void roadSegmentRemoved(SourceRoadSegment roadSegment) {
			update(roadSegment.getNetwork().getSession().getStatistics());
		}
		public void resultStateChanged(ResultState oldResultState,
				SourceRoadSegment roadSegment) {
			update(roadSegment.getNetwork().getSession().getStatistics());
		}
		public void stateChanged(SourceState oldState,
				SourceRoadSegment roadSegment) {
			update(roadSegment.getNetwork().getSession().getStatistics());
		}
		private void update(Statistics statistics) {
			this.statistics = statistics;
			timer.restart();
		}
		private Statistics statistics;
		private Timer timer = GUIUtil.createRestartableSingleEventTimer(500,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						StatisticsPanel.this.update(statistics);
					}
				});
		public void geometryModifiedExternally(SourceRoadSegment roadSegment) {
		}
        public void roadSegmentsChanged() {
            lastStatistics.refresh();
            //Note that this might not be called from the AWT Event Thread,
            //so it's good we're not calling StatisticsPanel.this.update
            //directly [Jon Aquino 2004-05-14]
            update(lastStatistics);
        }
	};
	private JLabel matchedNonReferenceLabel = new JLabel();
	private JLabel matchedNonReferenceLabel0 = new JLabel();
	private JLabel matchedNonReferenceLabel1 = new JLabel();
	private JLabel matchedReferenceLabel = new JLabel();
	private JLabel matchedReferenceLabel0 = new JLabel();
	private JLabel matchedReferenceLabel1 = new JLabel();
	private JLabel pendingLabel = new JLabel();
	private JLabel pendingLabel0 = new JLabel();
	private JLabel pendingLabel1 = new JLabel();
	private JButton refreshButton = new JButton();
	private JLabel retiredLabel = new JLabel();
	private JLabel retiredLabel0 = new JLabel();
	private JLabel retiredLabel1 = new JLabel();
	private JLabel standaloneLabel = new JLabel();
	private JLabel standaloneLabel0 = new JLabel();
	private JLabel standaloneLabel1 = new JLabel();
	private JLabel resultStatesLabel = new JLabel();
	private ToolboxPanel toolboxPanel;
	private JLabel totalLabel = new JLabel();
	private JLabel totalLabel0 = new JLabel();
	private JLabel totalLabel1 = new JLabel();
	private JLabel unknownLabel = new JLabel();
	private JLabel unknownLabel0 = new JLabel();
	private JLabel unknownLabel1 = new JLabel();
	private static final Color NON_ZERO_COLOUR = Color.red.darker();
	private static final Color ZERO_COLOUR = Color.green.darker();
	private JPanel unitsPanel = new JPanel();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private JComboBox unitsComboBox = new JComboBox();
	private JLabel unitsLabel = new JLabel();
}