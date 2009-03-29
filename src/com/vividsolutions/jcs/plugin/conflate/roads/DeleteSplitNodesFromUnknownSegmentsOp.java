package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jcs.algorithm.linearreference.LineStringLocation;
import com.vividsolutions.jcs.algorithm.linearreference.LocationIndexedLine;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegmentSiblingUpdater;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

/**
 * Bug-tolerant alternative to DeleteSplitNodeOp. Even if sibling split segments
 * overlap or point to "stale" segments (no longer in graph) instead of each
 * other, this operation will not fail because it reconstructs the geometry from
 * the parent.
 * <p>
 * A restriction with using this class is that the segments must be in the
 * Unknown state. DeleteSplitNodeOp does not have this restriction, making it
 * more suitable for the DeleteSplitNodeTool.
 */
public class DeleteSplitNodesFromUnknownSegmentsOp {

	/**
	 * @param segmentsSplitAtStart
	 *            some possibly not in the network (because of
	 *            discontiguous-split-segments bug)
	 */
	public UndoableCommand createUndoableCommand(final Set splitSegments,
			ToolboxModel toolboxModel, ErrorHandler errorHandler, String name) {
		final Transaction transaction = new Transaction(toolboxModel,
				errorHandler);
		for (Iterator i = splitSegments.iterator(); i.hasNext();) {
			SplitRoadSegment segment = (SplitRoadSegment) i.next();
			if (!segment.isInNetwork()) {
				// Caused by the discontiguous-split-segments bug. Don't include
				// it in the transaction; otherwise get assertion failure.
				// [Jon Aquino 2004-12-06]
				continue;
			}
			transaction.remove(segment);
		}
		final Collection chains = assertOneChainPerSplitSegment(
				chains(splitSegments), splitSegments.size());
		for (Iterator i = chains.iterator(); i.hasNext();) {
			Chain chain = (Chain) i.next();
			transaction.add(chain.getCombinedSegment());
		}
		return new UndoableCommand(name) {
			public void execute() {
				for (Iterator i = splitSegments.iterator(); i.hasNext();) {
					SplitRoadSegment segment = (SplitRoadSegment) i.next();
					Assert.isTrue(segment.getState() == SourceState.UNKNOWN);
				}
				transaction.execute();
				executeSiblingUpdaters(chains);
			}

			public void unexecute() {
				unexecuteSiblingUpdaters(chains);
				transaction.unexecute();
			}
		};
	}

	private void executeSiblingUpdaters(Collection chains) {
		for (Iterator i = chains.iterator(); i.hasNext();) {
			Chain chain = (Chain) i.next();
			chain.getSiblingUpdater().execute();
		}
	}

	private void unexecuteSiblingUpdaters(Collection chains) {
		for (Iterator i = chains.iterator(); i.hasNext();) {
			Chain chain = (Chain) i.next();
			chain.getSiblingUpdater().unexecute();
		}
	}

	private Collection chains(Set splitSegments) {
		CollectionMap parentToSegmentListMap = parentToSegmentListMap(splitSegments);
		Collection chains = new ArrayList();
		for (Iterator i = parentToSegmentListMap.keySet().iterator(); i
				.hasNext();) {
			SourceRoadSegment parent = (SourceRoadSegment) i.next();
			chains.addAll(chainsForSameParent(parentToSegmentListMap
					.getItems(parent)));
		}
		return chains;
	}

	private Collection chainsForSameParent(Collection childSegments) {
		// Note that we coalesce segments if their locations overlap.
		// But what if adjacent segments' locations do not overlap, and
		// they do not point to each other (the bug we've observed)?
		// The complex way to handle it would be to decide to coalesce
		// based on the locations of all of the children i.e. Does the
		// gap get filled? A simple way to handle it would be to revert
		// all the children to the parent if the fence simply contains
		// the parent. But we are not doing either currently (the
		// situation has not come up in practice). [Jon Aquino 2004-12-03]
		return toChains(buildPreChains(childSegments,
				new CompositePreChainBuilder(new PreChainBuilder[] {
						siblingReferencePreChainBuilder,
						coincidentNodePreChainBuilder,
						overlappingLocationPreChainBuilder })));
		// The second and third PreChainBuilders are workarounds for the
		// "discontiguous split segments" bug [Jon Aquino 2004-12-03]
	}

	private Collection toChains(Collection preChains) {
		return CollectionUtil.collect(preChains, new Block() {
			public Object yield(Object preChain) {
				return toChain((PreChain) preChain);
			}
		});
	}

	private Chain toChain(PreChain preChain) {
		return new Chain(preChain.getIntervals(), combinedSegment(preChain
				.getIntervals()));
	}

