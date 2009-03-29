package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringEndpointStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class GenerateAdjustmentVectorsLayerPlugIn extends AbstractPlugIn {
	public void initialize(PlugInContext context) throws Exception {
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

	public boolean execute(PlugInContext context) throws Exception {
		//Not truly undoable, but mark this plugin as undoable because
		//we're just adding a (reporting) layer -- better than truncating the
		//undo history [Jon Aquino 2004-05-11]
		reportNothingToUndoYet(context);
		final Layer layer = context.getLayerManager().addLayer(
				"Output",
				ToolboxModel.markAsForConflation(new Layer(
						"Adjustment Vectors", Color.CYAN,
						createFeatureCollection(ToolboxModel.instance(context)
								.getSession()), context.getLayerManager())));
		layer.getBasicStyle().setEnabled(false);
		layer.addStyle(enable(new ColorThemingStyle("Source", CollectionUtil
				.createMap(new Object[] {
						ToolboxModel.instance(context).getSession()
								.getSourceNetwork(0).getName(),
						createStyle(HighlightManager.instance(
								context.getWorkbenchContext())
								.getColourScheme().getDefaultColour0()),
						ToolboxModel.instance(context).getSession()
								.getSourceNetwork(1).getName(),
						createStyle(HighlightManager.instance(
								context.getWorkbenchContext())
								.getColourScheme().getDefaultColour1()) }),
				new BasicStyle())));
		layer.setDrawingLast(true);
		layer.addStyle(new ArrowLineStringEndpointStyle.NarrowSolidEnd() {
			public void paint(Feature f, Graphics2D g, Viewport viewport)
					throws Exception {
				lineColorWithAlpha = ((BasicStyle) ColorThemingStyle.get(layer)
						.getAttributeValueToBasicStyleMap().get(
								f.getAttribute("Source"))).getLineColor();
				super.paint(f, g, viewport);
			}
		});
		return true;
	}

	private Style enable(ColorThemingStyle style) {
		style.setEnabled(true);
		return style;
	}

	private BasicStyle createStyle(Color color) {
		return new BasicStyle(color).setLinePattern("3")
				.setRenderingLinePattern(true);
	}

	private FeatureCollection createFeatureCollection(ConflationSession session) {
		FeatureSchema featureSchema = new FeatureSchema();
		featureSchema.addAttribute("Geometry", AttributeType.GEOMETRY);
		featureSchema.addAttribute("Source", AttributeType.STRING);
		featureSchema.addAttribute("Length", AttributeType.DOUBLE);
		featureSchema.addAttribute("AdjAngDel", AttributeType.DOUBLE);
		FeatureCollection featureCollection = new FeatureDataset(featureSchema);
		for (Iterator i = session.getRoadSegments().iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			if (!roadSegment.isAdjusted()) {
				continue;
			}
			addVector(0, 0, roadSegment, featureCollection);
			addVector(roadSegment.getLine().getNumPoints() - 1, roadSegment
					.getApparentLine().getNumPoints() - 1, roadSegment,
					featureCollection);
		}
		return featureCollection;
	}

	private void addVector(int i, int j, SourceRoadSegment roadSegment,
			FeatureCollection featureCollection) {
		addIfNotNull(createFeature(roadSegment.getLine().getCoordinateN(i),
				roadSegment.getApparentLine().getCoordinateN(j), roadSegment
						.getAdjustmentAngleDelta(), featureCollection
						.getFeatureSchema(),
				roadSegment.getLine().getFactory(), roadSegment.getNetwork()),
				featureCollection);
	}

	private void addVector(int i, SourceRoadSegment roadSegment,
			FeatureCollection featureCollection) {
		addIfNotNull(createFeature(roadSegment.getLine().getCoordinateN(i),
				roadSegment.getApparentLine().getCoordinateN(i), roadSegment
						.getAdjustmentAngleDelta(), featureCollection
						.getFeatureSchema(),
				roadSegment.getLine().getFactory(), roadSegment.getNetwork()),
				featureCollection);
	}

	private void addIfNotNull(Feature feature,
			FeatureCollection featureCollection) {
		if (feature == null) {
			return;
		}
		featureCollection.add(feature);
	}

	private Feature createFeature(Coordinate start, Coordinate end,
			double adjustmentAngleDelta, FeatureSchema featureSchema,
			GeometryFactory factory, RoadNetwork network) {
		if (start.equals(end)) {
			return null;
		}
		Feature feature = new BasicFeature(featureSchema);
		feature.setAttribute("Geometry", factory
				.createLineString(new Coordinate[] { start, end }));
		feature.setAttribute("Source", network.getName());
		feature.setAttribute("Length", new Double(start.distance(end)));
		feature.setAttribute("AdjAngDel", new Double(adjustmentAngleDelta));
		return feature;
	}
}