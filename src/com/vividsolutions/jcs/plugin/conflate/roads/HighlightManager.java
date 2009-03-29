package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
public class HighlightManager {
	public static abstract class Highlight {
		public boolean equals(Object obj) {
			return getClass() == obj.getClass();
		}
		public String toString() {
			//Appears in combobox [Jon Aquino 2004-03-12]
			return StringUtil.toFriendlyName(getClass().getName(), "Highlight");
		}
		private static ColourScheme mute(ColourScheme colourScheme,
				Color mutedColour0, Color mutedColour1) {
			Color average = FUTURE_GUIUtil.average(mutedColour0, mutedColour1);
			return colourScheme.setAdjustedColour(average)
					.setPendingInnerLineColour0(mutedColour0)
					.setPendingInnerLineColour1(mutedColour1)
					.setInconsistentInnerLineColour0(mutedColour0)
					.setInconsistentInnerLineColour1(mutedColour1)
					.setIntegratedInnerLineColour0(mutedColour0)
					.setIntegratedInnerLineColour1(mutedColour1)
					.setIntegratedColour(average)
					.setMatchedNonReferenceColour0(mutedColour0)
					.setMatchedNonReferenceColour1(mutedColour1)
					.setRetiredColour0(mutedColour0).setRetiredColour1(
							mutedColour1).setRingColour(average)
					.setUnknownColour0(mutedColour0).setUnknownColour1(
							mutedColour1);
		}
		protected ColourScheme mute(ColourScheme colourScheme) {
			return mute(colourScheme, FUTURE_GUIUtil.multiply(
					MUTED_SATURATION_MULTIPLIER,
					MUTED_BRIGHTNESS_MULTIPLIER, MUTED_ALPHA_MULTIPLIER,
					colourScheme.getDefaultColour0()), FUTURE_GUIUtil.multiply(MUTED_SATURATION_MULTIPLIER, MUTED_BRIGHTNESS_MULTIPLIER,
					MUTED_ALPHA_MULTIPLIER, colourScheme
					.getDefaultColour1()));
		}
		public abstract ColourScheme highlight(
				ColourScheme unhighlightedColourScheme);
	}
	public static class InconsistentSegments extends Highlight {
		public ColourScheme highlight(ColourScheme unhighlightedColourScheme) {
			return mute((ColourScheme) unhighlightedColourScheme.clone())
					.setInconsistentInnerLineColour0(
							unhighlightedColourScheme
									.getInconsistentInnerLineColour0())
					.setInconsistentInnerLineColour1(
							unhighlightedColourScheme
									.getInconsistentInnerLineColour1());
		}
	}
	public static class IncludedSegments extends Highlight {
		public ColourScheme highlight(ColourScheme unhighlightedColourScheme) {
			return mute((ColourScheme) unhighlightedColourScheme.clone())
					.setPendingInnerLineColour0(
							unhighlightedColourScheme
									.getPendingInnerLineColour0())
					.setPendingInnerLineColour1(
							unhighlightedColourScheme
									.getPendingInnerLineColour1())
					.setInconsistentInnerLineColour0(
							unhighlightedColourScheme
									.getInconsistentInnerLineColour0())
					.setInconsistentInnerLineColour1(
							unhighlightedColourScheme
									.getInconsistentInnerLineColour1())
					.setIntegratedInnerLineColour0(
							unhighlightedColourScheme
									.getIntegratedInnerLineColour0())
					.setIntegratedInnerLineColour1(
							unhighlightedColourScheme
									.getIntegratedInnerLineColour1());
		}
	}
	public static class UnknownSegments extends Highlight {
		public ColourScheme highlight(ColourScheme unhighlightedColourScheme) {
			return mute((ColourScheme) unhighlightedColourScheme.clone())
					.setUnknownColour0(
							unhighlightedColourScheme.getUnknownColour0())
					.setUnknownColour1(
							unhighlightedColourScheme.getUnknownColour1());
		}
	}
	public HighlightManager(WorkbenchContext context) {
		this.context = context;
	}
	public ColourScheme getColourScheme() {
		if (!isHighlighting()) {
			return ColourSchemeRegistry.current(context);
		}
		if (lastUnhighlightedColourScheme != ColourSchemeRegistry
				.current(context)
				|| lastHighlightClass != getHighlightClass()) {
			lastUnhighlightedColourScheme = ColourSchemeRegistry
					.current(context);
			lastHighlightClass = getHighlightClass();
			try {
				lastColourScheme = ((Highlight) getHighlightClass()
						.newInstance())
						.highlight(lastUnhighlightedColourScheme);
			} catch (InstantiationException e) {
				Assert.shouldNeverReachHere(StringUtil.stackTrace(e));
			} catch (IllegalAccessException e) {
				Assert.shouldNeverReachHere(StringUtil.stackTrace(e));
			}
		}
		return lastColourScheme;
	}
	public Class getHighlightClass() {
		return (Class) ApplicationOptionsPlugIn.options(context).get(HIGHLIGHT_CLASS_KEY,
				DEFAULT_HIGHLIGHT_CLASS);
	}
	public boolean isHighlighting() {
		return ApplicationOptionsPlugIn.options(context).get(HIGHLIGHTING_KEY, false);
	}
	public void setHighlightClass(Class highlightClass) {
		ApplicationOptionsPlugIn.options(context).put(HIGHLIGHT_CLASS_KEY, highlightClass);
	}
	public void setHighlighting(boolean highlighting) {
		ApplicationOptionsPlugIn.options(context).put(HIGHLIGHTING_KEY, highlighting);
	}
	private WorkbenchContext context;
	private ColourScheme lastColourScheme;
	private Class lastHighlightClass;
	private ColourScheme lastUnhighlightedColourScheme;
	public static HighlightManager instance(WorkbenchContext context) {
		if (instance == null) {
			instance = new HighlightManager(context);
		}
		return instance;
	}
	private static final Class DEFAULT_HIGHLIGHT_CLASS = UnknownSegments.class;
	private static final String HIGHLIGHT_CLASS_KEY = HighlightManager.class
			.getName()
			+ " - HIGHLIGHT CLASS";
	private static final String HIGHLIGHTING_KEY = HighlightManager.class
			.getName()
			+ " - HIGHLIGHTING";
	private static HighlightManager instance;
	private static final double MUTED_ALPHA_MULTIPLIER = 0.1;
	private static final double MUTED_BRIGHTNESS_MULTIPLIER = 0.5;
	private static final double MUTED_SATURATION_MULTIPLIER = 1.0;
}
