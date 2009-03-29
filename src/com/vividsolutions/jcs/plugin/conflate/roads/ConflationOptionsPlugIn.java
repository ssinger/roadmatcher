package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_OptionsDialog;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;

public class ConflationOptionsPlugIn extends AbstractOptionsPlugIn {
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

	protected OptionsDialog createDialog(WorkbenchContext context) {
		OptionsDialog dialog = FUTURE_OptionsDialog.construct(context
				.getWorkbench().getFrame(), getName(), true);
		dialog.setTitle(getName());
		dialog.addTab("Precedence", new EnableDisableWrapper(
				new PrecedenceOptionsPanel(context), context));
		dialog.addTab("AutoMatch", new EnableDisableWrapper(
				new AutoMatchOptionsPanel(context), context));
		dialog.addTab("Adjustment Constraints", new EnableDisableWrapper(
				new AdjustmentConstraintsOptionsPanel(context), context));
		dialog.addTab("AutoAdjust", new EnableDisableWrapper(
				new AutoAdjustConflationOptionsPanel(context), context));
		//Workaround for Java bug 4879877: JTabbedPane#getPreferredSize
		//returns incorrect value for multi-line tabs: Ensure dialog is wide
		//enough to prevent a second row of tabs [Jon Aquino 2004-05-12]
		FUTURE_OptionsDialog.setMinWidth(367, dialog);
		return dialog;
	}

	private void clearCachedFields(PlugInContext context) {
		for (Iterator i = session(context).getSourceNetwork(0).getGraph()
				.getEdges().iterator(); i.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			segment.clearCachedFields();
			//This also clears the fields in the other network, so no need
			//to call it again on the other network. [Jon Aquino 2004-10-26]
		}
	}

	private double nearnessTolerance(ConflationSession session) {
		return session.getMatchOptions().getEdgeMatchOptions()
				.getNearnessTolerance();
	}

	private ConflationSession session(PlugInContext context) {
		return ToolboxModel.instance(context).getSession();
	}

	public static class EnableDisableWrapper extends JPanel implements
			OptionsPanel {
		private OptionsPanel panel;

		private WorkbenchContext context;

		public EnableDisableWrapper(OptionsPanel panel, WorkbenchContext context) {
			super(new BorderLayout());
			add((Component) panel, BorderLayout.CENTER);
			this.panel = panel;
			this.context = context;
		}

		public String validateInput() {
			return panel.validateInput();
		}

		public void okPressed() {
			panel.okPressed();
		}

		public void init() {
			if (/* NOTE */!ToolboxModel.instance(context).getSession()
					.isLocked()) {
				assignEnabled(true, this);
			}
			panel.init();
			if (ToolboxModel.instance(context).getSession().isLocked()) {
				assignEnabled(false, this);
			}
		}

		private void assignEnabled(boolean enabled, Container container) {
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component c = container.getComponent(i);
				if (c instanceof JLabel) {
					continue;
				}
				c.setEnabled(enabled);
				if (c instanceof JTextField) {
					c.setBackground(enabled ? new JTextField().getBackground()
							: new JPanel().getBackground());
				}
				if (c instanceof JList) {
					c.setBackground(enabled ? new JList().getBackground()
							: new JPanel().getBackground());
				}
				if (c instanceof Container) {
					assignEnabled(enabled, (Container) c);
				}
			}
		}
	}
}