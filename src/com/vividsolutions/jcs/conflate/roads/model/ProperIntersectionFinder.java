package com.vividsolutions.jcs.conflate.roads.model;
import java.util.Collection;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
public class ProperIntersectionFinder {
	private static boolean properlyIntersect(LineString a, LineString b) {
		IntersectionMatrix matrix = a.relate(b);
		if (matrix.matches("T********")) {
			return true;
		}
		if (matrix.matches("FF*F*****")) {
			return false;
		}
		//Might be a cul-de-sac [Jon Aquino 12/3/2003]
		Geometry intersection = a.intersection(b);
		Assert.isTrue(intersection.getDimension() == 0);
		Coordinate[] coordinates = intersection.getCoordinates();
		for (int i = 0; i < coordinates.length; i++) {
			if (coordinates[i].equals(LineStringUtil.first(a))
					&& coordinates[i].equals(LineStringUtil.first(b))) {
				continue;
			}
			if (coordinates[i].equals(LineStringUtil.first(a))
					&& coordinates[i].equals(LineStringUtil.last(b))) {
				continue;
			}
			if (coordinates[i].equals(LineStringUtil.last(a))
					&& coordinates[i].equals(LineStringUtil.first(b))) {
				continue;
			}
			if (coordinates[i].equals(LineStringUtil.last(a))
					&& coordinates[i].equals(LineStringUtil.last(b))) {
				continue;
			}
			return true;
		}
		return false;
	}
	public static Collection properlyIntersectingIncludedRoadSegments(
			SourceRoadSegment roadSegment) {
		return ProperIntersectionFinder.properlyIntersectingRoadSegments(
				roadSegment, new Block() {
					public Object yield(Object candidate) {
						return Boolean.valueOf(((SourceRoadSegment) candidate)
								.getState().indicates(SourceState.INCLUDED));
					}
				});
	}
	public static Collection properlyIntersectingRoadSegments(
			final SourceRoadSegment roadSegment, final Block criterion) {
		return properlyIntersectingRoadSegments(roadSegment.getApparentLine(),
				roadSegment.getNetwork().getSession(), new Block() {
					public Object yield(Object candidate) {
						return Boolean.valueOf(candidate != roadSegment
								&& ((Boolean) criterion.yield(candidate))
										.booleanValue());
					}
				});
	}
	public static Collection properlyIntersectingRoadSegments(LineString line,
			ConflationSession session, Block criterion) {
		return FUTURE_CollectionUtil.concatenate(
				properlyIntersectingRoadSegments(line, session
						.getSourceNetwork(0), criterion),
				properlyIntersectingRoadSegments(line, session
						.getSourceNetwork(1), criterion));
	}
	private static Collection properlyIntersectingRoadSegments(
			final LineString line, RoadNetwork network, final Block criterion) {
		return CollectionUtil.select(
				network.roadSegmentsApparentlyIntersecting(line
						.getEnvelopeInternal()), new Block() {
					public Object yield(Object arg) {
						SourceRoadSegment candidate = (SourceRoadSegment) arg;
						return Boolean.valueOf(((Boolean) criterion
								.yield(candidate)).booleanValue()
								&& properlyIntersect(line, candidate
										.getApparentLine()));
					}
				});
	}
}