	private SplitRoadSegment combinedSegment(Collection intervals) {
		SplitRoadSegment combinedSegment = new SplitRoadSegment(
				lineString(intervals), firstSegment(intervals)
						.getOriginalFeature(), firstSegment(intervals)
						.getNetwork(), firstSegment(intervals).getParent());
		combinedSegment.setSiblingAtStart(firstSegment(intervals)
				.getSiblingAtStart(), firstSegment(intervals)
				.wasStartSplitNodeExistingVertex());
		combinedSegment.setSiblingAtEnd(lastSegment(intervals)
				.getSiblingAtEnd(), lastSegment(intervals)
				.wasEndSplitNodeExistingVertex());
		return combinedSegment;
	}

	private SplitRoadSegment firstSegment(Collection intervals) {
		return first(intervals).getSegment();
	}

	private SplitRoadSegment lastSegment(Collection intervals) {
		return last(intervals).getSegment();
	}

	private Interval first(Collection intervals) {
		return (Interval) Collections.min(intervals, startComparator);
	}

	private Interval last(Collection intervals) {
		return (Interval) Collections.max(intervals, endComparator);
	}

	private Comparator startComparator = new Comparator() {
		public int compare(Object a, Object b) {
			return ((Interval) a).getMin().compareTo(((Interval) b).getMin());
		}
	};

	private Comparator endComparator = new Comparator() {
		public int compare(Object a, Object b) {
			return ((Interval) a).getMax().compareTo(((Interval) b).getMax());
		}
	};

	private LineString lineString(Collection intervals) {
		return new LocationIndexedLine(firstSegment(intervals).getParent()
				.getLine()).locate(first(intervals).getMin(), last(intervals)
				.getMax());
	}

	private PreChainBuilder overlappingLocationPreChainBuilder = new BasicPreChainBuilder() {
		protected boolean approvesAdditionOf(Interval interval,
				Interval existingInterval) {
			return interval.intersects(existingInterval);
		}
	};

	private class Interval {
		private LineStringLocation min;

		private LineStringLocation max;

		private SplitRoadSegment segment;

		public Interval(SplitRoadSegment segment) {
			this(new LocationIndexedLine(segment.getParent().getLine())
					.indicesOf(segment.getLine()), segment);
		}

		private Interval(LineStringLocation[] locations,
				SplitRoadSegment segment) {
			Assert.isTrue(locations.length == 2);
			Assert.isTrue(locations[0].compareTo(locations[1]) < 0);
			min = locations[0];
			max = locations[1];
			this.segment = segment;
		}

		public boolean intersects(Interval other) {
			return !(other.min.compareTo(max) > 0 || other.max.compareTo(min) < 0);
		}

		public SplitRoadSegment getSegment() {
			return segment;
		}

		public LineStringLocation getMax() {
			return max;
		}

		public LineStringLocation getMin() {
			return min;
		}
	}

	private PreChainBuilder coincidentNodePreChainBuilder = new BasicPreChainBuilder() {
		protected boolean approvesAdditionOf(Interval interval,
				Interval existingInterval) {
			// Theoretically we can simply compare node references. But compare
			// node locations to be bug-tolerant. [Jon Aquino 2004-12-03]
			// In fact, "bad" segments may return null for their start or end
			// nodes. So go a step further and use the start and end coordinates
			// of the lines [Jon Aquino 2004-12-06]
			return start(interval.getSegment().getLine()).equals(
					start(existingInterval.getSegment().getLine()))
					|| start(interval.getSegment().getLine()).equals(
							end(existingInterval.getSegment().getLine()))
					|| end(interval.getSegment().getLine()).equals(
							start(existingInterval.getSegment().getLine()))
					|| end(interval.getSegment().getLine()).equals(
							end(existingInterval.getSegment().getLine()));
		}

		private Coordinate start(LineString line) {
			return FUTURE_LineString.first(line);
		}

		private Coordinate end(LineString line) {
			return FUTURE_LineString.last(line);
		}
	};

	private PreChainBuilder siblingReferencePreChainBuilder = new BasicPreChainBuilder() {
		protected boolean approvesAdditionOf(Interval interval,
				Interval existingInterval) {
			// Redundant logic to work around the bug [Jon Aquino 2004-12-03]
			return interval.getSegment().getSiblingAtStart() == existingInterval
					.getSegment()
					|| interval.getSegment().getSiblingAtEnd() == existingInterval
							.getSegment()
					|| existingInterval.getSegment().getSiblingAtStart() == interval
							.getSegment()
					|| existingInterval.getSegment().getSiblingAtEnd() == interval
							.getSegment();
		}
	};

	private class CompositePreChainBuilder implements PreChainBuilder {
		private PreChainBuilder[] preChainBuilders;

