package com.vividsolutions.jcs.conflate.roads;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.match.RoadMatchOptions;
import com.vividsolutions.jcs.conflate.roads.match.RoadMatcherProcess;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency.SourceMatchConsistencyRule;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;

/**
 * The overall container class that represents the entire process of merging two
 * RoadNetworks.
 */
public class ConflationSession implements Serializable {

	public static final String DEFAULT_NAME = "New Session";

	/**
	 * Returns any coincident segments in the input data. If there are no
	 * coincident segments in a source dataset, the corresponding feature
	 * collection will be empty.
	 * 
	 * @return an array of 2 {@link FeatureCollection}s containing any
	 *         coincident segments.
	 */
	private static FeatureCollection[] getIllegalGeometries(
			FeatureCollection[] inputFC) {
		FeatureCollection[] illegalGeometryFC = new FeatureCollection[2];
		illegalGeometryFC[0] = getIllegalGeometries(inputFC[0]);
		illegalGeometryFC[1] = getIllegalGeometries(inputFC[1]);
		return illegalGeometryFC;
	}

	private boolean automatedProcessRunning = false;

	/**
	 * Checks for valid edge geometry. Edge geometry must be simple LineStrings.
	 * If there are no illegal geometries the feature collection will be empty.
	 * 
	 * @return a {@link FeatureCollection}containing any edges with illegal
	 *         geometry
	 */
	private static FeatureCollection getIllegalGeometries(
			FeatureCollection inputFC) {
		FeatureCollection fc = new FeatureDataset(inputFC.getFeatureSchema());
		for (Iterator i = inputFC.iterator(); i.hasNext();) {
			Feature f = (Feature) i.next();
			Geometry geom = f.getGeometry();
			boolean isValid = geom instanceof LineString && geom.isSimple();
			if (!isValid)
				fc.add(f);
		}
		return fc;
	}

	/** Transient because the UI stuff is not Serializable */
	private transient RoadsEventFirer roadsEventFirer;

	private boolean autoMatched = false;

	private String name;

	private Statistics statistics = new Statistics(this);

	private RoadMatchOptions matchOptions = new RoadMatchOptions();

	private ConsistencyRule consistencyRule = new SourceMatchConsistencyRule();

	private FeatureCollection[] originalFeatureCollections;

	private FeatureCollection[] coincidentSegmentFeatureCollections;

	private FeatureCollection[] illegalGeometryFC;

	private PrecedenceRuleEngine precedenceRuleEngine;

	private boolean[] warningAboutAdjustments = new boolean[] { false, false };

	private FeatureCollection[] contextFeatureCollections;

	private FeatureCollection[] unmatchedNodeConstraints;

	public boolean isWarningAboutAdjustments(int i) {
		//TODO: Combine #warningAboutAdjustments with
		//RoadNetwork#editable into a single field: #adjustmentConstraint.
		//Easiest is to implement it as a String -- if instead we go with the
		//enum pattern, remember to implement #readResolve.
		//[Jon Aquino 2004-04-30]
		return warningAboutAdjustments[i];
	}

	public void setWarningAboutAdjustments(int i,
			boolean warningAboutAdjustments) {
		this.warningAboutAdjustments[i] = warningAboutAdjustments;
	}

