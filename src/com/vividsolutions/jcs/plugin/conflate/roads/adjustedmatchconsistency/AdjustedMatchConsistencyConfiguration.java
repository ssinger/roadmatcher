package com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency;

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.ApparentNode;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.ConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.AutoAdjuster;
import com.vividsolutions.jcs.plugin.conflate.roads.ToolboxModel;
import com.vividsolutions.jcs.plugin.conflate.roads.PostponeInconsistencyHandler;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class AdjustedMatchConsistencyConfiguration
		implements
			ConsistencyConfiguration {

	public Style getStyle() {
		return style;
	}

	public ConsistencyRule getRule() {
		return rule;
	}

	private AdjustedMatchConsistencyRule rule = new AdjustedMatchConsistencyRule();

	private PostponeInconsistencyHandler postponeInconsistencyHandler = new PostponeInconsistencyHandler(
			AdjustedMatchConsistencyRule.START_NEIGHBOURHOOD_KEY,
			AdjustedMatchConsistencyRule.END_NEIGHBOURHOOD_KEY) {
		protected Collection roadSegments(Coordinate warningLocation,
				ConflationSession session) {
			return new ApparentNode(
					warningLocation, session).getIncidentRoadSegments();
		}

		protected Collection roadSegments(Envelope envelope,
				ConflationSession session)
				throws NoninvertibleTransformException {
			return FUTURE_CollectionUtil.concatenate(session
					.getSourceNetwork(0).roadSegmentsApparentlyIntersecting(
							envelope), session.getSourceNetwork(1)
					.roadSegmentsApparentlyIntersecting(envelope));
		}
		protected void updatePostponedInconsistentNeighbourhoods(
				Block executeBlock, Block unexecuteBlock,
				Collection inconsistentNeighbourhoods, ToolboxModel toolboxModel) {
			Assert.isTrue(inconsistentNeighbourhoods.size() == 1);
			super.updatePostponedInconsistentNeighbourhoods(executeBlock,
					unexecuteBlock, AdjustedMatchInconsistentEndpointStyle
							.associatedNeighbourhoods(
									(Collection) inconsistentNeighbourhoods
											.iterator().next(),
									toolboxModel.getSession()).toCollection(),
					toolboxModel);
		}
	};

	private Style style = new AdjustedMatchInconsistentEndpointStyle();

	public AutoAdjuster getAutoAdjuster() {
		return autoAdjuster;
	}

	private AutoAdjuster autoAdjuster = new AdjustedMatchAutoAdjuster();

	public void setPostponedForInconsistenciesAt(Point point, boolean postponed, PlugInContext context) throws NoninvertibleTransformException {
		postponeInconsistencyHandler.setPostponedForInconsistenciesAt(point, postponed, context);
	}

}