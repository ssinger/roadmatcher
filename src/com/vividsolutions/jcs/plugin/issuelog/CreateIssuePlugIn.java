package com.vividsolutions.jcs.plugin.issuelog;

import java.util.Date;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.ButtonPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

public class CreateIssuePlugIn extends AbstractPlugIn {
	private Geometry geometry;

	public boolean execute(final PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		String TYPE_KEY = getClass().getName() + " - TYPE";
		ButtonPanel buttonPanel = new ButtonPanel(new String[] { "Save",
				"Cancel" });
		final IssueLogPanel issuePanel = IssueLogPanel.prompt(getName(), "",
				"", IssueLog.AttributeValues.OPEN_STATUS,
				(String) PersistentBlackboardPlugIn.get(
						context.getWorkbenchContext()).get(TYPE_KEY,
						IssueLog.AttributeValues.COMMENT_TYPE), new Date(),
				new Date(), IssueLog.instance(context.getLayerManager())
						.getUserName(), buttonPanel, context
						.getWorkbenchContext());
		if (buttonPanel.getSelectedButton() == null
				|| buttonPanel.getSelectedButton().getText().equals("Cancel")) {
			return false;
		}
		PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(
				TYPE_KEY, issuePanel.getType());
		execute(new UndoableCommand(getName()) {
			private Feature issue = IssueLog
					.instance(context.getLayerManager()).createIssue(geometry,
							issuePanel.getType(), issuePanel.getDescription(),
							issuePanel.getComment(), issuePanel.getStatus());

			public void execute() {
				IssueLog.instance(context.getLayerManager()).addIssue(issue);
			}

			public void unexecute() {
				IssueLog.instance(context.getLayerManager()).deleteIssue(issue);
			}
		}, context);
		return true;
	}

	public CreateIssuePlugIn setGeometry(Geometry geometry) {
		this.geometry = geometry;
		return this;
	}
}