package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class CreateThemingLayerPlugIn extends AbstractPlugIn {

	public boolean execute(final PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		MultiInputDialog dialog = new MultiInputDialog(context
				.getWorkbenchFrame(), getName(), true);
		final String FIELD_NAME = "For:";
		dialog.addLayerComboBox(FIELD_NAME, ToolboxModel.instance(context)
				.getSourceLayer(0),
				"Layer for which to create a theming layer",
				FUTURE_CollectionUtil.list(ToolboxModel.instance(context)
						.getSourceLayer(0), ToolboxModel.instance(context)
						.getSourceLayer(1)));
		dialog.setVisible(true);
		if (!dialog.wasOKPressed()) {
			return false;
		}
		createThemingLayer(((RoadNetworkFeatureCollection) dialog.getLayer(
				FIELD_NAME).getFeatureCollectionWrapper().getUltimateWrappee())
				.getNetwork(), new Color(0, 204, 255, 105), context
				.getLayerManager());
		return true;
	}

	private void createThemingLayer(RoadNetwork network, Color color,
			LayerManager layerManager) {
		createThemingLayer(network.getName() + " Theming", network, color,
				layerManager);
	}

	public static Layer createThemingLayer(String name,
			final String networkName, final String attributeName,
			final int width, final String pattern,
			final ConflationSession session, final LayerManager layerManager,
			WorkbenchContext context, final Value[] values) {
		Layer layer = createThemingLayer(name, session
				.getSourceNetwork(networkName), values[0].colour, layerManager);
		layer.getBasicStyle().setEnabled(false);
		ColorThemingStyle.get(layer).setAttributeName(attributeName);
		ColorThemingStyle.get(layer).setAttributeValueToBasicStyleMap(
				createAttributeValueToBasicStyleMap(width, pattern, values));
		ColorThemingStyle.get(layer).setDefaultStyle(new BasicStyle() {
			{
				setRenderingFill(false);
				setRenderingLine(false);
			}
		});
		ColorThemingStyle.get(layer).setEnabled(true);
		layer.fireAppearanceChanged();
		return layer;
	}

	private static Map createAttributeValueToBasicStyleMap(final int width,
			final String pattern, final Value[] values) {
		Map attributeValueToBasicStyleMap = new HashMap();
		for (int i = 0; i < values.length; i++) {
			final Color colour = values[i].colour;
			attributeValueToBasicStyleMap.put(values[i].value,
					new BasicStyle() {
						{
							setAlpha(colour.getAlpha());
							setLineWidth(width);
							setLineColor(new Color(colour.getRed(), colour
									.getGreen(), colour.getBlue()));
							if (pattern != null) {
								setRenderingLinePattern(true);
								setLinePattern(pattern);
							}
						}
					});
		}
		return attributeValueToBasicStyleMap;
	}

	public static class Value {
		private Object value;

		private Color colour;

		public Value(Object value, Color colour) {
			this.value = value;
			this.colour = colour;
		}
	}

	private static Color defaultColour(String networkName,
			ConflationSession session, WorkbenchContext context) {
		return session.getSourceNetwork(networkName).getID() == 0 ? HighlightManager
				.instance(context).getColourScheme().getDefaultColour0()
				: HighlightManager.instance(context).getColourScheme()
						.getDefaultColour1();
	}

	public static Layer createLabelledThemingLayer(String name,
			final String networkName, final String attributeName,
			final int height, final String verticalAlignment,
			final ConflationSession session, final LayerManager layerManager,
			final WorkbenchContext context, final Value[] values) {
		final Layer layer = createThemingLayer(name, session
				.getSourceNetwork(networkName), values == null ? defaultColour(
				networkName, session, context) : values[0].colour, layerManager);
		// Defer layer events (1) to reduce the number of events (just
		// do one at the end) (2) to avoid NullPointerException as we replace
		// the LabelStyle. [Jon Aquino 2004-09-13]
		layerManager.deferFiringEvents(new Runnable() {
			public void run() {
				layer.getBasicStyle().setEnabled(false);
				layer.removeStyle(layer.getLabelStyle());
				layer.addStyle(values == null ? (LabelStyle) new LabelStyle() {
					{
						setColor(defaultColour(networkName, session, context));
					}
				} : new LabelStyle() {
					Map valueToColourMap = new HashMap();
					{
						for (int i = 0; i < values.length; i++) {
							valueToColourMap.put(values[i].value,
									values[i].colour);
						}
					}

					public void paint(Feature f, Graphics2D g, Viewport viewport)
							throws NoninvertibleTransformException {
						Color colour = (Color) valueToColourMap
								.get(ColorThemingStyle.trimIfString(f
										.getAttribute(attributeName)));
						if (colour == null) {
							return;
						}
						setColor(colour);
						super.paint(f, g, viewport);
					}
				});
				layer.getLabelStyle().setAttribute(attributeName);
				layer.getLabelStyle().setHeight(height);
				layer.getLabelStyle().setHidingOverlappingLabels(false);
				layer.getLabelStyle().setVerticalAlignment(verticalAlignment);
				layer.getLabelStyle().setEnabled(true);
			}
		});
		layer.fireAppearanceChanged();
		return layer;
	}

	public static ScaledStyle scale(double minScale, double maxScale,
			Style style, Layer layer) {
		// Rather than remove the style, simply disable it, so that the Change
		// Style Dialog can still control it. Hopefully the user won't enable
		// it! Even so, it will be reset back to normal when the session is next
		// opened. [Jon Aquino 2004-12-15]
		// Hmm - the Change Style Dialog creates a clone of the style, so the
		// ScaledStyleWrapper doesn't get the changes. Oh well - it's still a
		// good idea to keep the original style in the layer (though disabled)
		// to avoid NullPointerExceptions. [Jon Aquino 2004-12-15]
		style.setEnabled(false);
		ScaledStyle scaledStyle = new ScaledStyleWrapper(style).setMinScale(
				minScale).setMaxScale(maxScale);
		layer.addStyle(scaledStyle);
		return scaledStyle;
	}

	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.TOOLS_MENU_NAME },
						getName(),
						false,
						null,
						new MultiEnableCheck()
								.add(
										context
												.getCheckFactory()
												.createWindowWithLayerViewPanelMustBeActiveCheck())
								.add(
										SpecifyRoadFeaturesTool
												.createConflationSessionMustBeStartedCheck(context
														.getWorkbenchContext())));
	}

	private static Layer createThemingLayer(String name,
			RoadNetwork sourceNetwork, final Color colour,
			LayerManager layerManager) {
		final Layer layer = new Layer(name, colour,
				new RoadNetworkFeatureCollection(sourceNetwork, sourceNetwork
						.getFeatureCollection().getFeatureSchema()),
				layerManager);
		layerManager.deferFiringEvents(new Runnable() {
			public void run() {
				// Make the layer editable. Otherwise, when you select a segment
				// (and both the network layer and the theming layer will be
				// selected) then try to Insert Vertex, you'll get "No selected
				// editable items here." [Jon Aquino 2004-09-13]
				layer.setEditable(true);
				layer.getBasicStyle().setLineColor(
						new Color(colour.getRed(), colour.getGreen(), colour
								.getBlue()));
				// Set fill colour so that the layer icon looks good
				// (instead of a drab grey) [Jon Aquino 2004-09-13]
				layer.getBasicStyle().setFillColor(
						layer.getBasicStyle().getLineColor());
				layer.getBasicStyle().setAlpha(colour.getAlpha());
				layer.getBasicStyle().setLineWidth(12);
				layer.setDrawingLast(true);
			}
		});
		ToolboxModel.markAsForConflation(layer);
		// Create new category so theming layers appear above network layers.
		// [Jon Aquino 2004-09-13]
		layerManager.addCategory(CATEGORY_NAME, 0);
		return layerManager.addLayer(CATEGORY_NAME, layer);
	}

	public static final String CATEGORY_NAME = "Theming";
}