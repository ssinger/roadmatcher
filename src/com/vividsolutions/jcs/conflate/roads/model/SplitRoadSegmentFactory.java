package com.vividsolutions.jcs.conflate.roads.model;

import java.util.HashSet;

import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;

public class SplitRoadSegmentFactory {

	/**
	 * @param roadSegmentBeingSplit
	 *            may or may not be a SplitRoadSegment; in either case, the
	 *            returned SplitRoadSegments' parent will not be a
	 *            SplitRoadSegment.
	 */
	public static SplitRoadSegment[] create(LineString[] lineStrings,
			SourceRoadSegment roadSegmentBeingSplit) {
		Assert.isTrue(FUTURE_LineString.first(lineStrings[0]).equals2D(
				FUTURE_LineString.first(roadSegmentBeingSplit.getLine())));
		Assert.isTrue(FUTURE_LineString.last(
				lineStrings[lineStrings.length - 1]).equals2D(
				FUTURE_LineString.last(roadSegmentBeingSplit.getLine())));
		HashSet vertices = FUTURE_CollectionUtil
				.createHashSet(roadSegmentBeingSplit.getLine().getCoordinates());
		SplitRoadSegment[] splitRoadSegments = new SplitRoadSegment[lineStrings.length];
		for (int i = 0; i < lineStrings.length; i++) {
			SplitRoadSegment splitRoadSegment = new SplitRoadSegment(
					lineStrings[i],
					roadSegmentBeingSplit.getOriginalFeature(),
					roadSegmentBeingSplit.getNetwork(),
					roadSegmentBeingSplit instanceof SplitRoadSegment ? ((SplitRoadSegment) roadSegmentBeingSplit)
							.getParent()
							: roadSegmentBeingSplit);
			splitRoadSegments[i] = splitRoadSegment;
		}
		for (int i = 0; i < lineStrings.length; i++) {
			if (i > 0) {
				splitRoadSegments[i].setSiblingAtStart(
						splitRoadSegments[i - 1], vertices
								.contains(FUTURE_LineString
										.first(lineStrings[i])));
			}
			if (i < lineStrings.length - 1) {
				splitRoadSegments[i].setSiblingAtEnd(splitRoadSegments[i + 1],
						vertices.contains(FUTURE_LineString
								.last(lineStrings[i])));
			}
		}
		if (roadSegmentBeingSplit instanceof SplitRoadSegment) {
			splitRoadSegments[0].setSiblingAtStart(
					((SplitRoadSegment) roadSegmentBeingSplit)
							.getSiblingAtStart(),
					((SplitRoadSegment) roadSegmentBeingSplit)
							.wasStartSplitNodeExistingVertex());
			splitRoadSegments[splitRoadSegments.length - 1].setSiblingAtEnd(
					((SplitRoadSegment) roadSegmentBeingSplit)
							.getSiblingAtEnd(),
					((SplitRoadSegment) roadSegmentBeingSplit)
							.wasEndSplitNodeExistingVertex());
		}
		assertContiguous(splitRoadSegments);
		return splitRoadSegments;
	}

	private static void assertContiguous(SplitRoadSegment[] splitRoadSegments) {
		for (int i = 0; i < splitRoadSegments.length; i++) {
			if (splitRoadSegments[i].getSiblingAtStart() != null) {
				Assert.isTrue(FUTURE_LineString.first(
						splitRoadSegments[i].getLine()).equals(
						FUTURE_LineString.last(splitRoadSegments[i]
								.getSiblingAtStart().getLine())),
						"Split not contiguous");
			}
			if (splitRoadSegments[i].getSiblingAtEnd() != null) {
				Assert.isTrue(FUTURE_LineString.last(
						splitRoadSegments[i].getLine()).equals(
						FUTURE_LineString.first(splitRoadSegments[i]
								.getSiblingAtEnd().getLine())),
						"Split not contiguous");
			}
		}
	}

}