		public CompositePreChainBuilder(PreChainBuilder[] preChainBuilders) {
			this.preChainBuilders = preChainBuilders;
		}

		public boolean approvesAdditionOf(Interval interval, PreChain preChain) {
			for (int i = 0; i < preChainBuilders.length; i++) {
				if (preChainBuilders[i].approvesAdditionOf(interval, preChain)) {
					return true;
				}
			}
			return false;
		}
	}

	private Collection buildPreChains(Collection childSegments,
			PreChainBuilder builder) {
		Collection preChains = new ArrayList();
		//Sort child segments. Otherwise, if process A then C then B,
		//we end up with two chains: A+B and C. [Jon Aquino 2004-12-06]
		outer: for (Iterator i = intervalsSortedByStartLocations(childSegments)
				.iterator(); i.hasNext();) {
			Interval interval = (Interval) i.next();
			for (Iterator j = preChains.iterator(); j.hasNext();) {
				PreChain preChain = (PreChain) j.next();
				if (builder.approvesAdditionOf(interval, preChain)) {
					preChain.add(interval);
					continue outer;
				}
			}
			preChains.add(new PreChain(interval));
		}
		return preChains;
	}

	private List intervalsSortedByStartLocations(Collection splitSegments) {
		// Don't return a Set; otherwise we will lose intervals.
		// [Jon Aquino 2004-12-06]
		List intervals = new ArrayList(CollectionUtil.collect(splitSegments,
				new Block() {
					public Object yield(Object splitSegment) {
						return new Interval((SplitRoadSegment) splitSegment);
					}
				}));
		Collections.sort(intervals, startComparator);
		return intervals;
	}

	private interface PreChainBuilder {
		public boolean approvesAdditionOf(Interval interval, PreChain preChain);
	}

	private abstract class BasicPreChainBuilder implements PreChainBuilder {
		public boolean approvesAdditionOf(Interval interval, PreChain preChain) {
			for (Iterator i = preChain.getIntervals().iterator(); i.hasNext();) {
				Interval existingInterval = (Interval) i.next();
				if (approvesAdditionOf(interval, existingInterval)) {
					return true;
				}
			}
			return false;
		}

		protected abstract boolean approvesAdditionOf(Interval interval,
				Interval existingInterval);
	}

	/**
	 * Precursor to a chain. Used to build a chain gradually.
	 */
	private class PreChain {
		private Collection intervals = new ArrayList();

		public PreChain(Interval interval) {
			add(interval);
		}

		public void add(Interval interval) {
			intervals.add(interval);
		}

		public Collection getIntervals() {
			// Not wrapped with SwingUtilities#unmodifiableCollection
			// for (unverified) performance reasons [Jon Aquino 2004-12-03]
			return intervals;
		}
	}

	private CollectionMap parentToSegmentListMap(Set splitSegments) {
		CollectionMap parentToSegmentListMap = new CollectionMap();
		for (Iterator i = splitSegments.iterator(); i.hasNext();) {
			SplitRoadSegment segment = (SplitRoadSegment) i.next();
			parentToSegmentListMap.addItem(segment.getParent(), segment);
		}
		return parentToSegmentListMap;
	}

	private Collection assertOneChainPerSplitSegment(Collection chains,
			int segmentCount) {
		CollectionMap segmentToChainMap = new CollectionMap();
		for (Iterator i = chains.iterator(); i.hasNext();) {
			Chain chain = (Chain) i.next();
			for (Iterator j = chain.getIntervals().iterator(); j.hasNext();) {
				Interval interval = (Interval) j.next();
				segmentToChainMap.addItem(interval.getSegment(), chain);
			}
		}
		for (Iterator i = segmentToChainMap.keySet().iterator(); i.hasNext();) {
			SplitRoadSegment segment = (SplitRoadSegment) i.next();
			Assert.equals(new Integer(1), new Integer(segmentToChainMap
					.getItems(segment).size()));
		}
		Assert.equals(new Integer(segmentCount), new Integer(segmentToChainMap
				.keySet().size()));
		return chains;
	}

	private class Chain {
		private Collection intervals = new ArrayList();

		private SplitRoadSegmentSiblingUpdater siblingUpdater;

		private SplitRoadSegment combinedSegment;

		public Chain(Collection intervals, SplitRoadSegment combinedSegment) {
			this.intervals = intervals;
			this.combinedSegment = combinedSegment;
			this.siblingUpdater = new SplitRoadSegmentSiblingUpdater(
					combinedSegment);
		}

		public Collection getIntervals() {
			return intervals;
		}

		public SplitRoadSegmentSiblingUpdater getSiblingUpdater() {
			return siblingUpdater;
		}

		public SplitRoadSegment getCombinedSegment() {
			return combinedSegment;
		}
	}
}