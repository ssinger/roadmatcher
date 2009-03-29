package com.vividsolutions.jcs.plugin.conflate.roads;

public class ErrorMessages {
	public static final String adjustedMatchAutoAdjuster_unknownRoadSegments = "One or more problem nodes have road segments in Unknown state";

	public static final String adjustEndpointOperation_shortLineSegment_dialogText = "The new road segment has a line segment shorter ($1) than the minimum defined in the AutoMatch options ($2).";

	public static final String adjustEndpointOperation_shortLineSegment_statusLineWarning = "Warning: line-segment length < min length ($1 < $2)";
	
	public static final String revertAllOp_segmentsDiscontiguous_statusLineWarning = "Warning: Split segments discontiguous - see View > Log for details";
	
	public static final String revertAllOp_segmentsDiscontiguous_dialogText = "Discontiguous split segments were detected. See View > Log for details.";

	public static final String adjustEndpointOperation_zeroLength = "Length = 0. Cancelled.";

	public static final String revertToOriginalSegmentsTool_noUnknownSegments = "No unknown segments here";

	public static final String autoConnectEndpointTool_noRoadSegments = "No inconsistent nodes here";

	public static final String revertToOriginalSegmentsTool_splitSegmentsNotUnknown = "Some segments not in unknown state";

	public static final String autoAdjustAfterManualCommitOp_unsupportedConsistencyRule = "Unsupported consistency rule: $1. Only the Adjusted Match Consistency rule is supported.";

	public static final String createIntersectionSplitNodeTool_noIntersections = "No intersections here";

	public static final String commitTool_alreadyCommitted_bothSegments_dialogText = "The road segments you are matching have already been committed.";

	public static final String moveSplitNodeTool_noEndpoints = "No unadjusted split nodes here";

	public static final String moveSplitNodeTool_adjusted = "Can't move split node on adjusted road segment";

	public static final String rightClickSegmentPlugIn_noSegmentHere = "No segment here";

	public static final String commitTool_alreadyCommitted_bothSegments_statusLineWarning = "Warning: The road segments you matched had already been committed.";

	public static final String importSourcePackagePlugIn_noManifest = "manifest.xml missing from source package";

	public static final String revertAllOp_noKnownSegmentsOrSplitNodes = "No non-Unknown segments or split nodes in fence";

	public static final String constraintChecker_adjustmentConstraintError = "$1 segments cannot be adjusted";

	public static final String constraintChecker_nodeConstraintError = "Adjustment violates node constraint";

	public static final String constraintChecker_adjustmentConstraintWarning = "Warning: a $1 segment was adjusted";

	public static final String autoAdjustOp_adjustmentConstraintsViolation = "Adjustment Constraints on $1 prevented AutoAdjustments from being performed ";

	public static final String commitTool_alreadyCommitted_oneSegment_dialogText = "One of the road segments you are matching has already been committed.";

	public static final String matchSelectedSegmentsPlugIn_wrongCounts = "1 $1 segment and 1 $2 segment must be selected (currently $3 $1, $4 $2)";

	public static final String commitTool_alreadyCommitted_oneSegment_statusLineWarning = "Warning: One of the road segments you matched had already been committed.";

	public static final String sourcePackageManifest_unrecognizedShortName = "Unrecognized short name: $1";

	public static final String savePackageAsPlugIn_noIncludedRoadSegments = "At least 1 road segment must be included in the result (i.e. the Included state)";

	public static final String restorePostponedInconsistenciesPlugIn_noInconsistencies = "No postponed inconsistencies to restore";

	public static final String commitTool_alreadyPartOfMatch_oneSegment_dialogText = "One road segment you are marking as Standalone is part of a match.";

	public static final String commitTool_alreadyPartOfMatch_oneSegment_statusLineWarning = "Warning: One road segment you marked as Standalone had been part of a match.";

	public static final String commitTool_alreadyPartOfMatch_someSegments_dialogText = "Some road segments you are marking as Standalone are each part of a match.";

	public static final String newSessionPlugIn_javaVersionOld = "RoadMatcher requires Java 1.4 (currently Java $1 is running)";

	public static final String sourcePackageManifest_unrecognizedFormat = "Unrecognized format: $1. Allowed formats are: $2";

	public static final String commitTool_alreadyPartOfMatch_someSegments_statusLineWarning = "Warning: Some road segments you marked as Standalone had each been part of a match.";

	public static final String commitTool_alreadyPartOfMatch_theSegment_dialogText = "The road segment you are marking as Standalone is part of a match.";

	public static final String commitTool_alreadyPartOfMatch_theSegment_statusLineWarning = "Warning: The road segment you marked as Standalone had been part of a match.";

	public static final String commitTool_alreadyPartOfMatch_theSegments_dialogText = "The road segments you are marking as Standalone are each part of a match.";

	public static final String commitTool_alreadyPartOfMatch_theSegments_statusLineWarning = "Warning: The road segments you marked as Standalone had each been part of a match.";

	public static final String commitTool_bothLayers_noRoadSegments = "No source road segments here";

