package com.vividsolutions.jcs.plugin.issuelog;

import java.util.Date;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.ButtonPanel;

public class EditIssuePlugIn extends AbstractPlugIn {

    private Feature issue;

    public boolean execute(final PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);
        ButtonPanel buttonPanel = new ButtonPanel(new String[] { "Save",
                "Delete", "Cancel"});
        IssueLogPanel issuePanel = IssueLogPanel.prompt(getName(), issue
                .getString(IssueLog.AttributeNames.DESCRIPTION), issue
                .getString(IssueLog.AttributeNames.COMMENT), issue
                .getString(IssueLog.AttributeNames.STATUS), issue
                .getString(IssueLog.AttributeNames.TYPE), (Date) issue
                .getAttribute(IssueLog.AttributeNames.CREATION_DATE),
                new Date(),
                IssueLog.instance(context.getLayerManager()).getUserName(),
                buttonPanel, context.getWorkbenchContext());
        if (buttonPanel.getSelectedButton() == null
                || buttonPanel.getSelectedButton().getText().equals("Cancel")) { return false; }
        if (buttonPanel.getSelectedButton() == buttonPanel.getButton("Delete")) {
            delete(issue, context);
        } else {
            update(issuePanel.getDescription(), issuePanel.getComment(), issuePanel.getStatus(),
                    issuePanel.getType(), issue, context);
        }
        return true;
    }

    private void delete(final Feature closestIssueFeature,
            final PlugInContext context) {
        myExecute(new UndoableCommand(getName()) {

            public void execute() {
                IssueLog.instance(context.getLayerManager()).deleteIssue(
                        closestIssueFeature);
            }

            public void unexecute() {
                IssueLog.instance(context.getLayerManager()).addIssue(
                        closestIssueFeature);
            }
        }, context);
    }

    private void myExecute(UndoableCommand command, final PlugInContext context) {
        //Call #execute(..., LayerManagerProxy) rather than
        //#execute(..., PlugInContext). The latter calls PlugInContext#getLayerViewPanel,
        //but the active JInternalFrame may be an attribute viewer,
        //not a TaskFrame, leading to a NullPointerException.
        //[Jon Aquino 2004-03-10]
        execute(command, (LayerManagerProxy)context);
    }

    public void update(final String description, final String comment, final String status,
            final String type, final Feature issueFeature,
            final PlugInContext context) {
        final Feature originalIssueFeatureClone = issueFeature.clone(true);
        myExecute(new UndoableCommand(getName()) {

            public void execute() {
                IssueLog.instance(context.getLayerManager()).updateIssue(
                        issue,
                        type,
                        description,
                        comment,
                        status);
            }

            public void unexecute() {
                IssueLog
                        .instance(context.getLayerManager())
                        .updateIssue(
                                issue,
                                originalIssueFeatureClone
                                        .getString(IssueLog.AttributeNames.TYPE),
                                originalIssueFeatureClone
                                        .getString(IssueLog.AttributeNames.DESCRIPTION),
                                originalIssueFeatureClone
                                        .getString(IssueLog.AttributeNames.COMMENT),
                                originalIssueFeatureClone
                                        .getString(IssueLog.AttributeNames.STATUS));
            }
        }, context);
    }

    public EditIssuePlugIn setIssue(Feature issue) {
        this.issue = issue;
        return this;
    }
}
