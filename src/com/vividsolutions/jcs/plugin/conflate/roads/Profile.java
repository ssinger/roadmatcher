package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jcs.conflate.roads.match.RoadMatchOptions;
import com.vividsolutions.jcs.conflate.roads.model.ReferenceDatasetPrecedenceRuleEngine;
import com.vividsolutions.jcs.conflate.roads.model.PrecedenceRuleEngine;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.jump.FUTURE_XML2Java;
import com.vividsolutions.jump.util.StringUtil;

public class Profile {
	private RoadMatchOptions roadMatchOptions = new RoadMatchOptions();

	private AutoAdjustOptions autoAdjustOptions;

	private boolean transferringVertices;

	private Class vertexTransferOpClass;

	public boolean isTransferringVertices() {
		return transferringVertices;
	}

	public void setTransferringVertices(boolean transferringVertices) {
		this.transferringVertices = transferringVertices;
	}

	public Class getVertexTransferOpClass() {
		return vertexTransferOpClass;
	}

	public void setVertexTransferOpClass(Class vertexTransferOpClass) {
		this.vertexTransferOpClass = vertexTransferOpClass;
	}

	private List datasets = new ArrayList();

	public List getDatasets() {
		return Collections.unmodifiableList(datasets);
	}

	public void addDataset(Dataset dataset) {
		datasets.add(dataset);
	}

	public static class Dataset {
		private String shortName;

		private String resultAttributes;

		private String adjustmentConstraint;

		private boolean transferringVertices;

		public String getAdjustmentConstraint() {
			return adjustmentConstraint;
		}

		public void setAdjustmentConstraint(String adjustmentConstraint) {
			validateAdjustmentConstraint(adjustmentConstraint);
			this.adjustmentConstraint = adjustmentConstraint;
		}

		private void validateAdjustmentConstraint(String adjustmentConstraint) {
			if (!ADJUSTMENT_CONSTRAINTS.contains(adjustmentConstraint)) {
				throw new RuntimeException(
						FUTURE_StringUtil
								.substitute(
										ErrorMessages.profile_unrecognizedAdjustmentConstraint,
										new Object[] {
												adjustmentConstraint,
												StringUtil
														.toCommaDelimitedString(ADJUSTMENT_CONSTRAINTS) }));
			}
		}

		public String getShortName() {
			return shortName;
		}

		public void setShortName(String shortName) {
			this.shortName = shortName;
		}

		public String getResultAttributes() {
			return resultAttributes;
		}

		public void setResultAttributes(String resultAttributes) {
			this.resultAttributes = resultAttributes;
		}

		public boolean isTransferringVertices() {
			return transferringVertices;
		}

		public void setTransferringVertices(boolean transferringVertices) {
			this.transferringVertices = transferringVertices;
		}
	}

	private List issueLogDescriptions = new ArrayList();

	private List segmentComments = new ArrayList();

	private boolean segmentCommentsEditable;

	private boolean locked;

	private PrecedenceRuleEngine precedenceRuleEngine;

	public static final String NO_ADJUSTMENT_CONSTRAINT = "none";

	/**
	 * For use by Java2XML. Does not call #addDataset.
	 *  
	 */
	public Profile() {
	}

	public Profile(String shortName0, String shortName1) {
		this(shortName0, shortName1, NO_ADJUSTMENT_CONSTRAINT,
				NO_ADJUSTMENT_CONSTRAINT, new RoadMatchOptions(),
				new AutoAdjustOptions(),
				new ReferenceDatasetPrecedenceRuleEngine(),
				new ResultOptions(), false, new ArrayList(), new ArrayList(),
				true, "");
	}

	public Profile(String shortName0, String shortName1,
			String adjustmentConstraint0, String adjustmentConstraint1,
			RoadMatchOptions roadMatchOptions,
			AutoAdjustOptions autoAdjustOptions,
			PrecedenceRuleEngine precedenceRuleEngine,
			ResultOptions resultOptions, boolean locked,
			List issueLogDescriptions, List segmentComments,
			boolean segmentCommentsEditable, String onSessionLoadScript) {
		this.issueLogDescriptions = issueLogDescriptions;
		this.segmentComments = segmentComments;
		this.segmentCommentsEditable = segmentCommentsEditable;
		this.onSessionLoadScript = onSessionLoadScript;
		addDataset(new Dataset());
		addDataset(new Dataset());
		dataset(0).setShortName(shortName0);
		dataset(1).setShortName(shortName1);
		dataset(0).setAdjustmentConstraint(adjustmentConstraint0);
		dataset(1).setAdjustmentConstraint(adjustmentConstraint1);
		this.roadMatchOptions = roadMatchOptions;
		this.autoAdjustOptions = autoAdjustOptions;
		setPrecedenceRuleEngine(precedenceRuleEngine);
		setResultOptions(resultOptions);
		setLocked(locked);
	}

	public static final String PREVENT_ADJUSTMENT_CONSTRAINT = "prevent";

	public static final String WARNING_ADJUSTMENT_CONSTRAINT = "warn";

	public PrecedenceRuleEngine getPrecedenceRuleEngine() {
		return precedenceRuleEngine;
	}

	public void setPrecedenceRuleEngine(
			PrecedenceRuleEngine precedenceRuleEngine) {
		this.precedenceRuleEngine = precedenceRuleEngine;
	}

	public double getMinimumLineSegmentLength() {
		return roadMatchOptions.getEdgeMatchOptions()
				.getLineSegmentLengthTolerance();
	}

