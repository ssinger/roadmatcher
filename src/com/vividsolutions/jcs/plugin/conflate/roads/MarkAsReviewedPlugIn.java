package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.List;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public abstract class MarkAsReviewedPlugIn extends RightClickSegmentPlugIn {
	public static class Reviewed extends MarkAsReviewedPlugIn {
		public Reviewed() {
			super(true, "Mark As Reviewed");
		}
	}

	public static class Unreviewed extends MarkAsReviewedPlugIn {
		public Unreviewed() {
			super(false, "Mark As Unreviewed");
		}
	}

	private boolean reviewed;

	private String name;

	private MarkAsReviewedPlugIn(boolean reviewed, String name) {
		this.reviewed = reviewed;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected boolean execute(final List segments, final PlugInContext context) {
		final List originalValues = (List) CollectionUtil.collect(segments,
				new Block() {
					public Object yield(Object segment) {
						return Boolean.valueOf(((SourceRoadSegment) segment)
								.isReviewed());
					}
				});
		execute(new UndoableCommand(getName()) {
			public void execute() {
				for (int i = 0; i < segments.size(); i++) {
					((SourceRoadSegment) segments.get(i)).setReviewed(reviewed);
				}
				fireFeaturesChanged(0, segments, context);
				fireFeaturesChanged(1, segments, context);
			}

			public void unexecute() {
				for (int i = 0; i < segments.size(); i++) {
					((SourceRoadSegment) segments.get(i))
							.setReviewed(((Boolean) originalValues.get(i))
									.booleanValue());
				}
				fireFeaturesChanged(0, segments, context);
				fireFeaturesChanged(1, segments, context);
			}
		}, context);
		return true;
	}

}