	public static final String commitTool_incorrectInputCounts = "$1 $2 and $3 $4 features specified. To make a match, specify only 1 feature from each.";

	public static final String commitTool_oneLayer_noRoadSegments = "No $1 road segments here";

	public static final String createSplitNodeTool_adjusted = "Can't create split node on adjusted road segment";

	public static final String definePathsTool_pathRouteCannotBeDetermined = "\"$1\" path route cannot be determined";

	public static final String definePathsTool_nonUnknownRoadSegments = "The paths must contain only Unknown road segments";

	public static final String deleteSplitNodeTool_adjusted = "Can't delete split node from adjusted road segment";

	public static final String deleteSplitNodeTool_noRoadSegments = "No $1split nodes here";

	public static final String exportResultPlugIn_unknownOrInconsistent = "Warning: $1 Unknown/Inconsistent road segments";

	public static final String findClosestRoadSegmentPlugIn_noRoadSegments = "No road segments match the given criteria";

	public static final String queryToolboxPanel_taskFrameMustBeActive = "A task frame must be active";

	public static final String commitTool_overridesPrecedenceRule_statusLineWarning = "Warning: This action made $1 the reference, whereas the precedence rules would have made $2 the reference.";

	public static final String commitTool_overridesPrecedenceRule_dialogText = "This action will make $1 the reference, whereas the precedence rules would make $2 the reference.";

	public static final String matchPathsPlugIn_nonUnknownRoadSegments = "The two input paths must contain only Unknown road segments";

	public static final String queryToolboxPanel_taskFrameWithConflationSessionMustBeActive = "A task frame with a conflation session must be active";

	public static final String matchPathsPlugIn_noPathsDefined = "A path must be defined for each source network using the $1 Tool";

	public static final String matchPathsPlugIn_pathRoadSegmentsNotInNetwork = "Path is invalid because road segments have changed. Please clear the path and define it again.";

	public static final String newSessionPanel_noNetworkAShortName = "The first short name must be specified";

	public static final String newSessionPanel_noNetworkBShortName = "The second short name must be specified";

	public static final String newSessionPanel_sameLayers = "The two layers must be different";

	public static final String newSessionPanel_sameShortNames = "The two short-names must be different";

	public static final String newSessionPlugIn_existingSession_dialogText = "A session already exists. Creating a new session will erase all the work you have done in the current session.";

	public static final String newSessionPlugIn_nonConflationLayerCount = "At least 2 non-conflation layers must exist";

	public static final String newSessionPlugIn_validationErrors = "One or both of the source networks contains validation errors - please review before continuing.";

	public static final String profile_unrecognizedAdjustmentConstraint = "Unrecognized adjustment constraint: $1. Valid values are: $2";

	public static final String oneDragAdjustEndpointTool_noEndpoints = "No endpoints here";

	public static final String pathMatchTool_endpointsTooFarApart = "Endpoints too far apart (required = $1, actual = $2)";

	public static final String pathMatchTool_invalidInput = "Please specify two Unknown road segments, one from each network";

	public static final String extendOrClipTool_adjusted = "Couldn't create split node on other road segment because it has been adjusted";

	public static final String pathMatchTool_noRoadSegments = "No Unknown $1road segments here";

	public static final String pathMatchTool_pathMatchBuilder_noMatch = "Unable to determine path (PathMatchBuilder)";

	public static final String pathMatchTool_pathZipper_invalid = "Unable to determine path (RoadPathZipper)";

	public static final String preciseMatchTool_waitingForFirst_noFeatures = "No $1 or $2 features here";

	public static final String preciseMatchTool_waitingForSecond_noFeatures = "No $1 feature here";

	public static final String retireRoadSegmentTool_alreadyRetired = "Already retired";

	public static final String retireRoadSegmentTool_committed_dialogText = "Some features you are retiring have been committed.";

	public static final String retireRoadSegmentTool_committed_statusLineWarning = "Warning: Some features you retired had been committed";

	public static final String revertRoadSegmentTool_alreadyUnknown = "Already in the Unknown state";

	public static final String setUnmatchedStateTool_noRoadSegments = "No $1road segments here";

	public static final String specifyNClosestRoadFeaturesTool_noRoadSegments = "No $1road segments here";

	public static final String newSessionPlugIn_coincidentSegments = "Coincident segments were found in the source networks";

	public static final String newSessionPlugIn_illegalGeometries = "Illegal geometry types were found in the source networks";

	public static final String newSessionPlugIn_unmatchedNodeConstraints = "Unmatched nodes were found in the node constraint datasets  ";

	public static final String specifyNClosestRoadFeaturesTool_tooFewRoadSegments = "Fewer than $1 $2road segments here";

	public static final String specifyRoadFeaturesTool_noConflationSession = "A conflation session must be created or opened";

	public static final String postponeInconsistencyHandler_noInconsistentNodes = "No inconsistent nodes here";

	public static final String postponeInconsistencyHandler_noPostponedInconsistencies = "No postponed inconsistencies here";

	public static final String generateResultLayerPlugIn_nodeConstraintsViolated = "$1 node constraint(s) violated -- see Output Window for details";
}