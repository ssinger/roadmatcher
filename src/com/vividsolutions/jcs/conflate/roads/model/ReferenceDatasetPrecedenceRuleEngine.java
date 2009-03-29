package com.vividsolutions.jcs.conflate.roads.model;

import com.vividsolutions.jts.util.Assert;

public class ReferenceDatasetPrecedenceRuleEngine implements
		PrecedenceRuleEngine {
	public ReferenceDatasetPrecedenceRuleEngine() {
	}



	public SourceRoadSegment chooseReference(SourceRoadSegment a,
			SourceRoadSegment b) {
		if (a.getNetwork().getName().equals(referenceDatasetName)) return a;
		if (b.getNetwork().getName().equals(referenceDatasetName)) return b;
		Assert.shouldNeverReachHere();
		return null;
	}

	private String referenceDatasetName;

	public String getReferenceDatasetName() {		
		return referenceDatasetName;
	}

	public ReferenceDatasetPrecedenceRuleEngine setReferenceDatasetName(
			String referenceDatasetName) {
		this.referenceDatasetName = referenceDatasetName;
		return this;
	}
}