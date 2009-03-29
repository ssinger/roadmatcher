package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import com.vividsolutions.jcs.conflate.roads.match.RoadSegmentsMutualBestMatcher;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

public class CommitTool extends SpecifyRoadFeaturesTool {
	public static abstract class Mode {
		private Mode() {
		}

		public abstract boolean canSetFeaturesToStandaloneFor(Layer layer,
				CommitTool tool);

		public abstract String getNoRoadSegmentsWarning(CommitTool tool);

		public abstract SourceRoadSegment reference(SourceRoadSegment a,
				SourceRoadSegment b);
	}

	private static abstract class OneLayerMode extends Mode {
		public boolean canSetFeaturesToStandaloneFor(Layer layer,
				CommitTool tool) {
			return layer(tool) == layer;
		}

		public String getNoRoadSegmentsWarning(CommitTool tool) {
			return FUTURE_StringUtil.substitute(
					ErrorMessages.commitTool_oneLayer_noRoadSegments,
					new Object[] { layer(tool) });
		}

		protected abstract int getReferenceNetworkID();

		public SourceRoadSegment reference(SourceRoadSegment a,
				SourceRoadSegment b) {
			return a.getNetworkID() == getReferenceNetworkID() ? a : b;
		}

		protected abstract Layer layer(CommitTool tool);
	}

