package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_Java2XML;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class SaveProfileAsPlugIn extends AbstractSaveAsPlugIn {
	private Profile createProfile(final PlugInContext context) {
		return new Profile(session(context).getSourceNetwork(0).getName(),
				session(context).getSourceNetwork(1).getName(),
				adjustmentConstraint(0, session(context)),
				adjustmentConstraint(1, session(context)), session(context)
						.getMatchOptions(), AutoAdjustOptions
						.get(session(context)), session(context)
						.getPrecedenceRuleEngine(), ResultOptions
						.get(session(context)), checkBox.isSelected(),
				(List) session(context).getBlackboard().get(
						NewSessionPlugIn.ISSUE_LOG_DESCRIPTIONS),
				(List) session(context).getBlackboard().get(
						NewSessionPlugIn.SEGMENT_COMMENTS), ((Boolean) session(
						context).getBlackboard().get(
						NewSessionPlugIn.SEGMENT_COMMENTS_EDITABLE))
						.booleanValue(), (String) session(context)
						.getBlackboard()
						.get(OpenRoadMatcherSessionPlugIn.ON_SESSION_LOAD_KEY));
	}

	public boolean execute(final PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		String key = getClass().getName() + " - LAST PROFILE LOCKED";
		checkBox.setSelected(ApplicationOptionsPlugIn.options(
				context.getWorkbenchContext()).get(key, true));
		if (JOptionPane.OK_OPTION != JOptionPane.showOptionDialog(context
				.getWorkbenchFrame(), checkBox, "Save Profile",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, new String[] { "Save", "Cancel" }, "Save")) {
			return false;
		}
		ApplicationOptionsPlugIn.options(context.getWorkbenchContext()).put(
				key, checkBox.isSelected());
		return super.execute(context);
	}

	private JCheckBox checkBox = new JCheckBox("Lock profile?", true) {
		{
			setToolTipText(StringUtil
					.split(
							"<html>Whether to prevent users from changing the settings in conflation sessions based on this profile<html>",
							80).replaceAll("\n", "<br>"));
		}
	};

	private String adjustmentConstraint(int i, ConflationSession session) {
		return !session.getSourceNetwork(i).isEditable() ? Profile.PREVENT_ADJUSTMENT_CONSTRAINT
				: session.isWarningAboutAdjustments(i) ? Profile.WARNING_ADJUSTMENT_CONSTRAINT
						: Profile.NO_ADJUSTMENT_CONSTRAINT;
	}

	public String getExtension() {
		return "rmprofile";
	}

	protected String getFileFilterDescription() {
		return "Profiles";
	}

	protected void saveAs(File file, TaskMonitor monitor, PlugInContext context)
			throws Exception {
		new FUTURE_Java2XML().write(createProfile(context), "profile", file);
	}
}