	/**
	 * This is not dead code! #readObject is a special serialization method.
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		getRoadsEventFirer().addListener(statistics);
	}

	public ConflationSession(FeatureCollection originalFeatureCollection0,
			FeatureCollection originalFeatureCollection1) {
		this(DEFAULT_NAME, originalFeatureCollection0,
				originalFeatureCollection1, AddNewLayerPlugIn
						.createBlankFeatureCollection(), AddNewLayerPlugIn
						.createBlankFeatureCollection(), AddNewLayerPlugIn
						.createBlankFeatureCollection(), AddNewLayerPlugIn
						.createBlankFeatureCollection());
	}

	private static SpatialIndex index(Collection nodeConstraintCoordinates) {
		STRtree index = new STRtree();
		for (Iterator i = nodeConstraintCoordinates.iterator(); i.hasNext();) {
			Coordinate c = (Coordinate) i.next();
			index.insert(new Envelope(c), c);
		}
		index.build();
		return index;
	}

	public ConflationSession(String name,
			FeatureCollection originalFeatureCollection0,
			FeatureCollection originalFeatureCollection1,
			FeatureCollection contextFeatureCollection0,
			FeatureCollection contextFeatureCollection1,
			FeatureCollection nodeConstraints0,
			FeatureCollection nodeConstraints1) {
		setName(name);
		getRoadsEventFirer().addListener(statistics);
		originalFeatureCollections = new FeatureCollection[] {
				originalFeatureCollection0, originalFeatureCollection1 };
		contextFeatureCollections = new FeatureCollection[] {
				contextFeatureCollection0, contextFeatureCollection1 };
		// do validations on input features
		illegalGeometryFC = getIllegalGeometries(originalFeatureCollections);
		FeatureCollection hideConflationAttributesFeatureCollection0 = new HideConflationAttributesFeatureCollectionWrapper(
				originalFeatureCollection0);
		FeatureCollection hideConflationAttributesFeatureCollection1 = new HideConflationAttributesFeatureCollectionWrapper(
				originalFeatureCollection1);
		sourceNetworks[0] = new RoadNetwork(SourceFeature
				.createSchema(hideConflationAttributesFeatureCollection0
						.getFeatureSchema()), this);
		sourceNetworks[1] = new RoadNetwork(SourceFeature
				.createSchema(hideConflationAttributesFeatureCollection1
						.getFeatureSchema()), this);
		//Ensure sourceNetworks field is populated before adding to the
		//networks, because RoadNetwork#add leads to a call to
		//ConflationSession#getSourceNetworkN [Jon Aquino 12/1/2003]
		initializeSourceNetwork(sourceNetworks[0],
				hideConflationAttributesFeatureCollection0);
		initializeSourceNetwork(sourceNetworks[1],
				hideConflationAttributesFeatureCollection1);
		unmatchedNodeConstraints = new FeatureCollection[] {
				assignNodeConstraints(nodeConstraints0, getSourceNetwork(0)),
				assignNodeConstraints(nodeConstraints1, getSourceNetwork(1)) };
	}

	private static FeatureCollection assignNodeConstraints(
			FeatureCollection nodeConstraints, RoadNetwork network) {
		Collection unassignedNodeConstraints = new ArrayList();
		Set nodeConstraintCoordinates = new HashSet();
		for (Iterator i = nodeConstraints.iterator(); i.hasNext();) {
			Feature feature = (Feature) i.next();
			//Allow multipoints as workaround for ShapefileWriter bug: it seems
			//to save points as multipoints. [Jon Aquino 2004-06-01]
			if (!(feature.getGeometry() instanceof Point)
					&& !(feature.getGeometry() instanceof MultiPoint)) {
				unassignedNodeConstraints.add(feature.getGeometry());
				continue;
			}
			nodeConstraintCoordinates.addAll(Arrays.asList(feature
					.getGeometry().getCoordinates()));
		}
		unassignedNodeConstraints.addAll(assignNodeConstraints(
				nodeConstraintCoordinates, network));
		return toFeatureCollection(unassignedNodeConstraints);
	}

	private static FeatureCollection toFeatureCollection(Collection geometries) {
		FeatureCollection featureCollection = AddNewLayerPlugIn
				.createBlankFeatureCollection();
		for (Iterator i = geometries.iterator(); i.hasNext();) {
			Geometry geometry = (Geometry) i.next();
			Feature feature = new BasicFeature(featureCollection
					.getFeatureSchema());
			feature.setGeometry(geometry);
			featureCollection.add(feature);
		}
		return featureCollection;
	}

	private static Collection assignNodeConstraints(
			Set nodeConstraintCoordinates, RoadNetwork network) {
		Set unassignedNodeConstraints = new HashSet(nodeConstraintCoordinates);
		for (Iterator i = network.getGraph().getEdges().iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			roadSegment.setStartNodeConstrained(nodeConstraintCoordinates
					.contains(roadSegment.getApparentStartCoordinate()));
			roadSegment.setEndNodeConstrained(nodeConstraintCoordinates
					.contains(roadSegment.getApparentEndCoordinate()));
			unassignedNodeConstraints.remove(roadSegment
					.getApparentStartCoordinate());
			unassignedNodeConstraints.remove(roadSegment
					.getApparentEndCoordinate());
		}
		return CollectionUtil.collect(unassignedNodeConstraints, new Block() {
			public Object yield(Object coordinate) {
				return geometryFactory.createPoint((Coordinate) coordinate);
			}
		});
	}

	private static GeometryFactory geometryFactory = new GeometryFactory();

	/**
	 * Returns any coincident segments in the input data. If there are no
	 * coincident segments in a source dataset, the corresponding feature
	 * collection will be empty.
	 * 
	 * @return an array of 2 {@link FeatureCollection}s containing any
	 *         coincident segments.
	 */
	public FeatureCollection[] getCoincidentSegments() {
		if (coincidentSegmentFeatureCollections == null) {
			coincidentSegmentFeatureCollections = new FeatureCollection[2];
			coincidentSegmentFeatureCollections[0] = sourceNetworks[0]
					.checkCoincidentEdges();
			coincidentSegmentFeatureCollections[1] = sourceNetworks[1]
					.checkCoincidentEdges();
		}
		return coincidentSegmentFeatureCollections;
	}

	/**
	 * Returns any coincident segments in the input data. If there are no
	 * coincident segments in a source dataset, the corresponding feature
	 * collection will be empty.
	 * 
	 * @return an array of 2 {@link FeatureCollection}s containing any
	 *         coincident segments.
	 */
	public FeatureCollection[] getIllegalGeometries() {
		return illegalGeometryFC;
	}

