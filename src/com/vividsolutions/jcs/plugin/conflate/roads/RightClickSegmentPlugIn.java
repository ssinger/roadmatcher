package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.plugin.issuelog.FeatureAtClickFinder;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * A plug-in that operates on the segment that was right-clicked on. If the
 * segment was selected, operates on all selected segments.
 */
public abstract class RightClickSegmentPlugIn extends AbstractPlugIn {
	public boolean execute(final PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		SourceFeature feature = (SourceFeature) FeatureAtClickFinder
				.featureAtClick(FUTURE_CollectionUtil.list(ToolboxModel
						.instance(context).getSourceLayer(0)
						.getFeatureCollectionWrapper(), ToolboxModel.instance(
						context).getSourceLayer(1)
						.getFeatureCollectionWrapper()), context);
		if (feature == null) {
			context.getLayerViewPanel().getContext().warnUser(
					ErrorMessages.rightClickSegmentPlugIn_noSegmentHere);
			return false;
		}
		return execute(
				context.getLayerViewPanel().getSelectionManager()
						.getFeaturesWithSelectedItems().contains(feature) ? (List) CollectionUtil
						.collect(CollectionUtil.select(context
								.getLayerViewPanel().getSelectionManager()
								.getFeaturesWithSelectedItems(), new Block() {
							public Object yield(Object feature) {
								return Boolean
										.valueOf(feature instanceof SourceFeature);
							}
						}), new Block() {
							public Object yield(Object feature) {
								return ((SourceFeature) feature)
										.getRoadSegment();
							}
						})
						: Collections.singletonList(feature.getRoadSegment()),
				context);
	}

	protected abstract boolean execute(final List segments,
			final PlugInContext context);

	protected void fireFeaturesChanged(final int i, List segments,
			PlugInContext context) {
		Collection features = CollectionUtil.collect(CollectionUtil.select(
				segments, new Block() {
					public Object yield(Object segment) {
						return Boolean
								.valueOf(i == ((SourceRoadSegment) segment)
										.getNetworkID());
					}
				}), new Block() {
			public Object yield(Object segment) {
				return ((SourceRoadSegment) segment).getFeature();
			}
		});
		if (features.isEmpty()) {
			return;
		}
		context.getLayerManager().fireFeaturesChanged(features,
				FeatureEventType.ATTRIBUTES_MODIFIED,
				ToolboxModel.instance(context).getSourceLayer(i));
	}

	public static MultiEnableCheck createEnableCheck(final PlugInContext context) {
		// No need to check what kind of window is active, as this plug-in is
		// placed in the right-click menu of the correct window (task frame or
		// attribute viewer) [Jon Aquino 2004-11-12]
		return new MultiEnableCheck().add(SpecifyRoadFeaturesTool
				.createConflationSessionMustBeStartedCheck(context
						.getWorkbenchContext()));
	}
}