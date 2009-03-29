package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.*;
import javax.swing.*;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import java.awt.event.*;
public class AdjustmentConstraintsOptionsPanel extends JPanel
		implements
			OptionsPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JCheckBox constrainAdjustmentsToNetwork0CheckBox = new JCheckBox();
	private JRadioButton warnRadioButton0 = new JRadioButton();
	private JRadioButton preventRadioButton0 = new JRadioButton();
	private JCheckBox constrainAdjustmentsToNetwork1CheckBox = new JCheckBox();
	private JRadioButton warnRadioButton1 = new JRadioButton();
	private JRadioButton preventRadioButton1 = new JRadioButton();
	private ButtonGroup buttonGroup0 = new ButtonGroup();
	private ButtonGroup buttonGroup1 = new ButtonGroup();
	private JPanel fillerPanel1 = new JPanel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JPanel fillerPanel2 = new JPanel();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private WorkbenchContext context;
	private JPanel jPanel1 = new JPanel();
	public AdjustmentConstraintsOptionsPanel(WorkbenchContext context) {
		this.context = context;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	void jbInit() throws Exception {
		constrainAdjustmentsToNetwork0CheckBox
				.setText("Constrain adjustments to Network 0");
		constrainAdjustmentsToNetwork0CheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						constrainAdjustmentsToNetwork0CheckBox_actionPerformed(e);
					}
				});
		this.setLayout(gridBagLayout1);
		warnRadioButton0.setText("Warn");
		preventRadioButton0.setText("Prevent");
		constrainAdjustmentsToNetwork1CheckBox
				.setText("Constrain adjustments to Network 1");
		constrainAdjustmentsToNetwork1CheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						constrainAdjustmentsToNetwork1CheckBox_actionPerformed(e);
					}
				});
		warnRadioButton1.setText("Warn");
		preventRadioButton1.setText("Prevent");
		fillerPanel1.setLayout(gridBagLayout2);
		fillerPanel1.setPreferredSize(new Dimension(12, 12));
		fillerPanel2.setLayout(gridBagLayout3);
		this.add(constrainAdjustmentsToNetwork0CheckBox,
				new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(warnRadioButton0, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						40, 0, 0), 0, 0));
		this.add(preventRadioButton0, new GridBagConstraints(1, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 40, 0, 0), 0, 0));
		this.add(constrainAdjustmentsToNetwork1CheckBox,
				new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(warnRadioButton1, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						40, 0, 0), 0, 0));
		this.add(preventRadioButton1, new GridBagConstraints(1, 6, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 40, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		buttonGroup0.add(warnRadioButton0);
		buttonGroup0.add(preventRadioButton0);
		buttonGroup1.add(warnRadioButton1);
		buttonGroup1.add(preventRadioButton1);
		this.add(fillerPanel2, new GridBagConstraints(2, 7, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(jPanel1, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
	}
	void constrainAdjustmentsToNetwork0CheckBox_actionPerformed(ActionEvent e) {
		updateComponents();
	}
	void constrainAdjustmentsToNetwork1CheckBox_actionPerformed(ActionEvent e) {
		updateComponents();
	}
	private void updateComponents() {
		updateComponents(0);
		updateComponents(1);
	}
	private void updateComponents(int i) {
		getWarnRadioButton(i).setEnabled(
				getConstrainAdjustmentsToNetworkCheckBox(i).isSelected());
		getPreventRadioButton(i).setEnabled(
				getConstrainAdjustmentsToNetworkCheckBox(i).isSelected());
	}
	private JRadioButton getPreventRadioButton(int i) {
		return i == 0 ? preventRadioButton0 : preventRadioButton1;
	}
	private JCheckBox getConstrainAdjustmentsToNetworkCheckBox(int i) {
		return i == 0
				? constrainAdjustmentsToNetwork0CheckBox
				: constrainAdjustmentsToNetwork1CheckBox;
	}
	private JRadioButton getWarnRadioButton(int i) {
		return i == 0 ? warnRadioButton0 : warnRadioButton1;
	}
	public String validateInput() {
		return null;
	}
	public void okPressed() {
		okPressed(0);
		okPressed(1);
	}
	private void okPressed(int i) {
		getSession().setWarningAboutAdjustments(
				i,
				getConstrainAdjustmentsToNetworkCheckBox(i).isSelected()
						&& getWarnRadioButton(i).isSelected());
		getSession().getSourceNetwork(i)
				.setEditable(
						!(getConstrainAdjustmentsToNetworkCheckBox(i)
								.isSelected() && getPreventRadioButton(i)
								.isSelected()));
	}
	private ConflationSession getSession() {
		return ToolboxModel.instance(context).getSession();
	}
	public void init() {
		init(0);
		init(1);
		updateComponents();
	}
	private void init(int i) {
		getConstrainAdjustmentsToNetworkCheckBox(i).setText(
				"Constrain " + getSession().getSourceNetwork(i).getName()
						+ " adjustments");
		getConstrainAdjustmentsToNetworkCheckBox(i).setSelected(
				getSession().isWarningAboutAdjustments(i)
						|| !getSession().getSourceNetwork(i).isEditable());
		getWarnRadioButton(i).setSelected(true);
		getPreventRadioButton(i).setSelected(
				!getSession().getSourceNetwork(i).isEditable());
	}
	public boolean constrainingAdjustments(int i) {
		return getConstrainAdjustmentsToNetworkCheckBox(i).isSelected();
	}
}