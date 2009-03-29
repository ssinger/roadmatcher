package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import java.util.*;

/**
 * Handles firing of FeatureEvents and undo.
 */
public class Transaction {
	private ErrorHandler errorHandler;

	private LayerManager layerManager;

	private CollectionMap networkToAddedRoadSegmentsMap = new CollectionMap(
			HashMap.class, HashSet.class);

	private CollectionMap networkToModifiedRoadSegmentsMap = new CollectionMap(
			HashMap.class, HashSet.class);

	private CollectionMap networkToRemovedRoadSegmentsMap = new CollectionMap(
			HashMap.class, HashSet.class);

	private List operations = new ArrayList();

	private ToolboxModel toolboxModel;

	private TaskMonitor monitor;

	public Transaction(ToolboxModel toolboxModel, ErrorHandler errorHandler) {
		this(toolboxModel, errorHandler, new DummyTaskMonitor());
	}

	public Transaction(ToolboxModel toolboxModel, ErrorHandler errorHandler,
			TaskMonitor monitor) {
		this.monitor = monitor;
		this.errorHandler = errorHandler;
		this.toolboxModel = toolboxModel;
		this.layerManager = toolboxModel.getSourceLayer(0).getLayerManager();
	}

	private boolean modifyingNetworks = true;

	public Transaction add(final SourceRoadSegment roadSegment) {
		operations.add(new Operation() {
			public void execute() {
				if (modifyingNetworks) {
					roadSegment.getNetwork().add(roadSegment);
				}
				networkToAddedRoadSegmentsMap.addItem(roadSegment.getNetwork(),
						roadSegment);
			}

			public void unexecute() {
				if (modifyingNetworks) {
					roadSegment.getNetwork().remove(roadSegment);
				}
				networkToRemovedRoadSegmentsMap.addItem(roadSegment
						.getNetwork(), roadSegment);
			}
		});
		return this;
	}

	private void clearEvents() {
		for (int i = 0; i < 2; i++) {
			networkToAddedRoadSegmentsMap.put(toolboxModel.getSession()
					.getSourceNetwork(i), new HashSet());
			networkToRemovedRoadSegmentsMap.put(toolboxModel.getSession()
					.getSourceNetwork(i), new HashSet());
			networkToModifiedRoadSegmentsMap.put(toolboxModel.getSession()
					.getSourceNetwork(i), new HashSet());
		}
	}

	public void execute() {
		clearEvents();
		for (Iterator i = operations.iterator(); i.hasNext();) {
			Operation operation = (Operation) i.next();
			try {
				operation.execute();
			} catch (Exception e) {
				//Catch exceptions, so that we won't lose roads [Jon Aquino
				// 12/22/2003]
				errorHandler.handleThrowable(e);
			}
		}
		updateResultStatesAndFireEvents();
	}

	private void updateResultStatesAndFireEvents() {
		addModifiedEvents(updateResultStates());
		removeUnnecessaryModifiedEvents();
		fireEvents();
	}

	private void fireEvents() {
		monitor.report("Firing events ...");		
		fireFeatureEvents(networkToAddedRoadSegmentsMap, FeatureEventType.ADDED);
		fireFeatureEvents(networkToModifiedRoadSegmentsMap,
				FeatureEventType.ATTRIBUTES_MODIFIED);
		fireFeatureEvents(networkToRemovedRoadSegmentsMap,
				FeatureEventType.DELETED);
	}

	private void fireFeatureEvents(CollectionMap networkToRoadSegmentsMap,
			FeatureEventType type) {
		for (Iterator i = networkToRoadSegmentsMap.keySet().iterator(); i
				.hasNext();) {
			RoadNetwork roadNetwork = (RoadNetwork) i.next();
			if (networkToRoadSegmentsMap.getItems(roadNetwork).isEmpty()) {
				continue;
			}
			layerManager.fireFeaturesChanged(features(networkToRoadSegmentsMap
					.getItems(roadNetwork)), type, toolboxModel
					.getSourceLayer(roadNetwork.getID()));
			//Cheat a bit -- the oldFeatureClones are actually clones
			//of the *new* features; however, the only party that
			//checks oldFeatureClones is SelectionManager, and for its
			//purposes it doesn't matter provided that the number of
			//sub-geometries in each feature does not change (applies
			//to Polygons and GeometryCollections, which roads are
			//not). [Jon Aquino 2004-02-20]
			layerManager.fireGeometryModified(features(networkToRoadSegmentsMap
					.getItems(roadNetwork)), toolboxModel
					.getSourceLayer(roadNetwork.getID()), CollectionUtil
					.collect(features(networkToRoadSegmentsMap
							.getItems(roadNetwork)), new Block() {
						public Object yield(Object feature) {
							return ((SourceFeature) feature).clone();
						}
					}));
			//Need to fire GeometryModified; otherwise SelectionManager will
			//not update selection. [Jon Aquino 2004-02-20]
		}
	}

	private Collection features(Collection roadSegments) {
		return CollectionUtil.collect(roadSegments, new Block() {
			public Object yield(Object arg) {
				return ((SourceRoadSegment) arg).getFeature();
			}
		});
	}

	private void removeUnnecessaryModifiedEvents() {
		for (int i = 0; i < 2; i++) {
			RoadNetwork network = toolboxModel.getSession().getSourceNetwork(i);
			removeUnnecessaryModifiedEvents(networkToAddedRoadSegmentsMap
					.getItems(network), networkToRemovedRoadSegmentsMap
					.getItems(network), networkToModifiedRoadSegmentsMap,
					network);
		}
	}

