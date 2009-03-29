package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

public class AutoConflatePlugIn extends ThreadedBasePlugIn {
	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME },
						getName() + "...",
						false,
						null,
						new MultiEnableCheck()
								.add(
										context
												.getCheckFactory()
												.createWindowWithLayerViewPanelMustBeActiveCheck())
								.add(
										SpecifyRoadFeaturesTool
												.createConflationSessionMustBeStartedCheck(context
														.getWorkbenchContext())));
	}

	public String getName() {
		//Ensure no space [Jon Aquino 2004-07-29]
		return "AutoConflate";
	}

	public boolean execute(PlugInContext context) throws Exception {
		final JPanel panel = new JPanel(new GridBagLayout());
		final JCheckBox autoMatchCheckBox = checkBox(0, "AutoMatch",
				panel, context);
		final JCheckBox autoAdjustCheckBox = checkBox(1, "AutoAdjust",
				panel, context);
		panel.add(conflationOptionsButton(context.getWorkbenchContext()),
				new GridBagConstraints(1, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 15, 0, 0), 0, 0));
		ActionListener okButtonEnabler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FUTURE_GUIUtil.findDescendant(
						SwingUtilities.windowForComponent(panel), new Block() {
							public Object yield(Object child) {
								return Boolean.valueOf(child instanceof JButton
										&& ((JButton) child).getText().equals(
												"OK"));
							}
						}).setEnabled(
						autoMatchCheckBox.isSelected()
								|| autoAdjustCheckBox.isSelected());
			}
		};
		autoMatchCheckBox.addActionListener(okButtonEnabler);
		autoAdjustCheckBox.addActionListener(okButtonEnabler);
		if (JOptionPane.OK_OPTION != JOptionPane.showOptionDialog(context
				.getWorkbenchFrame(), panel, "AutoConflation",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				null, null)) {
			return false;
		}
		blackboard(context).put(key(autoMatchCheckBox),
				autoMatchCheckBox.isSelected());
		blackboard(context).put(key(autoAdjustCheckBox),
				autoAdjustCheckBox.isSelected());
		autoMatching = autoMatchCheckBox.isSelected();
		autoAdjusting = autoAdjustCheckBox.isSelected();
		return true;
	}

	private JButton conflationOptionsButton(final WorkbenchContext context) {
		return new JButton("Conflation Options...") {
			{
				//setMargin(new Insets(0, 0, 0, 0));
				addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AbstractPlugIn.toActionListener(
								new ConflationOptionsPlugIn(), context, null)
								.actionPerformed(null);
					}
				});
			}
		};
	}

	private String key(JCheckBox checkBox) {
		return key(checkBox.getText());
	}

	private String key(String text) {
		return getClass().getName() + " - " + text;
	}

	private JCheckBox checkBox(int gridy, String text, final JPanel panel,
			PlugInContext context) {
		final JCheckBox autoMatchCheckBox = new JCheckBox(text, blackboard(
				context).get(key(text), true));
		panel.add(autoMatchCheckBox, new GridBagConstraints(0, gridy, 1, 1, 0,
				0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		return autoMatchCheckBox;
	}

	private Blackboard blackboard(PlugInContext context) {
		return PersistentBlackboardPlugIn.get(context.getWorkbenchContext());
	}

	private boolean autoMatching;

	private boolean autoAdjusting;

	public void run(TaskMonitor monitor, final PlugInContext context)
			throws Exception {
		final String summary[] = { "" };
		if (autoMatching) {
			summary[0] += AutoMatchPlugIn.summary("was", context);
			new AutoMatchPlugIn().run(monitor, context);
			context.getLayerManager().getUndoableEditReceiver()
					.reportIrreversibleChange();
		}
		if (autoAdjusting) {
			//summary[0] += summary[0].length() == 0 ? "" : "\n\n\n";
			summary[0] += new AutoAdjustPlugIn().runProper(monitor, context);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(context.getWorkbenchFrame(),
						summary[0], "AutoConflation Summary",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
	public AutoConflatePlugIn setAutoAdjusting(boolean autoAdjusting) {
		this.autoAdjusting = autoAdjusting;
		return this;
	}
	public AutoConflatePlugIn setAutoMatching(boolean autoMatching) {
		this.autoMatching = autoMatching;
		return this;
	}
}