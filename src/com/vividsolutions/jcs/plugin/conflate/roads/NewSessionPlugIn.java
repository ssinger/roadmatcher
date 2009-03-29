package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import bsh.EvalError;
import bsh.Interpreter;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;

public class NewSessionPlugIn extends ThreadedBasePlugIn {
	private WizardDialog dialog;

	private static final String PROFILE_FILES = NewSessionPlugIn.class
			.getName()
			+ " - PROFILE FILES";

	public static final String ISSUE_LOG_DESCRIPTIONS = NewSessionPlugIn.class
			.getName()
			+ " - ISSUE LOG DESCRIPTIONS";

	public static final String SEGMENT_COMMENTS = NewSessionPlugIn.class
			.getName()
			+ " - SEGMENT COMMENTS";

	public static final String SEGMENT_COMMENTS_EDITABLE = NewSessionPlugIn.class
			.getName()
			+ " - SEGMENT COMMENTS EDITABLE";

	public NewSessionPlugIn(WorkbenchContext context) {
	}

	protected ToolboxPanel toolboxPanel(WorkbenchContext context) {
		return RoadMatcherToolboxPlugIn.instance(context).getToolboxPanel();
	}

	private void createDialog(PlugInContext context) {
		dialog = new WizardDialog(context.getWorkbenchFrame(), getName(),
				context.getErrorHandler());
		dialog.setData(ChooseConflationProfileWizardPanel.WIZARD_PROFILE_FILES,
				ApplicationOptionsPlugIn.options(context.getWorkbenchContext())
						.get(PROFILE_FILES, new ArrayList()));
		dialog
				.init(new WizardPanel[] {
						new ChooseConflationProfileWizardPanel(context
								.getWorkbenchContext()),
						new SelectInputLayersWizardPanel(context
								.getWorkbenchContext()),
						new SelectNodeConstraintLayersWizardPanel(context
								.getWorkbenchContext()),
						new PerformAutomaticConflationWizardPanel() });
		GUIUtil.centreOnWindow(dialog);
	}

	private static boolean javaVersionOld() {
		if (System.getProperty("java.version") == null) {
			return true;
		}
		String version[] = (String[]) StringUtil.fromCommaDelimitedString(
				StringUtil.replaceAll(System.getProperty("java.version"), ".",
						",")).toArray(new String[] {});
		if (Integer.parseInt(version[0]) > 1) {
			return false;
		}
		if (Integer.parseInt(version[1]) > 3) {
			return false;
		}
		return true;
	}

