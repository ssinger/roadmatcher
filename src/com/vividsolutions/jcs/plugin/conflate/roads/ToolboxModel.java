package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import bsh.EvalError;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.jump.FUTURE_Block;
import com.vividsolutions.jcs.jump.FUTURE_DelegatingStyle;
import com.vividsolutions.jcs.jump.FUTURE_DotLineStringEndpointStyle;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.OrderedMap;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class ToolboxModel {
	private static double INCONSISTENT_OUTER_LINE_ALPHA = 0.3;

	public static final String SOURCE_CATEGORY = "RoadMatcher Networks";

	public static final String SHOWING_INCLUDED_STATUS_KEY = ToolboxPanel.class
			.getName()
			+ " - SHOWING INCLUDED STATUS";

	public static final String SHOWING_INCONSISTENT_SEGMENTS_KEY = ToolboxPanel.class
			.getName()
			+ " - SHOWING INCONSISTENT SEGMENTS";

	private WorkbenchContext context;

	private LayerManager layerManager;

	public ToolboxModel(LayerManager layerManager, WorkbenchContext context) {
		this.layerManager = layerManager;
		this.context = context;
	}

	public static final int INCLUDED_OUTER_LINE_WIDTH = 8;

	public static final int INCLUDED_INNER_LINE_WIDTH = 2;

	private static final String ROADMATCHER_LAYER_MARKER_KEY = ToolboxModel.class
			.getName()
			+ "ROADMATCHER LAYER MARKER";

	private static class SourceFeatureEndpointStyle extends
			FUTURE_DotLineStringEndpointStyle {
		private ToolboxModel.StyleChooser styleChooser;

		private BasicStyle style;

		private WorkbenchContext context;

		public void paint(Feature f, Graphics2D g, Viewport viewport)
				throws Exception {
			style = styleChooser.style(((SourceFeature) f).getRoadSegment());
			super.paint(f, g, viewport);
		}

		private SourceFeatureEndpointStyle(String name, boolean start,
				String iconFile, ToolboxModel.StyleChooser styleChooser,
				WorkbenchContext context) {
			super(name, start, iconFile);
			this.context = context;
			this.styleChooser = styleChooser;
		}

		protected Color getColor() {
			return GUIUtil.alphaColor(style.getLineColor(), style.getAlpha());
		}

		protected int getWidth() {
			return ApplicationOptionsPlugIn.options(context).get(
					ViewPanel.NODE_SIZE_KEY, 4);
		}
	}

	/**
	 * @param matchStyle
	 *            only one of the two layers needs this turned on
	 */
	private void initializeSourceLayer(final Layer sourceLayer,
			final Color defaultColor, final Color unknownColor,
			final Color matchedNonReferenceColor, final Color retiredColor,
			Color pendingInnerLineColor, Color integratedInnerLineColor,
			Color inconsistentInnerLineColor, boolean matchStyle) {
		sourceLayer.getBasicStyle().setEnabled(false);
		//This method may be called multiple times on a layer (as user changes
		//colour schemes). So clear the styles. [Jon Aquino 2004-02-18]
		removeAddedStyles(sourceLayer);
		//For attribute-panel sidebars [Jon Aquino 2004-02-06]
		sourceLayer.getBasicStyle().setLineColor(unknownColor);
		sourceLayer.getBasicStyle().setFillColor(unknownColor);
		sourceLayer.addStyle(checkShowingIncludedStatus(createResultStateStyle(
				ResultState.INTEGRATED, HighlightManager.instance(context)
						.getColourScheme().getIntegratedColour(),
				INCLUDED_OUTER_LINE_WIDTH, FUTURE_Block.TRUE)));
		sourceLayer.addStyle(createAdjustedStyle());
		sourceLayer.addStyle(checkShowingIncludedStatus(createResultStateStyle(
				ResultState.INCONSISTENT, FUTURE_GUIUtil.multiply(1, 1,
						INCONSISTENT_OUTER_LINE_ALPHA, HighlightManager
								.instance(context).getColourScheme()
								.getInconsistentColour()),
				INCLUDED_OUTER_LINE_WIDTH, new Block() {
					public Object yield() {
						return Boolean.valueOf(context.getBlackboard().get(
								ToolboxModel.SHOWING_INCONSISTENT_SEGMENTS_KEY,
								false));
					}
				})));
		sourceLayer
				.addStyle(checkShowingIncludedStatus(createConsistencyConfigurationStyle()));
		sourceLayer.addStyle(checkShowingIncludedStatus(new IntersectionStyle(
				context)));
		if (matchStyle) {
			sourceLayer.addStyle(new MatchStyle());
		}
		StyleChooser innerLineStyleChooser = createStyleChooser(defaultColor,
				unknownColor, matchedNonReferenceColor, retiredColor,
				pendingInnerLineColor, inconsistentInnerLineColor,
				integratedInnerLineColor);
		sourceLayer.addStyle(style(innerLineStyleChooser));
		sourceLayer.addStyle(new SplitNodeStyle.X(innerLineStyleChooser));
		sourceLayer.addStyle(new SourceFeatureEndpointStyle(null, true, null,
				innerLineStyleChooser, context));
		sourceLayer.addStyle(new SourceFeatureEndpointStyle(null, false, null,
				innerLineStyleChooser, context));
		sourceLayer.addStyle(new NodeConstraintStyle());
	}

	private FUTURE_DelegatingStyle style(final StyleChooser styleChooser) {
		return new FUTURE_DelegatingStyle() {
			public boolean isEnabled() {
				return true;
			}

			public void paint(Feature f, Graphics2D g, Viewport viewport)
					throws Exception {
				setStyle(chooseStyle(((SourceFeature) f).getRoadSegment()));
				super.paint(f, g, viewport);
			}

			private BasicStyle chooseStyle(SourceRoadSegment segment) {
				return styleChooser.style(segment);
			}
		};
	}

	private FUTURE_DelegatingStyle createConsistencyConfigurationStyle() {
		return new FUTURE_DelegatingStyle() {
			public void initialize(Layer layer) {
				setStyle(getConsistencyConfiguration().getStyle());
				super.initialize(layer);
			}

			public boolean isEnabled() {
				return true;
			}
		};
	}

	private void removeAddedStyles(Layer layer) {
		Collection stylesToRemove = new ArrayList();
		for (Iterator i = layer.getStyles().iterator(); i.hasNext();) {
			Style style = (Style) i.next();
			if (style == layer.getBasicStyle()) {
				continue;
			}
			if (style == layer.getVertexStyle()) {
				continue;
			}
			if (style == layer.getLabelStyle()) {
				continue;
			}
			stylesToRemove.add(style);
		}
		for (Iterator i = stylesToRemove.iterator(); i.hasNext();) {
			Style styleToRemove = (Style) i.next();
			layer.removeStyle(styleToRemove);
		}
	}

	private Style createAdjustedStyle() {
		BasicStyle adjustedStyle = new BasicStyle() {
			public void paint(Feature f, Graphics2D g, Viewport viewport)
					throws NoninvertibleTransformException {
				if (!((SourceFeature) f).getRoadSegment().isAdjusted()) {
					return;
				}
				super.paint(f, g, viewport);
			}
		};
		adjustedStyle.setRenderingFill(false);
		adjustedStyle.setRenderingLine(true);
		adjustedStyle.setLinePattern("1,1");
		adjustedStyle.setRenderingLinePattern(true);
		adjustedStyle.setLineColor(HighlightManager.instance(context)
				.getColourScheme().getAdjustedColour());
		adjustedStyle.setAlpha(HighlightManager.instance(context)
				.getColourScheme().getAdjustedColour().getAlpha());
		adjustedStyle.setLineWidth(INCLUDED_OUTER_LINE_WIDTH);
		//Hide class so this style will not be mistaken for the main
		// BasicStyle [Jon Aquino 12/17/2003]
		return hideClass(adjustedStyle);
	}

	private Style hideClass(Style style) {
		return new FUTURE_DelegatingStyle().setStyle(style);
	}

	private Style checkShowingIncludedStatus(Style style) {
		return new FUTURE_DelegatingStyle() {
			public boolean isEnabled() {
				return super.isEnabled()
						&& context.getBlackboard().get(
								ToolboxModel.SHOWING_INCLUDED_STATUS_KEY, true);
			}
		}.setStyle(style);
	}

	private Block test(final SourceState sourceState,
			final ResultState resultState) {
		return new Block() {
			public Object yield(Object segment) {
				if (sourceState != null
						&& ((SourceRoadSegment) segment).getState() != sourceState) {
					return Boolean.FALSE;
				}
				if (resultState != null
						&& ((SourceRoadSegment) segment).getResultState() != resultState) {
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}
		};
	}

	private StyleChooser createStyleChooser(final Color defaultColor,
			final Color unknownColor, final Color matchedNonReferenceColor,
			final Color retiredColor, final Color pendingInnerLineColour,
			final Color inconsistentInnerLineColour,
			final Color integratedInnerLineColour) {
		final OrderedMap testToStyleMap = (OrderedMap) CollectionUtil
				.createMap(OrderedMap.class, new Object[] {
						test(SourceState.UNKNOWN, null),
						new BasicStyle(defaultColor) {
							{
								setLineColor(unknownColor);
								setAlpha(unknownColor.getAlpha());
								setLineWidth(2);
							}
						},
						test(SourceState.MATCHED_NON_REFERENCE, null),
						new BasicStyle(defaultColor) {
							{
								setLineColor(matchedNonReferenceColor);
								setAlpha(matchedNonReferenceColor.getAlpha());
							}
						},
						test(SourceState.RETIRED, null),
						new BasicStyle(defaultColor) {
							{
								setAlpha(retiredColor.getAlpha());
								setLineColor(retiredColor);
								setLinePattern("3");
								setRenderingLinePattern(true);
							}
						},
						test(SourceState.STANDALONE, ResultState.PENDING),
						new BasicStyle(pendingInnerLineColour) {
							{
								setLinePattern(standaloneLinePattern);
								setRenderingLinePattern(standaloneLinePatternEnabled);
								setLineColor(pendingInnerLineColour);
								setAlpha(pendingInnerLineColour.getAlpha());
								setLineWidth(INCLUDED_INNER_LINE_WIDTH);
							}
						},
						test(SourceState.STANDALONE, ResultState.INCONSISTENT),
						new BasicStyle(inconsistentInnerLineColour) {
							{
								setLinePattern(standaloneLinePattern);
								setRenderingLinePattern(standaloneLinePatternEnabled);
								setLineColor(inconsistentInnerLineColour);
								setAlpha(inconsistentInnerLineColour.getAlpha());
								setLineWidth(INCLUDED_INNER_LINE_WIDTH);
							}
						},
						test(SourceState.STANDALONE, ResultState.INTEGRATED),
						new BasicStyle(integratedInnerLineColour) {
							{
								setLinePattern(standaloneLinePattern);
								setRenderingLinePattern(standaloneLinePatternEnabled);
								setLineColor(integratedInnerLineColour);
								setAlpha(integratedInnerLineColour.getAlpha());
								setLineWidth(INCLUDED_INNER_LINE_WIDTH);
							}
						},
						test(null, ResultState.PENDING),
						new BasicStyle(pendingInnerLineColour) {
							{
								setLineColor(pendingInnerLineColour);
								setAlpha(pendingInnerLineColour.getAlpha());
								setLineWidth(INCLUDED_INNER_LINE_WIDTH);
							}
						},
						test(null, ResultState.INCONSISTENT),
						new BasicStyle(inconsistentInnerLineColour) {
							{
								setLineColor(inconsistentInnerLineColour);
								setAlpha(inconsistentInnerLineColour.getAlpha());
								setLineWidth(INCLUDED_INNER_LINE_WIDTH);
							}
						}, test(null, ResultState.INTEGRATED),
						new BasicStyle(integratedInnerLineColour) {
							{
								setLineColor(integratedInnerLineColour);
								setAlpha(integratedInnerLineColour.getAlpha());
								setLineWidth(INCLUDED_INNER_LINE_WIDTH);
							}
						}, });
		return new StyleChooser() {
			public BasicStyle style(SourceRoadSegment segment) {
				for (Iterator i = testToStyleMap.keyList().iterator(); i
						.hasNext();) {
					Block test = (Block) i.next();
					if (test.yield(segment) == Boolean.TRUE) {
						return (BasicStyle) testToStyleMap.get(test);
					}
				}
				// Get here momentarily between setting the Source State and
				// setting the Result State. [Jon Aquino 2004-09-03]
				return new BasicStyle(defaultColor);
			}
		};
	}

	public static interface StyleChooser {
		public BasicStyle style(SourceRoadSegment segment);
	}

	private boolean standaloneLinePatternEnabled = true;

	private ColorThemingStyle createResultStateStyle(ResultState resultState,
			final Color colour, final int lineWidth, final Block enabledBlock) {
		return new ColorThemingStyle("ResultState",
				CollectionUtil.createMap(new Object[] { resultState.getName(),
						new BasicStyle(Color.black) {
							{
								setLineColor(colour);
								setAlpha(colour.getAlpha());
								setLineWidth(lineWidth);
							}
						} }), new BasicStyle(Color.black) {
					{
						setRenderingFill(false);
						setRenderingLine(false);
					}
				}) {
			public boolean isEnabled() {
				return ((Boolean) enabledBlock.yield()).booleanValue();
			}
		};
	}

	public static Layer markAsForConflation(Layer layer) {
		layer.getBlackboard().put(ROADMATCHER_LAYER_MARKER_KEY, true);
		return layer;
	}

	public ToolboxModel initialize(ConflationSession session) {
		this.session = session;
		deleteConflationLayers();
		try {
			OpenRoadMatcherSessionPlugIn.createInterpreter(session,
					layerManager, context).eval(
					(String) session.getBlackboard().get(
							OpenRoadMatcherSessionPlugIn.ON_SESSION_LOAD_KEY,
							""));
		} catch (EvalError e) {
			throw new RuntimeException(e);
		}
		sourceLayers[0] = createSourceLayer(session.getSourceNetwork(0));
		sourceLayers[1] = createSourceLayer(session.getSourceNetwork(1));
		updateStyles();
		layerManager().addCategory(SOURCE_CATEGORY, 0);
		layerManager().addLayer(SOURCE_CATEGORY, getSourceLayer(1));
		layerManager().addLayer(SOURCE_CATEGORY, getSourceLayer(0));
		sourceLayers[0].setEditable(true);
		sourceLayers[1].setEditable(true);
		initialized = true;
		return this;
	}

	private Layer createSourceLayer(final RoadNetwork sourceNetwork) {
		return markAsForConflation(new Layer("Network "
				+ sourceNetwork.getName(), Color.black,
				warnOnAdditionsAndDeletions(sourceNetwork
						.getFeatureCollection()), layerManager()));
	}

	private void deleteConflationLayers() {
		for (Iterator i = conflationLayers().iterator(); i.hasNext();) {
			Layer conflationLayer = (Layer) i.next();
			layerManager().remove(conflationLayer);
		}
	}

	private Collection conflationLayers() {
		return CollectionUtil.select(layerManager().getLayers(), new Block() {
			public Object yield(Object layer) {
				return Boolean.valueOf(((Layer) layer).getBlackboard().get(
						ROADMATCHER_LAYER_MARKER_KEY, false));
			}
		});
	}

	public Collection nonConflationLayers() {
		Collection layers = new ArrayList(layerManager().getLayers());
		layers.removeAll(conflationLayers());
		return layers;
	}

	private FeatureCollection warnOnAdditionsAndDeletions(
			FeatureCollection featureCollection) {
		return new FeatureCollectionWrapper(featureCollection) {
			public void add(Feature feature) {
				context.getWorkbench().getFrame().warnUser(
						"Add Feature not allowed on this layer");
			}

			public void addAll(Collection features) {
				context.getWorkbench().getFrame().warnUser(
						"Add Feature not allowed on this layer");
			}

			public Collection remove(Envelope env) {
				context.getWorkbench().getFrame().warnUser(
						"Remove Feature not allowed on this layer");
				return Collections.EMPTY_LIST;
			}

			public void removeAll(Collection features) {
				context.getWorkbench().getFrame().warnUser(
						"Remove Feature not allowed on this layer");
			}

			public void remove(Feature feature) {
				context.getWorkbench().getFrame().warnUser(
						"Remove Feature not allowed on this layer");
			}
		};
	}

	public void updateStyles() {
		boolean originallyFiringEvents = layerManager().isFiringEvents();
		layerManager().setFiringEvents(false);
		try {
			initializeSourceLayer(getSourceLayer(0), HighlightManager.instance(
					context).getColourScheme().getDefaultColour0(),
					HighlightManager.instance(context).getColourScheme()
							.getUnknownColour0(), HighlightManager.instance(
							context).getColourScheme()
							.getMatchedNonReferenceColour0(), HighlightManager
							.instance(context).getColourScheme()
							.getRetiredColour0(), HighlightManager.instance(
							context).getColourScheme()
							.getPendingInnerLineColour0(), HighlightManager
							.instance(context).getColourScheme()
							.getIntegratedInnerLineColour0(), HighlightManager
							.instance(context).getColourScheme()
							.getInconsistentInnerLineColour0(), true);
			initializeSourceLayer(getSourceLayer(1), HighlightManager.instance(
					context).getColourScheme().getDefaultColour1(),
					HighlightManager.instance(context).getColourScheme()
							.getUnknownColour1(), HighlightManager.instance(
							context).getColourScheme()
							.getMatchedNonReferenceColour1(), HighlightManager
							.instance(context).getColourScheme()
							.getRetiredColour1(), HighlightManager.instance(
							context).getColourScheme()
							.getPendingInnerLineColour1(), HighlightManager
							.instance(context).getColourScheme()
							.getIntegratedInnerLineColour1(), HighlightManager
							.instance(context).getColourScheme()
							.getInconsistentInnerLineColour1(), false);
		} finally {
			layerManager().setFiringEvents(originallyFiringEvents);
		}
	}

	private LayerManager layerManager() {
		return layerManager;
	}

	public boolean isInitialized() {
		return initialized;
	}

	private boolean initialized = false;

	private Layer[] sourceLayers = new Layer[2];

	private ConflationSession session;

	private String standaloneLinePattern = "30,1,1,1";

	public Layer other(Layer layer) {
		return getSourceLayer(0) != layer ? getSourceLayer(0)
				: getSourceLayer(1);
	}

	public ConflationSession getSession() {
		return session;
	}

	public Layer getSourceLayer(int i) {
		return sourceLayers[i];
	}

	public static ToolboxModel instance(PlugInContext context) {
		return instance(context.getLayerManager(), context
				.getWorkbenchContext());
	}

	public static ToolboxModel instance(WorkbenchContext context) {
		return instance(context.getLayerManager(), context);
	}

	public static ToolboxModel instance(LayerManager layerManager,
			WorkbenchContext context) {
		final String TOOLBOX_MODEL_KEY = ToolboxModel.class.getName()
				+ " - TOOLBOX MODEL";
		if (layerManager.getBlackboard().get(TOOLBOX_MODEL_KEY) == null) {
			layerManager.getBlackboard().put(TOOLBOX_MODEL_KEY,
					new ToolboxModel(layerManager, context));
		}
		return (ToolboxModel) layerManager.getBlackboard().get(
				TOOLBOX_MODEL_KEY);
	}

	public ConsistencyConfiguration getConsistencyConfiguration() {
		try {
			return (ConsistencyConfiguration) ((Class) ApplicationOptionsPlugIn
					.options(context).get(
							ConsistencyConfiguration.CURRENT_CLASS_KEY,
							ConsistencyConfiguration.DEFAULT_CLASS))
					.newInstance();
		} catch (InstantiationException e) {
			Assert.shouldNeverReachHere(e.toString());
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere(e.toString());
		}
		return null;
	}

	public void updateResultStates(final TaskMonitor monitor) {
		session.getRoadsEventFirer().deferFiringEvents(new Block() {
			public Object yield() {
				session.updateResultStates(monitor);
				return null;
			}
		});
		layerManager().fireFeaturesChanged(
				sourceLayers[0].getFeatureCollectionWrapper().getFeatures(),
				FeatureEventType.ATTRIBUTES_MODIFIED, sourceLayers[0]);
		layerManager().fireFeaturesChanged(
				sourceLayers[1].getFeatureCollectionWrapper().getFeatures(),
				FeatureEventType.ATTRIBUTES_MODIFIED, sourceLayers[1]);
		// Hmm ... theming layers don't get updated, probably because they
		// don't get an event fired here (e.g. appearance-changed). 
		// Oh well - not critical because they will get repainted when the
		// user zooms or pans. [Jon Aquino 2004-09-15]
	}

	/**
	 * Validates the input to the conflation session, and creates any layers
	 * required to show validation errors.
	 */
	public void validateInput() {
		createQALayers(session.getCoincidentSegments(), "Coincident Segments-",
				"Coincident Segments for ");
		createQALayers(session.getIllegalGeometries(), "Illegal Geometry-",
				"Illegal Geometry for ");
		createQALayers(session.getUnmatchedNodeConstraints(),
				"Unmatched Node Constraints-",
				"Unmatched Node Constraints for ");
	}

	public void createQALayers(FeatureCollection[] fc, String layerPrefix,
			String descriptionPrefix) {
		// Create 1 before 0 so the layers appear in the correct order
		// in the layer tree. [Jon Aquino 2004-09-27]
		if (fc[1].size() > 0)
			createQALayer(fc[1], 1, layerPrefix, descriptionPrefix);
		if (fc[0].size() > 0)
			createQALayer(fc[0], 0, layerPrefix, descriptionPrefix);		
	}

	/*
	 * private void createCoincidentSegmentsLayer(FeatureCollection fc, int
	 * index) { Layer lyr = markAsForConflation(new Layer(
	 * COINCIDENT_SEGMENTS_LAYER_PREFIX +
	 * session.getSourceNetwork(index).getName(), Color.red, fc,
	 * layerManager())); lyr.setDescription("Coincident Segments for " +
	 * session.getSourceNetwork(index).getName());
	 * lyr.getBasicStyle().setLineColor(Color.RED);
	 * lyr.getBasicStyle().setLineWidth(4);
	 * layerManager().addCategory(StandardCategoryNames.QA, 2);
	 * layerManager().addLayer(StandardCategoryNames.QA, lyr); }
	 */
	private void createQALayer(FeatureCollection fc, int index,
			String layerNamePrefix, String layerDescriptionPrefix) {
		final Layer lyr = markAsForConflation(new Layer(layerNamePrefix
				+ session.getSourceNetwork(index).getName(), Color.red, fc,
				layerManager()));
		lyr.setDescription(layerDescriptionPrefix
				+ session.getSourceNetwork(index).getName());
		lyr.getBasicStyle().setLineColor(Color.RED);
		lyr.getBasicStyle().setLineWidth(8);
		layerManager().deferFiringEvents(new Runnable(){
			public void run() {				
				lyr.setDrawingLast(true);
			}});		
		layerManager().addCategory(StandardCategoryNames.QA, 2);
		layerManager().addLayer(StandardCategoryNames.QA, lyr);
	}

	public LayerManager getLayerManager() {
		return layerManager;
	}

	public WorkbenchContext getContext() {
		return context;
	}

	public ToolboxModel setStandaloneLinePatternEnabled(
			boolean standaloneLinePatternEnabled) {
		this.standaloneLinePatternEnabled = standaloneLinePatternEnabled;
		return this;
	}

	public ToolboxModel setStandaloneLinePattern(String standaloneLinePattern) {
		this.standaloneLinePattern = standaloneLinePattern;
		return this;
	}
}