package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;
import java.util.*;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.NeighbourhoodList;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public abstract class PostponeInconsistencyHandler {
	private String startNeighbourhoodKey;

	private String endNeighbourhoodKey;

	public PostponeInconsistencyHandler(String startNeighbourhoodKey,
			String endNeighbourhoodKey) {
		this.startNeighbourhoodKey = startNeighbourhoodKey;
		this.endNeighbourhoodKey = endNeighbourhoodKey;
	}

	public void setPostponedForInconsistenciesAt(Point point,
			boolean postponed, final PlugInContext context)
			throws NoninvertibleTransformException {
		context.getLayerManager().getUndoableEditReceiver()
				.reportNothingToUndoYet();
		Envelope envelope = EnvelopeUtil.expand(new Envelope(context
				.getLayerViewPanel().getViewport().toModelCoordinate(point)),
				ToolboxModel.INCLUDED_OUTER_LINE_WIDTH
						/ context.getLayerViewPanel().getViewport().getScale());
		final ToolboxModel toolboxModel = ToolboxModel.instance(context);
		Collection roadSegments = roadSegments(envelope, toolboxModel
				.getSession());
		if (roadSegments.isEmpty()) {
			warn(postponed, context);
			return;
		}
		Collection inconsistentNeighbourhoods = inconsistentNeighbourhoods(roadSegments);
		if (inconsistentNeighbourhoods.isEmpty()) {
			warn(postponed, context);
			return;
		}
		final Collection closestInconsistentNeighbourhood = closest(context
				.getLayerViewPanel().getViewport().toModelCoordinate(point),
				inconsistentNeighbourhoods, !postponed, NeighbourhoodList
						.postponedInconsistentNeighbourhoods(toolboxModel
								.getSession()));
		if (closestInconsistentNeighbourhood == null) {
			warn(postponed, context);
			return;
		}
		Block postponeBlock = new Block() {
			public Object yield(Object inconsistentNeighbourhoods) {
				NeighbourhoodList.postponedInconsistentNeighbourhoods(
						toolboxModel.getSession()).addAll(
						(Collection) inconsistentNeighbourhoods);
				return null;
			}
		};
		Block restoreBlock = new Block() {
			public Object yield(Object inconsistentNeighbourhoods) {
				NeighbourhoodList.postponedInconsistentNeighbourhoods(
						toolboxModel.getSession()).removeAll(
						(Collection) inconsistentNeighbourhoods);
				return null;
			}
		};
		updatePostponedInconsistentNeighbourhoods(postponed
				? postponeBlock
				: restoreBlock, postponed ? restoreBlock : postponeBlock,
				Collections.singleton(new TreeSet(
						closestInconsistentNeighbourhood)), toolboxModel);
	}

	private void warn(boolean postpone, PlugInContext context) {
		context
				.getWorkbenchFrame()
				.warnUser(
						postpone
								? ErrorMessages.postponeInconsistencyHandler_noInconsistentNodes
								: ErrorMessages.postponeInconsistencyHandler_noPostponedInconsistencies);
	}

	protected void updatePostponedInconsistentNeighbourhoods(
			final Block executeBlock, final Block unexecuteBlock,
			final Collection inconsistentNeighbourhoods,
			final ToolboxModel toolboxModel) {
		//Note that this method is overridden [Jon Aquino 2004-06-07]
		AbstractPlugIn.execute(new UndoableCommand(AbstractPlugIn
				.createName(PostponeInconsistencyPlugIn.class)) {
			public void execute() {
				executeBlock.yield(inconsistentNeighbourhoods);
				toolboxModel.getSourceLayer(0).fireAppearanceChanged();
				toolboxModel.getSourceLayer(1).fireAppearanceChanged();
			}

			public void unexecute() {
				unexecuteBlock.yield(inconsistentNeighbourhoods);
				toolboxModel.getSourceLayer(0).fireAppearanceChanged();
				toolboxModel.getSourceLayer(1).fireAppearanceChanged();
			}
		}, toolboxModel.getContext());
	}

	private Set roadSegments(SortedSet warningLocations,
			ConflationSession session) {
		HashSet roadSegments = new HashSet();
		for (Iterator i = warningLocations.iterator(); i.hasNext();) {
			Coordinate warningLocation = (Coordinate) i.next();
			roadSegments.addAll(roadSegments(warningLocation, session));
		}
		return roadSegments;
	}

	protected abstract Collection roadSegments(Coordinate warningLocation,
			ConflationSession session);

	private Collection closest(Coordinate coordinate,
			Collection neighbourhoods, boolean postponed,
			NeighbourhoodList postponedInconsistentNeighbourhoods) {
		Collection closestNeighbourhood = null;
		double closestDistance = -1;
		for (Iterator i = neighbourhoods.iterator(); i.hasNext();) {
			Collection candidateNeighbourhood = (Collection) i.next();
			if (postponed != postponedInconsistentNeighbourhoods
					.contains(new TreeSet(candidateNeighbourhood))) {
				continue;
			}
			double candidateDistance = distance(coordinate,
					candidateNeighbourhood);
			if (closestNeighbourhood == null
					|| candidateDistance < closestDistance) {
				closestNeighbourhood = candidateNeighbourhood;
				closestDistance = candidateDistance;
			}
		}
		return closestNeighbourhood;
	}

	private double distance(Coordinate coordinate, Collection neighbourhood) {
		double closestDistance = ((Coordinate) neighbourhood.iterator().next())
				.distance(coordinate);
		for (Iterator i = neighbourhood.iterator(); i.hasNext();) {
			Coordinate neighbour = (Coordinate) i.next();
			closestDistance = Math.min(closestDistance, neighbour
					.distance(coordinate));
		}
		return closestDistance;
	}

	private Collection inconsistentNeighbourhoods(Collection roadSegments) {
		return (Collection) FUTURE_CollectionUtil.injectInto(roadSegments,
				new ArrayList(), new Block() {
					public Object yield(Object inconsistentNeighbourhoods,
							Object roadSegment) {
						CollectionUtil.addIfNotNull(
								((SourceRoadSegment) roadSegment)
										.getResultStateDescription()
										.getBlackboard().get(
												startNeighbourhoodKey),
								(Collection) inconsistentNeighbourhoods);
						CollectionUtil.addIfNotNull(
								((SourceRoadSegment) roadSegment)
										.getResultStateDescription()
										.getBlackboard().get(
												endNeighbourhoodKey),
								(Collection) inconsistentNeighbourhoods);
						return null;
					}
				});
	}

	protected abstract Collection roadSegments(Envelope envelope,
			ConflationSession session) throws NoninvertibleTransformException;

}