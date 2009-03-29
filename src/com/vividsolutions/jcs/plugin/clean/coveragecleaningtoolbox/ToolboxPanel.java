package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import com.vividsolutions.jcs.jump.FUTURE_ValidatingTextField;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import com.vividsolutions.jump.workbench.ui.toolbox.MainButtonPlugIn;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ToolboxPanel extends JPanel {
	private LayerComboBox inputLayerComboBox = new LayerComboBox();

	private GridBagLayout gridBagLayout = new GridBagLayout();

	private JPanel upperPanel = new JPanel();

	private GridBagLayout upperPanelGridBagLayout = new GridBagLayout();

	private JLabel gapToleranceLabel = new JLabel();

	private JLabel angleToleranceLabel = new JLabel();

	private ValidatingTextField gapToleranceTextField = new ValidatingTextField(
			"", 5, SwingConstants.RIGHT,
			new ValidatingTextField.CompositeValidator(
					new ValidatingTextField.Validator[] {
							ValidatingTextField.DOUBLE_VALIDATOR,
							new ValidatingTextField.GreaterThanValidator(0) }),
			new FUTURE_ValidatingTextField.NumberCleaner("1.0"));

	private JTabbedPane resultsTabbedPane = new JTabbedPane();

	private JButton findGapsButton = new JButton("Find Gaps", IconLoader
			.icon("BlueFlag.gif"));

	private JButton findOverlapsButton = new JButton("Find Overlaps",
			IconLoader.icon("RedFlag.gif"));

	private JLabel outputLayerLabel = new JLabel();

	private JTextField outputLayerTextField = new JTextField();

	private GridLayout gridLayout1 = new GridLayout();

	private JPanel angleTolerancePanel = new JPanel();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private ValidatingTextField angleToleranceTextField = new ValidatingTextField(
			"", 5, SwingConstants.RIGHT,
			new ValidatingTextField.CompositeValidator(
					new ValidatingTextField.Validator[] {
							ValidatingTextField.DOUBLE_VALIDATOR,
							new ValidatingTextField.BoundedDoubleValidator(0,
									false, 180, false) }),
			new FUTURE_ValidatingTextField.NumberCleaner("22.5"));

	private JLabel degreesLabel = new JLabel();

	private MyTableTab gapsTab = new MyTableTab("Gaps", "LENGTH",
			resultsTabbedPane, "Find Gaps");

	private MyTableTab autoFixedTab = new MyTableTab("Auto-Fixed", "LENGTH",
			resultsTabbedPane, "Fix Gaps");

	private MyTableTab overlapsTab = new MyTableTab("Overlaps", null,
			resultsTabbedPane, "Find Overlaps");

	private Tab logTab = new Tab() {
		public Component createDefaultChild() {
			return new HTMLPanel();
		}
	};

	private LayerDependencyManager layerDependencyManager = new LayerDependencyManager(
			new MyTableTab[] { gapsTab, overlapsTab }, inputLayerComboBox) {

		{
			addDependencyListener(new LayerDependencyManager.DependencyListener() {
				public void dependencyChanged() {
					for (int i = 0; i < getDependents().length; i++) {
						((MyTableTab) getDependents()[i]).updateTitle();
					}
				}
			});
		}
	};

	private WorkbenchContext context;

	private JTabbedPane parametersTabbedPane = new JTabbedPane();

	private JPanel findGapsTab = new JPanel();

	private JPanel findOverlapsTab = new JPanel();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private JPanel inputLayerPanel = new JPanel();

	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	private JLabel inputLayerLabel = new JLabel();

	private JCheckBox fixAutomaticallyCheckBox = new JCheckBox();

	public ToolboxPanel(WorkbenchContext context) {
		try {
			this.context = context;
			jbInit();
			inputLayerComboBox.getModel().addListDataListener(
					new ListDataListener() {
						public void intervalAdded(ListDataEvent e) {
						}

						public void intervalRemoved(ListDataEvent e) {
						}

						public void contentsChanged(ListDataEvent e) {
							if ((e.getIndex0() == -1) && (e.getIndex1() == -1)) {
								//A new item has been selected. Better than
								// #actionPerformed,
								//which doesn't catch calls to
								// #setSelectedItem. [Jon Aquino]
								outputLayerTextField
										.setText((inputLayerComboBox
												.getSelectedLayer() == null) ? ""
												: ((inputLayerComboBox
														.getSelectedLayer()
														.getBlackboard()
														.get(
																MainButtonPlugIn.GENERATED_KEY) != null) ? inputLayerComboBox
														.getSelectedLayer()
														.getName()
														: (inputLayerComboBox
																.getSelectedLayer()
																.getName() + " (cleaned)")));
							}
						}
					});
			findGapsButton.addActionListener(AbstractPlugIn
					.toActionListener(new FindGapsPlugIn(this), context,
							new TaskMonitorManager()));
			findOverlapsButton.addActionListener(AbstractPlugIn
					.toActionListener(new FindOverlapsPlugIn(this), context,
							new TaskMonitorManager()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	WorkbenchContext getContext() {
		return context;
	}

	public MyTableTab getAutoFixedTab() {
		return autoFixedTab;
	}

	public MyTableTab getGapsTab() {
		return gapsTab;
	}

	public MyTableTab getOverlapsTab() {
		return overlapsTab;
	}

	public JCheckBox getFixAutomaticallyCheckBox() {
		return fixAutomaticallyCheckBox;
	}

	public JTextField getOutputLayerTextField() {
		return outputLayerTextField;
	}

	public ValidatingTextField getAngleToleranceTextField() {
		return angleToleranceTextField;
	}

	public ValidatingTextField getGapToleranceTextField() {
		return gapToleranceTextField;
	}

	public LayerComboBox getInputLayerComboBox() {
		return inputLayerComboBox;
	}

	void jbInit() throws Exception {
		this.setLayout(gridBagLayout);
		upperPanel.setLayout(upperPanelGridBagLayout);
		gapToleranceLabel.setText("Gap Tolerance: ");
		angleToleranceLabel.setText("Angle Tolerance: ");
		gapToleranceTextField.setToolTipText("");
		findGapsButton.setToolTipText("");
		outputLayerLabel.setText("Output Layer:");
		angleTolerancePanel.setLayout(gridBagLayout1);
		degreesLabel.setText(" degrees");
		outputLayerTextField.setOpaque(false);
		outputLayerTextField.setEditable(false);
		gridLayout1.setColumns(2);
		gridLayout1.setRows(2);
		resultsTabbedPane.setPreferredSize(new Dimension(250, 250));
		findGapsTab.setLayout(gridBagLayout2);
		findOverlapsTab.setLayout(gridBagLayout3);
		inputLayerPanel.setLayout(gridBagLayout4);
		inputLayerLabel.setText("Input Layer: ");
		fixAutomaticallyCheckBox.setToolTipText("");
		fixAutomaticallyCheckBox.setSelected(true);
		fixAutomaticallyCheckBox.setText("Fix Automatically");
		fixAutomaticallyCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						fixAutomaticallyCheckBox_actionPerformed(e);
					}
				});
		findGapsTab.add(upperPanel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		upperPanel.add(gapToleranceLabel, new GridBagConstraints(0, 2, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		upperPanel.add(angleToleranceLabel, new GridBagConstraints(0, 3, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		upperPanel.add(gapToleranceTextField, new GridBagConstraints(1, 2, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(resultsTabbedPane, new GridBagConstraints(0, 7, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 4, 4, 4), 0, 0));
		findGapsTab.add(findGapsButton, new GridBagConstraints(0, 3, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(4, 4, 4, 4), 0, 0));
		findOverlapsTab.add(findOverlapsButton, new GridBagConstraints(0, 0, 1,
				1, 1.0, 1.0, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
		resultsTabbedPane.add(gapsTab, gapsTab.getTitle());
		resultsTabbedPane.add(autoFixedTab, autoFixedTab.getTitle());
		resultsTabbedPane.add(overlapsTab, overlapsTab.getTitle());
		resultsTabbedPane.add(logTab, "<html>Log<br></html>");
		upperPanel.add(outputLayerLabel, new GridBagConstraints(0, 5, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		upperPanel.add(outputLayerTextField, new GridBagConstraints(1, 5, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		upperPanel.add(angleTolerancePanel, new GridBagConstraints(1, 3, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		angleTolerancePanel.add(angleToleranceTextField,
				new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		angleTolerancePanel.add(degreesLabel, new GridBagConstraints(2, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		upperPanel.add(fixAutomaticallyCheckBox, new GridBagConstraints(0, 4,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(parametersTabbedPane, new GridBagConstraints(0, 5, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(4, 4, 4, 4), 0, 0));
		parametersTabbedPane.add(findGapsTab, "Find Gaps");
		parametersTabbedPane.add(findOverlapsTab, "Find Overlaps");
		this.add(inputLayerPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		inputLayerPanel.add(inputLayerLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		inputLayerPanel.add(inputLayerComboBox, new GridBagConstraints(1, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	public HTMLPanel getLogPanel() {
		return (HTMLPanel) logTab.getChild();
	}

	public JTabbedPane getResultsTabbedPane() {
		return resultsTabbedPane;
	}

	public LayerDependencyManager getLayerDependencyManager() {
		return layerDependencyManager;
	}

	void fixAutomaticallyCheckBox_actionPerformed(ActionEvent e) {
		outputLayerLabel.setEnabled(fixAutomaticallyCheckBox.isSelected());
		outputLayerTextField.setEnabled(fixAutomaticallyCheckBox.isSelected());
	}

	public class MyTableTab extends TableTab {
		private JTabbedPane tabbedPane;

		private String basicTitle;

		private String associatedOperation;

		private JLabel errorMessageLabel = new JLabel("TEST");

		public TableTab setLayer(Layer layer, WorkbenchContext context) {
			super.setLayer(layer, context);
			updateTitle();
			return this;
		}

		public MyTableTab(String basicTitle, String sortAttributeName,
				JTabbedPane tabbedPane, String associatedOperation) {
			super(sortAttributeName);
			this.basicTitle = basicTitle;
			add(errorMessageLabel, BorderLayout.NORTH);
			errorMessageLabel.setVisible(true);
			this.tabbedPane = tabbedPane;
			this.associatedOperation = associatedOperation;
		}

		public void setChild(JPanel attributeTab) {
			super.setChild(attributeTab);
			updateTitle();
		}

		public void updateTitle() {
			if (tabbedPane.indexOfComponent(this) == -1) {
				//Get here during initialization [Jon Aquino]
				return;
			}

			tabbedPane
					.setTitleAt(tabbedPane.indexOfComponent(this), getTitle());

			if (isOutOfDate()) {
				String errorMessage = getChild() instanceof OneLayerAttributeTab ? "These results may be out of date. Please re-run "
						+ associatedOperation + "."
						: associatedOperation + " has not yet been run.";
				tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(this),
						errorMessage);
				errorMessageLabel.setText("<html><font color=#ff0000><b>"
						+ errorMessage + "</b></font></html>");
				errorMessageLabel.setVisible(true);
			} else {
				tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(this),
						null);
				errorMessageLabel.setVisible(false);
			}

			repaint();
		}

		private String getTitle() {
			String title = "<html><center>";

			if (isOutOfDate()) {
				title += "<font color=#ff0000><b>";
			}

			title += basicTitle + "<br>";

			if (getChild() instanceof OneLayerAttributeTab) {
				OneLayerAttributeTab attributeTab = (OneLayerAttributeTab) getChild();
				title += ((attributeTab.getLayer() != null) ? ("("
						+ attributeTab.getLayer().getFeatureCollectionWrapper()
								.size() + ")") : "(0)");
			} else {
				title += "";
			}

			if (isOutOfDate()) {
				title += "</b></font>";
			}

			title += "</center></html>";

			return title;
		}

		private boolean isOutOfDate() {
			return !layerDependencyManager.isUpToDate(this,
					getInputLayerComboBox().getSelectedLayer());
		}

		public Component createDefaultChild() {
			return new JPanel();
		}
	}
}