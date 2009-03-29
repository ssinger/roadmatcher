package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

/**
 * Deletes all SplitRoadSegment children of a SourceRoadSegment, then adds the
 * SourceRoadSegment.
 */
public class RevertToOriginalSegmentOp {
	/**
	 * Pre-condition: the children are in the Unknown state.
	 * 
	 * @param parent
	 *            typically not in the network (yet).
	 */
	public void unsplit(SourceRoadSegment parent, ToolboxModel toolboxModel) {
		Collection children = children(parent);
		// Sidestep the effort required to break matches for segments in the
		// Matched state. [Jon Aquino 2004-12-23]
		assertChildrenInUnknownState(children);
		for (Iterator i = children.iterator(); i.hasNext();) {
			SplitRoadSegment child = (SplitRoadSegment) i.next();
			parent.getNetwork().remove(child);
		}
		parent.getNetwork().add(parent);
		toolboxModel.getLayerManager().fireFeaturesChanged(features(children),
				FeatureEventType.DELETED,
				toolboxModel.getSourceLayer(parent.getNetworkID()));
		toolboxModel.getLayerManager().fireFeaturesChanged(
				features(Collections.singleton(parent)),
				FeatureEventType.ADDED,
				toolboxModel.getSourceLayer(parent.getNetworkID()));
	}

	private Collection features(Collection segments) {
		return CollectionUtil.collect(segments, new Block() {
			public Object yield(Object segment) {
				return ((SourceRoadSegment) segment).getFeature();
			}
		});
	}

	public static Collection children(SourceRoadSegment parent) {
		return CollectionUtil.concatenate(RevertAllOp
				.parentSegmentToSplitSegmentsMap(Collections.singleton(parent),
						parent.getNetwork()).values());
	}

	private void assertChildrenInUnknownState(Collection children) {
		for (Iterator i = children.iterator(); i.hasNext();) {
			SplitRoadSegment child = (SplitRoadSegment) i.next();
			Assert.isTrue(child.getState() == SourceState.UNKNOWN);
		}
	}
}