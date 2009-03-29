package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class PerformAutomaticConflationPanel extends JPanel {
	public PerformAutomaticConflationPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		autoMatchCheckBox.setText("AutoMatch");
		autoMatchCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						autoMatchCheckBox_actionPerformed(e);
					}
				});
		autoAdjustCheckBox.setText("AutoAdjust");
		this.add(autoMatchCheckBox, new GridBagConstraints(1, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(autoAdjustCheckBox, new GridBagConstraints(1, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}
	public void init(boolean profileSpecified) {
		autoMatchCheckBox.setSelected(profileSpecified);
		autoAdjustCheckBox.setSelected(profileSpecified);
		updateComponents();
	}
	private void updateComponents() {
		autoAdjustCheckBox.setEnabled(autoMatchCheckBox.isSelected());
	}
	public boolean isAutoMatchSpecified() {
		return autoMatchCheckBox.isSelected();
	}
	public boolean isAutoAdjustSpecified() {
		return autoAdjustCheckBox.isEnabled()
				&& autoAdjustCheckBox.isSelected();
	}
	private JCheckBox autoMatchCheckBox = new JCheckBox();
	private JCheckBox autoAdjustCheckBox = new JCheckBox();
	void autoMatchCheckBox_actionPerformed(ActionEvent e) {
		updateComponents();
	}
}