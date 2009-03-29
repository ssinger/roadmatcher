package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.vividsolutions.jump.task.TaskMonitor;

public interface StateTransitionImpactAssessment extends Serializable {
	public Set affectedRoadSegments(Collection segments, TaskMonitor monitor);
}