	private void removeUnnecessaryModifiedEvents(Collection addedRoadSegments,
			Collection removedRoadSegments,
			CollectionMap networkToModifiedRoadSegmentsMap, RoadNetwork network) {
		networkToModifiedRoadSegmentsMap
				.removeItems(network, addedRoadSegments);
		networkToModifiedRoadSegmentsMap.removeItems(network,
				removedRoadSegments);
		Assert
				.isTrue(
						!new ArrayList(addedRoadSegments)
								.removeAll(removedRoadSegments),
						"Feature added event is fired before feature removed event. "
								+ "This behaviour assumes that the two events do not have "
								+ "common features. Hence the assert. [Jon Aquino 12/16/2003]");
	}

	public Transaction remove(final SourceRoadSegment roadSegment) {
		operations.add(new Operation() {
			//Handle unexpected exceptions (e.g. AssertionFailedException) in
			// a catch block, so we won't lose roads! [Jon Aquino 12/22/2003]
			public void execute() {
				if (modifyingNetworks) {
					roadSegment.getNetwork().remove(roadSegment);
				}
				networkToRemovedRoadSegmentsMap.addItem(roadSegment
						.getNetwork(), roadSegment);
			}

			public void unexecute() {
				if (modifyingNetworks) {
					roadSegment.getNetwork().add(roadSegment);
				}
				networkToAddedRoadSegmentsMap.addItem(roadSegment.getNetwork(),
						roadSegment);
			}
		});
		return this;
	}

	public Transaction setState(final SourceRoadSegment roadSegment,
			final SourceState state, final RoadSegmentMatch match) {
		operations.add(new Operation() {
			public void execute() {
				oldState = roadSegment.getState();
				oldMatch = roadSegment.getMatch();
				roadSegment.setState(state, match);
				networkToModifiedRoadSegmentsMap.addItem(roadSegment
						.getNetwork(), roadSegment);
			}

			public void unexecute() {
				roadSegment.setState(oldState, oldMatch);
				networkToModifiedRoadSegmentsMap.addItem(roadSegment
						.getNetwork(), roadSegment);
			}

			private RoadSegmentMatch oldMatch;

			private SourceState oldState;
		});
		return this;
	}

	public Transaction markAsModified(final SourceRoadSegment roadSegment) {
		operations.add(new Operation() {
			public void execute() {
				networkToModifiedRoadSegmentsMap.addItem(roadSegment
						.getNetwork(), roadSegment);
			}

			public void unexecute() {
				networkToModifiedRoadSegmentsMap.addItem(roadSegment
						.getNetwork(), roadSegment);
			}
		});
		return this;
	}

	public void severMatch(SourceRoadSegment roadSegment) {
		if (roadSegment.getMatch() == null) {
			return;
		}
		setState(roadSegment.getMatch().getA(), SourceState.UNKNOWN, null);
		setState(roadSegment.getMatch().getB(), SourceState.UNKNOWN, null);
	}

	public void unexecute() {
		clearEvents();
		for (int i = operations.size() - 1; i >= 0; i--) {
			Operation operation = (Operation) operations.get(i);
			try {
				operation.unexecute();
			} catch (Exception e) {
				//Catch exceptions, so that we won't lose roads [Jon Aquino
				// 12/22/2003]
				errorHandler.handleThrowable(e);
			}
		}
		updateResultStatesAndFireEvents();
	}

	private void addModifiedEvents(Collection roadSegments) {
		roadSegments.removeAll(networkToModifiedRoadSegmentsMap.values());
		for (Iterator i = roadSegments.iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			networkToModifiedRoadSegmentsMap.addItem(roadSegment.getNetwork(),
					roadSegment);
		}
	}

	private Collection updateResultStates() {
		return updateAffectedSegments(CollectionUtil.concatenate(Arrays
				.asList(new Collection[] {
						CollectionUtil
								.concatenate(networkToAddedRoadSegmentsMap
										.values()),
						CollectionUtil
								.concatenate(networkToRemovedRoadSegmentsMap
										.values()),
						CollectionUtil
								.concatenate(networkToModifiedRoadSegmentsMap
										.values()) })), toolboxModel
				.getSession(), monitor);
	}

	public static Collection updateAffectedSegments(Collection segments,
			ConflationSession session, TaskMonitor monitor) {
		Collection roadSegmentsToUpdate = session.getConsistencyRule()
				.getStateTransitionImpactAssessment().affectedRoadSegments(
						segments, monitor);
		for (Iterator i = roadSegmentsToUpdate.iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			roadSegment.setResultState(ResultStateRules.instance()
					.determineResultState(roadSegment));
		}
		return roadSegmentsToUpdate;
	}

	private static interface Operation {
		public void execute();

		public void unexecute();
	}

	protected CollectionMap getNetworkToAddedRoadSegmentsMap() {
		return networkToAddedRoadSegmentsMap;
	}

	protected CollectionMap getNetworkToModifiedRoadSegmentsMap() {
		return networkToModifiedRoadSegmentsMap;
	}

	protected CollectionMap getNetworkToRemovedRoadSegmentsMap() {
		return networkToRemovedRoadSegmentsMap;
	}

	public void setModifyingNetworks(boolean modifyingNetworks) {
		this.modifyingNetworks = modifyingNetworks;
	}

	public boolean isModifyingNetworks() {
		return modifyingNetworks;
	}
}