	public static void warnIfJavaVersionOld(PlugInContext context) {
		if (!javaVersionOld()) {
			return;
		}
		context.getWorkbenchFrame().warnUser(
				FUTURE_StringUtil.substitute(
						ErrorMessages.newSessionPlugIn_javaVersionOld,
						new String[] { System.getProperty("java.version") }));
	}

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		warnIfJavaVersionOld(context);
		if (ToolboxModel.instance(context).isInitialized()
				&& !FUTURE_PreventableConfirmationDialog
						.show(
								context.getWorkbenchFrame(),
								"JUMP",
								null,
								ErrorMessages.newSessionPlugIn_existingSession_dialogText,
								"Proceed anyway", "Cancel", getClass()
										.getName()
										+ " - DO NOT SHOW AGAIN")) {
			return false;
		}
		createDialog(context);
		dialog.setVisible(true);
		ApplicationOptionsPlugIn
				.options(context.getWorkbenchContext())
				.put(
						PROFILE_FILES,
						dialog
								.getData(ChooseConflationProfileWizardPanel.WIZARD_PROFILE_FILES));
		if (!dialog.wasFinishPressed()) {
			return false;
		}
		return true;
	}

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
										context
												.getCheckFactory()
												.createAtLeastNLayersMustExistCheck(
														2)).add(
										new EnableCheck() {
											public String check(
													JComponent component) {
												//Can't use
												//ToolboxModel#instance(PlugInContext)
												//at this point, because
												// PlugInContext#getLayerManager
												//returns null [Jon Aquino
												// 2004-02-26]
												return ToolboxModel
														.instance(
																context
																		.getWorkbenchContext()
																		.getLayerManager(),
																context
																		.getWorkbenchContext())
														.nonConflationLayers()
														.size() < 2 ? ErrorMessages.newSessionPlugIn_nonConflationLayerCount
														: null;
											}
										}));
	}

	public void run(TaskMonitor monitor, final PlugInContext context)
			throws Exception {
		getLayer(0).setVisible(false);
		getLayer(1).setVisible(false);
		Profile profile = getProfile();
		WorkbenchContext workbenchContext = context.getWorkbenchContext();
		monitor.report("Creating Road Networks");
		final ConflationSession session = createSession(
				ConflationSession.DEFAULT_NAME,
				getLayer(0).getFeatureCollectionWrapper().getUltimateWrappee(),
				getLayer(1).getFeatureCollectionWrapper().getUltimateWrappee(),
				((FeatureCollection) dialog
						.getData(SelectNodeConstraintLayersWizardPanel.NODE_CONSTRAINT_FEATURE_COLLECTION_0_KEY)),
				((FeatureCollection) dialog
						.getData(SelectNodeConstraintLayersWizardPanel.NODE_CONSTRAINT_FEATURE_COLLECTION_1_KEY)),
				AddNewLayerPlugIn.createBlankFeatureCollection(),
				AddNewLayerPlugIn.createBlankFeatureCollection(), profile,
				ToolboxModel.instance(workbenchContext)
						.getConsistencyConfiguration());
		initializeTask(
				session,
				profile,
				dialog
						.getData(PerformAutomaticConflationWizardPanel.AUTOMATCH_KEY) == Boolean.TRUE,
				dialog
						.getData(PerformAutomaticConflationWizardPanel.AUTOADJUST_KEY) == Boolean.TRUE,
				monitor, workbenchContext);
	}

	public static void initializeTask(final ConflationSession session,
			Profile profile, boolean autoMatch, boolean autoAdjust,
			TaskMonitor monitor, final WorkbenchContext workbenchContext)
			throws Exception {
		ToolboxModel.instance(workbenchContext).initialize(session);
		monitor.report("Validating input data");
		ToolboxModel.instance(workbenchContext).validateInput();
		new ConflationSessionInitializer().initialize(session, workbenchContext
				.getTask(), workbenchContext);
		// No need to check the autoAdjust flag, as autoMatch is a prerequisite.
		// [Jon Aquino 2004-09-07]
		if (validationErrorMessage(session) == null && autoMatch) {
			new AutoConflatePlugIn().setAutoMatching(autoMatch)
					.setAutoAdjusting(autoAdjust).run(monitor,
							workbenchContext.createPlugInContext());
		}
		if (validationErrorMessage(session) != null && autoMatch) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									workbenchContext.getWorkbench().getFrame(),
									StringUtil
											.split(
													"Automated conflation has been cancelled due to errors in the input data.",
													80),
									"Automated Conflation Cancelled",
									JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		if (validationErrorMessage(session) != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(workbenchContext
							.getWorkbench().getFrame(),
							validationErrorMessage(session), "Input Errors",
							JOptionPane.WARNING_MESSAGE);
				}
			});
		}
	}

	private static String validationErrorMessage(ConflationSession session) {
		if (validationErrorMessages(session).isEmpty()) {
			return null;
		}
		String fullMessage = "<HTML>The following errors were found in the input data.  Please review before continuing.<UL>";
		for (Iterator i = validationErrorMessages(session).iterator(); i
				.hasNext();) {
			String message = (String) i.next();
			fullMessage += "<LI>" + message;
		}
		fullMessage += "</UL></HTML>";
		return fullMessage;
	}

	public static Collection validationErrorMessages(ConflationSession session) {
		ArrayList messages = new ArrayList();
		if (session.getCoincidentSegments()[0].size()
				+ session.getCoincidentSegments()[1].size() > 0) {
			messages.add(ErrorMessages.newSessionPlugIn_coincidentSegments);
		}
		if (session.getIllegalGeometries()[0].size()
				+ session.getIllegalGeometries()[1].size() > 0) {
			messages.add(ErrorMessages.newSessionPlugIn_illegalGeometries);
		}
		if (session.getUnmatchedNodeConstraints()[0].size()
				+ session.getUnmatchedNodeConstraints()[1].size() > 0) {
			messages
					.add(ErrorMessages.newSessionPlugIn_unmatchedNodeConstraints);
		}
		return messages;
	}

	public static ConflationSession createSession(String name,
			FeatureCollection originalFeatureCollection0,
			FeatureCollection originalFeatureCollection1,
			FeatureCollection nodeConstraints0,
			FeatureCollection nodeConstraints1,
			FeatureCollection contextFeatureCollection0,
			FeatureCollection contextFeatureCollection1, Profile profile,
			ConsistencyConfiguration consistencyConfiguration) throws EvalError {
		ConflationSession session = new ConflationSession(name,
				originalFeatureCollection0, originalFeatureCollection1,
				contextFeatureCollection0, contextFeatureCollection1,
				nodeConstraints0, nodeConstraints1)
				.setConsistencyRule(consistencyConfiguration.getRule());
		profile.getResultOptions().setSession(session);
		session.setMatchOptions(profile.getRoadMatchOptions());
		session.setPrecedenceRuleEngine(profile.getPrecedenceRuleEngine());
		session.setLocked(profile.isLocked());
		session.getBlackboard().put(
				OpenRoadMatcherSessionPlugIn.ON_SESSION_LOAD_KEY,
				profile.getOnSessionLoadScript());
		AutoAdjustOptions.set(profile.getAutoAdjustOptions(), session);
		ResultOptions.set(profile.getResultOptions(), session);
		session.getSourceNetwork(0).setName(profile.dataset(0).getShortName());
		session.getSourceNetwork(1).setName(profile.dataset(1).getShortName());
		session.getSourceNetwork(0).setEditable(
				!profile.dataset(0).getAdjustmentConstraint().equals(
						Profile.PREVENT_ADJUSTMENT_CONSTRAINT));
		session.getSourceNetwork(1).setEditable(
				!profile.dataset(1).getAdjustmentConstraint().equals(
						Profile.PREVENT_ADJUSTMENT_CONSTRAINT));
		session.setWarningAboutAdjustments(0, profile.dataset(0)
				.getAdjustmentConstraint().equals(
						Profile.WARNING_ADJUSTMENT_CONSTRAINT));
		session.setWarningAboutAdjustments(1, profile.dataset(1)
				.getAdjustmentConstraint().equals(
						Profile.WARNING_ADJUSTMENT_CONSTRAINT));
		session.getBlackboard().put(ISSUE_LOG_DESCRIPTIONS,
				profile.getIssueLogDescriptions());
		session.getBlackboard().put(SEGMENT_COMMENTS,
				profile.getSegmentComments());
		session.getBlackboard().put(SEGMENT_COMMENTS_EDITABLE,
				Boolean.valueOf(profile.getSegmentCommentsEditable()));
		return session;
	}

	private Layer getLayer(int i) {
		return (Layer) dialog
				.getData(i == 0 ? SelectInputLayersWizardPanel.LAYER_A_KEY
						: SelectInputLayersWizardPanel.LAYER_B_KEY);
	}

	private Profile getProfile() {
		return (Profile) dialog
				.getData(ChooseConflationProfileWizardPanel.SELECTED_PROFILE_KEY);
	}
}