	public RoadMatchOptions getMatchOptions() {
		return matchOptions;
	}

	public RoadMatcherProcess autoMatch(final TaskMonitor monitor) {
		//Assert.isTrue(!autoMatched);
		// MD - for now, run automatching right away. Probably eventually needs
		// to be done under user control
		final RoadMatcherProcess rm = new RoadMatcherProcess(
				getSourceNetwork(0), getSourceNetwork(1));
		doAutomatedProcess(new Block() {
			public Object yield() {
				rm.match(matchOptions, monitor);
				return null;
			}
		});
		autoMatched = true;
		return rm;
	}

	public void updateResultStates(final TaskMonitor monitor) {
		Block block = new Block() {
			public Object yield() {
				int totalRoadSegments = getSourceNetwork(0).getGraph()
						.getEdges().size()
						+ getSourceNetwork(1).getGraph().getEdges().size();
				int[] roadSegmentsUpdated = new int[] { 0 };
				updateResultStates(getSourceNetwork(0), monitor,
						totalRoadSegments, roadSegmentsUpdated);
				updateResultStates(getSourceNetwork(1), monitor,
						totalRoadSegments, roadSegmentsUpdated);
				return null;
			}
		};
		if (getConsistencyRule() instanceof Optimizable) {
			((Optimizable) getConsistencyRule()).doOptimizedOp(block);
		} else {
			block.yield();
		}
	}

	public void doAutomatedProcess(Block process) {
		boolean automatedProcessRunningOriginally = automatedProcessRunning;
		automatedProcessRunning = true;
		try {
			process.yield();
		} finally {
			automatedProcessRunning = automatedProcessRunningOriginally;
		}
	}

	private void updateResultStates(RoadNetwork network, TaskMonitor monitor,
			int totalRoadSegments, int[] roadSegmentsUpdated) {
		monitor.report("Updating result states");
		for (Iterator i = network.getGraph().getEdges().iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			roadSegment.setResultState(ResultStateRules.instance()
					.determineResultState(roadSegment));
			monitor.report(++roadSegmentsUpdated[0], totalRoadSegments,
					"road segments");
		}
	}

	private void initializeSourceNetwork(RoadNetwork sourceNetwork,
			FeatureCollection originalFeatureCollection) {
		for (Iterator i = originalFeatureCollection.iterator(); i.hasNext();) {
			Feature originalFeature = (Feature) i.next();
			Geometry geom = (Geometry) originalFeature.getGeometry().clone();
			if (geom instanceof LineString) {
				sourceNetwork.add(new SourceRoadSegment((LineString) geom,
						originalFeature, sourceNetwork));
			}
		}
	}

	public RoadNetwork getSourceNetwork(int index) {
		return sourceNetworks[index];
	}

	public RoadNetwork getSourceNetwork(String name) {
		if (sourceNetworks[0].getName().equals(name)) {
			return sourceNetworks[0];
		}
		if (sourceNetworks[1].getName().equals(name)) {
			return sourceNetworks[1];
		}
		Assert.shouldNeverReachHere();
		return null;
	}

	public FeatureCollection getOriginalFeatureCollection(int index) {
		return originalFeatureCollections[index];
	}

	public FeatureCollection getContextFeatureCollection(int index) {
		return contextFeatureCollections[index];
	}

	private RoadNetwork[] sourceNetworks = new RoadNetwork[2];

	public Statistics getStatistics() {
		return statistics;
	}

	public ConsistencyRule getConsistencyRule() {
		return consistencyRule;
	}

	private Blackboard blackboard = new Blackboard();

	private File file;

	public Blackboard getBlackboard() {
		return blackboard;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public ConflationSession setConsistencyRule(ConsistencyRule consistencyRule) {
		this.consistencyRule = consistencyRule;
		return this;
	}

	public boolean isAutoMatched() {
		return autoMatched;
	}

	public RoadsEventFirer getRoadsEventFirer() {
		if (roadsEventFirer == null) {
			//Get here after deserialization [Jon Aquino 2004-01-29]
			roadsEventFirer = new RoadsEventFirer();
		}
		return roadsEventFirer;
	}

	public void setMatchOptions(RoadMatchOptions matchOptions) {
		this.matchOptions = matchOptions;
	}

	public PrecedenceRuleEngine getPrecedenceRuleEngine() {
		return precedenceRuleEngine;
	}

	public void setPrecedenceRuleEngine(
			PrecedenceRuleEngine precedenceRuleEngine) {
		this.precedenceRuleEngine = precedenceRuleEngine;
	}

	private boolean locked = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection getRoadSegments() {
		return FUTURE_CollectionUtil.concatenate(getSourceNetwork(0).getGraph()
				.getEdges(), getSourceNetwork(1).getGraph().getEdges());
	}

	public boolean isAutomatedProcessRunning() {
		return automatedProcessRunning;
	}

	public FeatureCollection[] getUnmatchedNodeConstraints() {
		return unmatchedNodeConstraints;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}
}