	public void setMinimumLineSegmentLength(double minimumLineSegmentLength) {
		roadMatchOptions.getEdgeMatchOptions().setLineSegmentLengthTolerance(
				minimumLineSegmentLength);
	}

	public double getNearnessTolerance() {
		return roadMatchOptions.getEdgeMatchOptions().getNearnessTolerance();
	}

	public void setNearnessTolerance(double nearnessTolerance) {
		roadMatchOptions.getEdgeMatchOptions().setNearnessTolerance(
				nearnessTolerance);
	}

	public boolean isFindStandaloneRoadsEnabled() {
		return roadMatchOptions.isStandaloneEnabled();
	}

	public void setFindStandaloneRoadsEnabled(boolean findStandaloneRoadsEnabled) {
		roadMatchOptions.setStandaloneEnabled(findStandaloneRoadsEnabled);
	}

	public double getFindStandaloneRoadsMinimumDistance() {
		return roadMatchOptions.getStandaloneOptions().getDistanceTolerance();
	}

	public void setFindStandaloneRoadsMinimumDistance(
			double findStandaloneRoadsMinimumDistance) {
		roadMatchOptions.getStandaloneOptions().setDistanceTolerance(
				findStandaloneRoadsMinimumDistance);
	}

	public boolean isFindMatchedRoadsEnabled() {
		return roadMatchOptions.isEdgeMatchEnabled();
	}

	public void setFindMatchedRoadsEnabled(boolean findMatchedRoadsEnabled) {
		roadMatchOptions.setEdgeMatchEnabled(findMatchedRoadsEnabled);
	}

	public double getFindMatchedRoadsMaximumDistance() {
		return roadMatchOptions.getEdgeMatchOptions().getDistanceTolerance();
	}

	public void setFindMatchedRoadsMaximumDistance(
			double findMatchedRoadsMaximumDistance) {
		roadMatchOptions.getEdgeMatchOptions().setDistanceTolerance(
				findMatchedRoadsMaximumDistance);
	}

	public RoadMatchOptions getRoadMatchOptions() {
		return roadMatchOptions;
	}

	public static Profile createProfile(File profileFile) throws Exception {
		return (Profile) new FUTURE_XML2Java(Profile.class.getClassLoader())
				.read(profileFile, Profile.class);
	}

	public Dataset dataset(int i) {
		return (Dataset) getDatasets().get(i);
	}

	private static final Collection ADJUSTMENT_CONSTRAINTS = Arrays
			.asList(new String[] { NO_ADJUSTMENT_CONSTRAINT,
					PREVENT_ADJUSTMENT_CONSTRAINT,
					WARNING_ADJUSTMENT_CONSTRAINT });

	public AutoAdjustOptions getAutoAdjustOptions() {
		return autoAdjustOptions;
	}

	public void setAutoAdjustOptions(AutoAdjustOptions autoAdjustOptions) {
		this.autoAdjustOptions = autoAdjustOptions;
	}

	public ResultOptions getResultOptions() {
		ResultOptions resultOptions = new ResultOptions();
		resultOptions.setDataset0AttributesToInclude(dataset(0)
				.getResultAttributes());
		resultOptions.setDataset1AttributesToInclude(dataset(1)
				.getResultAttributes());
		resultOptions
				.setVertexTransferProperties(new VertexTransferProperties());
		resultOptions.getVertexTransferProperties().setTransferringVertices(
				isTransferringVertices());
		resultOptions.getVertexTransferProperties().setVertexTransferOpClass(
				getVertexTransferOpClass());
		resultOptions.getVertexTransferProperties()
				.setTransferringVerticesFrom0To1(
						dataset(0).isTransferringVertices());
		resultOptions.getVertexTransferProperties()
				.setTransferringVerticesFrom1To0(
						dataset(1).isTransferringVertices());
		return resultOptions;
	}

	public void setResultOptions(ResultOptions resultOptions) {
		dataset(0).setResultAttributes(
				resultOptions.getDataset0AttributesToInclude());
		dataset(1).setResultAttributes(
				resultOptions.getDataset1AttributesToInclude());
		setTransferringVertices(resultOptions.getVertexTransferProperties()
				.isTransferringVertices());
		setVertexTransferOpClass(resultOptions.getVertexTransferProperties()
				.getVertexTransferOpClass());
		dataset(0).setTransferringVertices(
				resultOptions.getVertexTransferProperties()
						.isTransferringVerticesFrom0To1());
		dataset(1).setTransferringVertices(
				resultOptions.getVertexTransferProperties()
						.isTransferringVerticesFrom1To0());
	}

	public List getIssueLogDescriptions() {
		return Collections.unmodifiableList(issueLogDescriptions);
	}

	public boolean getSegmentCommentsEditable() {
		return segmentCommentsEditable;
	}

	public List getSegmentComments() {
		return Collections.unmodifiableList(segmentComments);
	}

	public void addIssueLogDescription(String issueLogDescription) {
		issueLogDescriptions.add(issueLogDescription);
	}

	public void setSegmentCommentsEditable(boolean segmentCommentsEditable) {
		this.segmentCommentsEditable = segmentCommentsEditable;
	}

	public void addSegmentComment(String segmentComment) {
		segmentComments.add(segmentComment);
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	private String onSessionLoadScript;

	public String getOnSessionLoadScript() {
		return onSessionLoadScript;
	}

	public void setOnSessionLoadScript(String onSessionLoadScript) {
		this.onSessionLoadScript = onSessionLoadScript;
	}
}