	public CommitTool(Mode mode, String cursorImage, String buttonImage,
			Color color, WorkbenchContext context) {
		super(true, true, cursorImage, buttonImage, color, context,
				GestureMode.LINE);
		this.mode = mode;
	}

	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		if (!checkConflationSessionStarted(getContext())) {
			return;
		}
		noRoadSegmentsWarning = mode.getNoRoadSegmentsWarning(this);
		Map layerToSpecifiedFeaturesMap = layerToSpecifiedFeaturesMap();
		if (layerToSpecifiedFeaturesMap.size() == 0) {
			warnUser(noRoadSegmentsWarning, getContext());
			return;
		}
		if (layerToSpecifiedFeaturesMap.size() == 1) {
			handleFeaturesFromOneLayer(layerToSpecifiedFeaturesMap);
			return;
		}
		if (layerToSpecifiedFeaturesMap.size() == 2) {
			Map layerToUnretiredFeaturesMap = CollectionUtil
					.createMap(new Object[] {
							ToolboxModel.instance(getContext()).getSourceLayer(
									0),
							unretiredFeatures((Collection) layerToSpecifiedFeaturesMap
									.get(ToolboxModel.instance(getContext())
											.getSourceLayer(0))),
							ToolboxModel.instance(getContext()).getSourceLayer(
									1),
							unretiredFeatures((Collection) layerToSpecifiedFeaturesMap
									.get(ToolboxModel.instance(getContext())
											.getSourceLayer(1))) });
			handleFeaturesFromBothLayers(CollectionUtil.select(
					layerToUnretiredFeaturesMap.values(), new Block() {
						public Object yield(Object arg) {
							return Boolean
									.valueOf(((Collection) arg).size() == 1);
						}
					}).size() == 2 ? layerToUnretiredFeaturesMap
					: layerToSpecifiedFeaturesMap);
			return;
		}
		Assert.shouldNeverReachHere();
	}

	private Collection unretiredFeatures(Collection features) {
		return CollectionUtil.select(features, new Block() {
			public Object yield(Object feature) {
				return Boolean.valueOf(((SourceFeature) feature)
						.getRoadSegment().getState() != SourceState.RETIRED);
			}
		});
	}

	private void handleFeaturesFromBothLayers(Map layerToSpecifiedFeaturesMap) {
		for (Iterator i = layerToSpecifiedFeaturesMap.keySet().iterator(); i
				.hasNext();) {
			Layer layer = (Layer) i.next();
			Collection features = (Collection) layerToSpecifiedFeaturesMap
					.get(layer);
			if (features.size() != 1) {
				warnUser(FUTURE_StringUtil.substitute(
						ErrorMessages.commitTool_incorrectInputCounts,
						new Object[] {
								features.size() + "",
								layer,
								((Collection) layerToSpecifiedFeaturesMap
										.get(toolboxModel().other(layer)))
										.size()
										+ "", toolboxModel().other(layer) }),
						getContext());
				return;
			}
		}
		final SourceRoadSegment sourceRoadSegmentA = ((SourceFeature) ((Collection) new ArrayList(
				layerToSpecifiedFeaturesMap.values()).get(0)).iterator().next())
				.getRoadSegment();
		final SourceRoadSegment sourceRoadSegmentB = ((SourceFeature) ((Collection) new ArrayList(
				layerToSpecifiedFeaturesMap.values()).get(1)).iterator().next())
				.getRoadSegment();
		final SourceRoadSegment reference = sourceRoadSegmentA == mode
				.reference(sourceRoadSegmentA, sourceRoadSegmentB) ? sourceRoadSegmentA
				: sourceRoadSegmentB;
		final SourceRoadSegment nonReference = reference != sourceRoadSegmentA ? sourceRoadSegmentA
				: sourceRoadSegmentB;
		if (reference != reference.getNetwork().getSession()
				.getPrecedenceRuleEngine().chooseReference(sourceRoadSegmentA,
						sourceRoadSegmentB)
				&& !FUTURE_PreventableConfirmationDialog
						.show(
								getContext().getWorkbench().getFrame(),
								"RoadMatcher",
								FUTURE_StringUtil
										.substitute(
												ErrorMessages.commitTool_overridesPrecedenceRule_statusLineWarning,
												new Object[] {
														reference.getNetwork()
																.getName(),
														nonReference
																.getNetwork()
																.getName() }),
								FUTURE_StringUtil
										.substitute(
												ErrorMessages.commitTool_overridesPrecedenceRule_dialogText,
												new Object[] {
														reference.getNetwork()
																.getName(),
														nonReference
																.getNetwork()
																.getName() }),
								"Match them anyway",
								"Cancel",
								getClass().getName()
										+ " - OVERRIDES PRECEDENCE RULE - DO NOT SHOW AGAIN")) {
			return;
		}
		matchUndoably(reference, nonReference, getClass().getName()
				+ " - MATCH - DO NOT SHOW AGAIN", getContext(), getName());
	}

	public static void matchUndoably(final SourceRoadSegment reference,
			final SourceRoadSegment nonReference, String doNotShowAgainID,
			WorkbenchContext context, String name) {
		if (!warnIfCommitted(reference, nonReference, doNotShowAgainID, context)) {
			return;
		}
		matchUndoably(reference, nonReference, name, ToolboxModel
				.instance(context), context.getErrorHandler());
		new AutoAdjustAfterManualCommitOp().autoAdjustFeatures(
				FUTURE_CollectionUtil.list(reference.getFeature(), nonReference
						.getFeature()), context);
	}

	public static boolean warnIfCommitted(final SourceRoadSegment a,
			final SourceRoadSegment b, String doNotShowAgainID,
			WorkbenchContext context) {
		boolean result = true;
		String statusLineWarning = null;
		String dialogText = null;
		if (a.getState().indicates(SourceState.COMMITTED)
				&& b.getState().indicates(SourceState.COMMITTED)) {
			statusLineWarning = ErrorMessages.commitTool_alreadyCommitted_bothSegments_statusLineWarning;
			dialogText = ErrorMessages.commitTool_alreadyCommitted_bothSegments_dialogText;
		} else if (a.getState().indicates(SourceState.COMMITTED)
				|| b.getState().indicates(SourceState.COMMITTED)) {
			statusLineWarning = ErrorMessages.commitTool_alreadyCommitted_oneSegment_statusLineWarning;
			dialogText = ErrorMessages.commitTool_alreadyCommitted_oneSegment_dialogText;
		}
		if (dialogText != null
				&& !FUTURE_PreventableConfirmationDialog.show(context
						.getWorkbench().getFrame(), "RoadMatcher",
						statusLineWarning, dialogText, "Match them anyway",
						"Cancel", doNotShowAgainID)) {
			result = false;
		}
		return result;
	}

	private void handleFeaturesFromOneLayer(Map layerToSpecifiedFeaturesMap) {
		if (!mode.canSetFeaturesToStandaloneFor(
				(Layer) layerToSpecifiedFeaturesMap.keySet().iterator().next(),
				this)) {
			warnUser(mode.getNoRoadSegmentsWarning(this), getContext());
			return;
		}
		int matchedFeatures = CollectionUtil.select(
				CollectionUtil
						.concatenate(layerToSpecifiedFeaturesMap.values()),
				new Block() {
					public Object yield(Object feature) {
						return Boolean.valueOf(((SourceFeature) feature)
								.getRoadSegment().getState().indicates(
										SourceState.MATCHED));
					}
				}).size();
		String statusLineWarning = null;
		String dialogText = null;
		if (matchedFeatures == 1) {
			statusLineWarning = features(layerToSpecifiedFeaturesMap) == matchedFeatures ? ErrorMessages.commitTool_alreadyPartOfMatch_theSegment_statusLineWarning
					: ErrorMessages.commitTool_alreadyPartOfMatch_oneSegment_statusLineWarning;
			dialogText = features(layerToSpecifiedFeaturesMap) == matchedFeatures ? ErrorMessages.commitTool_alreadyPartOfMatch_theSegment_dialogText
					: ErrorMessages.commitTool_alreadyPartOfMatch_oneSegment_dialogText;
		} else if (matchedFeatures > 1) {
			statusLineWarning = features(layerToSpecifiedFeaturesMap) == matchedFeatures ? ErrorMessages.commitTool_alreadyPartOfMatch_theSegments_statusLineWarning
					: ErrorMessages.commitTool_alreadyPartOfMatch_someSegments_statusLineWarning;
			dialogText = features(layerToSpecifiedFeaturesMap) == matchedFeatures ? ErrorMessages.commitTool_alreadyPartOfMatch_theSegments_dialogText
					: ErrorMessages.commitTool_alreadyPartOfMatch_someSegments_dialogText;
		}
		//Use CommitTool.class rather than #getClass, because this class is
		//subclassed [Jon Aquino 2004-05-11]
		if (dialogText != null
				&& !FUTURE_PreventableConfirmationDialog.show(getContext()
						.getWorkbench().getFrame(), "RoadMatcher",
						statusLineWarning, dialogText,
						"Mark as Standalone anyway", "Cancel", CommitTool.class
								.getName()
								+ " - MARK AS STANDALONE - DO NOT SHOW AGAIN")) {
			return;
		}
		execute(SetUnmatchedStateTool.createUndoableCommand(getName(),
				SourceState.STANDALONE, layerToSpecifiedFeaturesMap,
				toolboxModel(), getPanel().getContext()));
		//AutoAdjust only after we've passed through the thicket of early exits
		//[Jon Aquino 2004-05-11]
		new AutoAdjustAfterManualCommitOp().autoAdjustFeatures(CollectionUtil
				.concatenate(layerToSpecifiedFeaturesMap.values()),
				getContext());
	}

	private int features(Map layerToSpecifiedFeaturesMap) {
		return CollectionUtil.concatenate(layerToSpecifiedFeaturesMap.values())
				.size();
	}

	private Mode mode;

	private String noRoadSegmentsWarning;

	private static void matchUndoably(SourceRoadSegment reference,
			SourceRoadSegment nonReference, String name,
			final ToolboxModel toolboxModel, ErrorHandler errorHandler) {
		final Transaction transaction = new Transaction(toolboxModel,
				errorHandler);
		transaction.severMatch(reference);
		transaction.severMatch(nonReference);
		RoadSegmentMatch match = new RoadSegmentMatch(reference, nonReference);
		transaction.setState(reference, SourceState.MATCHED_REFERENCE, match);
		transaction.setState(nonReference, SourceState.MATCHED_NON_REFERENCE,
				match);
		AbstractPlugIn.execute(new UndoableCommand(name) {
			public void execute() {
				transaction.execute();
			}

			public void unexecute() {
				transaction.unexecute();
			}
		}, new LayerManagerProxy() {
			public LayerManager getLayerManager() {
				return toolboxModel.getSourceLayer(0).getLayerManager();
			}
		});
	}

	public String getName() {
		return "Match or Commit Road Segment";
	}

	public static final Mode BOTH_LAYERS = new Mode() {
		public boolean canSetFeaturesToStandaloneFor(Layer layer,
				CommitTool tool) {
			return true;
		}

		public String getNoRoadSegmentsWarning(CommitTool tool) {
			return ErrorMessages.commitTool_bothLayers_noRoadSegments;
		}

		public SourceRoadSegment reference(SourceRoadSegment a,
				SourceRoadSegment b) {
			return a.getNetwork().getSession().getPrecedenceRuleEngine()
					.chooseReference(a, b);
		}
	};

	public static final Mode SOURCE_LAYER_0 = new OneLayerMode() {
		protected Layer layer(CommitTool tool) {
			return tool.toolboxModel().getSourceLayer(0);
		}

		protected int getReferenceNetworkID() {
			return 0;
		}
	};

	public static final Mode SOURCE_LAYER_1 = new OneLayerMode() {
		protected Layer layer(CommitTool tool) {
			return tool.toolboxModel().getSourceLayer(1);
		}

		protected int getReferenceNetworkID() {
			return 1;
		}
	};
}