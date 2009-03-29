package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchWithStandaloneEliminationConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.ApparentNode;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
public class AutoAdjustAfterManualCommitOp {
	private static final String AUTO_ADJUSTING_AFTER_MANUAL_COMMIT_KEY = AutoAdjustAfterManualCommitOp.class
			.getName()
			+ " - AUTO ADJUSTING AFTER MANUAL COMMIT";
	public void autoAdjustFeatures(Collection seedFeatures,
			WorkbenchContext context) {
		autoAdjustRoadSegments(CollectionUtil.collect(seedFeatures,
				new Block() {
					public Object yield(Object feature) {
						return ((SourceFeature) feature).getRoadSegment();
					}
				}), context);
	}
	public void autoAdjust(MatchPathsOperation.MyUndoableCommand command,
			WorkbenchContext context) {
		Assert.isTrue(command.isExecutionSuccessful());
		autoAdjustRoadSegments(command.getTransaction()
				.getAddedAndModifiedRoadSegments(), context);
	}
	public void autoAdjustRoadSegments(Collection seedRoadSegments,
			WorkbenchContext context) {
		if (!isAutoAdjustingAfterManualCommit(context)) {
			return;
		}
		checkConsistencyRule(context);
		Set roadSegments = new HashSet(seedRoadSegments);
		for (Iterator i = seedRoadSegments.iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			roadSegments.addAll(includedRoadSegmentsIncidentOn(collection(
					AdjustedMatchConsistencyRule.START_NEIGHBOURHOOD_KEY,
					roadSegment), session(context)));
			roadSegments.addAll(includedRoadSegmentsIncidentOn(collection(
					AdjustedMatchConsistencyRule.END_NEIGHBOURHOOD_KEY,
					roadSegment), session(context)));
		}
		if (new AutoAdjustOp().checkAutoAdjustNotConstrained(context)) {
			new AutoAdjustOp().autoAdjust(StringUtil.toFriendlyName(getClass()
					.getName()), roadSegments, new DummyTaskMonitor(), context,
					ToolboxModel.instance(context)
							.getConsistencyConfiguration().getAutoAdjuster());
		}
	}
	public static void setAutoAdjustingAfterManualCommit(
			boolean autoAdjustingAfterManualCommit, WorkbenchContext context) {
		ApplicationOptionsPlugIn.options(context).put(
				AUTO_ADJUSTING_AFTER_MANUAL_COMMIT_KEY,
				autoAdjustingAfterManualCommit);
	}
	public static boolean isAutoAdjustingAfterManualCommit(
			WorkbenchContext context) {
		return ApplicationOptionsPlugIn.options(context).get(
				AUTO_ADJUSTING_AFTER_MANUAL_COMMIT_KEY, true);
	}
	private Collection collection(String key, SourceRoadSegment roadSegment) {
		return (Collection) LangUtil.ifNull(roadSegment
				.getResultStateDescription().getBlackboard().get(key),
				new ArrayList());
	}
	private Collection includedRoadSegmentsIncidentOn(
			Collection apparentNodeLocations, ConflationSession session) {
		Collection includedRoadSegments = new ArrayList();
		for (Iterator i = apparentNodeLocations.iterator(); i.hasNext();) {
			Coordinate location = (Coordinate) i.next();
			includedRoadSegments
					.addAll(new ApparentNode(
							location, session)
							.getIncludedIncidentRoadSegments());
		}
		return includedRoadSegments;
	}
	private void autoAdjust(Collection neighbourhood) {
		throw new UnsupportedOperationException();
	}
	private void checkConsistencyRule(WorkbenchContext context) {
		if (!(session(context).getConsistencyRule() instanceof AdjustedMatchConsistencyRule)
				&& !(session(context).getConsistencyRule() instanceof AdjustedMatchWithStandaloneEliminationConsistencyRule)) {
			throw new RuntimeException(
					FUTURE_StringUtil
							.substitute(
									ErrorMessages.autoAdjustAfterManualCommitOp_unsupportedConsistencyRule,
									new Object[]{StringUtil
											.toFriendlyName(session(context)
													.getConsistencyRule()
													.getClass().getName())}));
		}
	}
	private ConflationSession session(WorkbenchContext context) {
		return ToolboxModel.instance(context).getSession();
	}
}