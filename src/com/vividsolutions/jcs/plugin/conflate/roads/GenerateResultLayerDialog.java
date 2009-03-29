package com.vividsolutions.jcs.plugin.conflate.roads;

import javax.swing.JDialog;

import javax.swing.JPanel;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class GenerateResultLayerDialog extends JDialog {

	private javax.swing.JPanel jContentPane = null;

	private JPanel southPanel = null;

	private JPanel optionsButtonPanel = null;

	private WorkbenchContext context;

	private GenerateResultLayerPanel panel = new GenerateResultLayerPanel();

	public GenerateResultLayerDialog(Frame owner, String title,
			WorkbenchContext context) throws HeadlessException {
		super(owner, title, true);
		initialize();
		jContentPane.add(panel, java.awt.BorderLayout.CENTER);
		pack();
		GUIUtil.centreOnWindow(this);
		this.context = context;
	}

	private void initialize() {
		panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		this.setContentPane(getJContentPane());
	}

	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getSouthPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(panel, java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	private JPanel getSouthPanel() {
		if (southPanel == null) {
			southPanel = new JPanel();
			southPanel.setLayout(new BorderLayout());
			southPanel.add(getOptionsButtonPanel(), java.awt.BorderLayout.WEST);
			southPanel.add(okCancelPanel, java.awt.BorderLayout.EAST);
		}
		return southPanel;
	}

	private OKCancelPanel okCancelPanel = new OKCancelPanel() {
		{
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GenerateResultLayerDialog.this.setVisible(false);
				}
			});
		}
	};

	private JButton optionsButton = null;

	private JPanel getOptionsButtonPanel() {
		if (optionsButtonPanel == null) {
			optionsButtonPanel = new JPanel();
			optionsButtonPanel.setBorder(javax.swing.BorderFactory
					.createEmptyBorder(0, 0, 0, 40));
			optionsButtonPanel.add(getOptionsButton(), null);
		}
		return optionsButtonPanel;
	}

	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setText("Options...");
			optionsButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							try {
								AbstractPlugIn.toActionListener(
										new ResultOptionsPlugIn(), context,
										new TaskMonitorManager())
										.actionPerformed(null);
							} catch (Exception x) {
								throw new RuntimeException(x);
							}
						}
					});
		}
		return optionsButton;
	}

	public boolean wasOKPressed() {
		return okCancelPanel.wasOKPressed();
	}

	public GenerateResultLayerPanel getPanel() {
		return panel;
	}

} //  @jve:decl-index=0:visual-constraint="10,10"
