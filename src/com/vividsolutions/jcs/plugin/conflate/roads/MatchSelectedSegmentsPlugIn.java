package com.vividsolutions.jcs.plugin.conflate.roads;

import javax.swing.JComponent;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class MatchSelectedSegmentsPlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		SourceRoadSegment[] segments = (SourceRoadSegment[]) CollectionUtil
				.collect(
						FUTURE_CollectionUtil.concatenate(context
								.getLayerViewPanel().getSelectionManager()
								.getFeaturesWithSelectedItems(
										ToolboxModel.instance(context)
												.getSourceLayer(0)), context
								.getLayerViewPanel().getSelectionManager()
								.getFeaturesWithSelectedItems(
										ToolboxModel.instance(context)
												.getSourceLayer(1))),
						new Block() {
							public Object yield(Object feature) {
								return ((SourceFeature) feature)
										.getRoadSegment();
							}
						}).toArray(new SourceRoadSegment[2]);
		CommitTool.matchUndoably(reference(segments[0], segments[1], context),
				nonReference(segments[0], segments[1], context), getClass()
						+ " - DO NOT SHOW AGAIN",
				context.getWorkbenchContext(), getName());
		return true;
	}

	private SourceRoadSegment reference(SourceRoadSegment a,
			SourceRoadSegment b, PlugInContext context) {
		return ToolboxModel.instance(context).getSession()
				.getPrecedenceRuleEngine().chooseReference(a, b);
	}

	private SourceRoadSegment nonReference(SourceRoadSegment a,
			SourceRoadSegment b, PlugInContext context) {
		return reference(a, b, context) != a ? a : b;
	}

	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME },
						getName(),
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
														.getWorkbenchContext()))
								.add(
										createCountSelectedSegmentsCheck(context
												.getWorkbenchContext())));
	}

	private EnableCheck createCountSelectedSegmentsCheck(
			final WorkbenchContext context) {
		return new EnableCheck() {
			public String check(JComponent component) {
				int network0Features = context.getLayerViewPanel()
						.getSelectionManager().getFeaturesWithSelectedItems(
								ToolboxModel.instance(context)
										.getSourceLayer(0)).size();
				int network1Features = context.getLayerViewPanel()
						.getSelectionManager().getFeaturesWithSelectedItems(
								ToolboxModel.instance(context)
										.getSourceLayer(1)).size();
				return !(network0Features == 1 && network1Features == 1) ? FUTURE_StringUtil
						.substitute(
								ErrorMessages.matchSelectedSegmentsPlugIn_wrongCounts,
								new Object[] {
										ToolboxModel.instance(context)
												.getSession().getSourceNetwork(
														0).getName(),
										ToolboxModel.instance(context)
												.getSession().getSourceNetwork(
														1).getName(),
										network0Features + "",
										network1Features + "" })
						: null;
			}
		};
	}

}