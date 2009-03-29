package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import java.util.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public abstract class SpecifyRoadFeaturesTool extends SpecifyFeaturesTool {

    private Cursor cursor;

    private boolean forLayer0;

    private boolean forLayer1;

    private GeometryFactory geometryFactory = new GeometryFactory();

    private GestureMode gestureMode;

    private Icon icon;

    private WorkbenchContext context;

    public SpecifyRoadFeaturesTool(boolean forLayer0, boolean forLayer1,
            String cursorImage, String iconImage, Color color,
            WorkbenchContext context, GestureMode gestureMode) {
        setColor(color);
        setViewClickBuffer(4);
        this.gestureMode = gestureMode;
        this.context = context;
        this.forLayer0 = forLayer0;
        this.forLayer1 = forLayer1;
        cursor = cursorImage != null ? createCursor(cursorImage)
                : iconImage != null ? FUTURE_GUIUtil.createCursorFromIcon(iconImage) : null;
        icon = iconImage != null ? createIcon(iconImage) : null;
    }

    public static Cursor createCursor(String cursorImage) {
        return createCursor(new ImageIcon(SpecifyRoadFeaturesTool.class
                .getResource("images/" + cursorImage)).getImage(), new Point(0,
                15));
    }

    protected Iterator candidateLayersIterator() {
        ArrayList candidateLayers = new ArrayList();
        if (forLayer0) {
            candidateLayers.add(toolboxModel().getSourceLayer(0));
        }
        if (forLayer1) {
            candidateLayers.add(toolboxModel().getSourceLayer(1));
        }

        return candidateLayers.iterator();
    }

    protected ToolboxModel toolboxModel() {
        return ToolboxModel.instance(context.getLayerManager(), context);
    }

    public Cursor getCursor() {
        return cursor;
    }

    public Icon getIcon() {
        return icon;
    }

    protected Coordinate getModelSource() {
        return gestureMode.getModelSource(super.getModelSource(), this);
    }

    protected Shape getShape(Point2D source, Point2D destination)
            throws Exception {
        return gestureMode.getShape(source, destination, this);
    }

    protected String sourceLayerDescription() {
        if (isForLayer0() && isForLayer1()) { return ""; }

        return (isForLayer0() ? toolboxModel().getSourceLayer(0).getName()
                : toolboxModel().getSourceLayer(1).getName())
                + " ";
    }

    public boolean isForLayer0() {
        return forLayer0;
    }

    public boolean isForLayer1() {
        return forLayer1;
    }

    protected Map layerToSpecifiedFeaturesMap()
            throws NoninvertibleTransformException {
        return gestureMode.layerToSpecifiedFeaturesMap(super
                .layerToSpecifiedFeaturesMap(), this);
    }

    protected Set specifiedFeatures() throws NoninvertibleTransformException {
        throw new UnsupportedOperationException(
                "If we need this function, we must modify it to return only those features that intersect the start-end line");
    }

    protected Collection specifiedFeatures(Collection layers)
            throws NoninvertibleTransformException {
        throw new UnsupportedOperationException(
                "If we need this function, we must modify it to return only those features that intersect the start-end line");
    }

    public static boolean checkConflationSessionStarted(WorkbenchContext context) {
        String warning = createConflationSessionMustBeStartedCheck(context)
                .check(null);
        if (warning != null) {
            context.getWorkbench().getFrame().warnUser(warning);

            return false;
        }

        return true;
    }

    public static ImageIcon createIcon(String iconImage) {
        return new ImageIcon(SpecifyRoadFeaturesTool.class
                .getResource("images/" + iconImage));
    }

    public static EnableCheck createConflationSessionMustBeStartedCheck(
            final WorkbenchContext context) {
        return new EnableCheck() {

            public String check(JComponent component) {
                return !ToolboxModel.instance(context.getLayerManager(),
                        context).isInitialized() ? ErrorMessages.specifyRoadFeaturesTool_noConflationSession
                        : null;
            }
        };
    }

    public static void warnUser(String message, WorkbenchContext context) {
        context.getWorkbench().getFrame().warnUser(message);
    }

    public static abstract class GestureMode {

        public static GestureMode BOX = new GestureMode() {

            public Coordinate getModelSource(Coordinate superGetModelSource,
                    SpecifyRoadFeaturesTool tool) {
                return superGetModelSource;
            }

            public Shape getShape(Point2D source, Point2D destination,
                    SpecifyRoadFeaturesTool tool) throws Exception {
                double minX = Math.min(source.getX(), destination.getX());
                double minY = Math.min(source.getY(), destination.getY());
                double maxX = Math.max(source.getX(), destination.getX());
                double maxY = Math.max(source.getY(), destination.getY());

                return new Rectangle.Double(minX, minY, maxX - minX, maxY
                        - minY);
            }

            public Map layerToSpecifiedFeaturesMap(
                    Map superLayerToSpecifiedFeaturesMap,
                    SpecifyRoadFeaturesTool tool) {
                return superLayerToSpecifiedFeaturesMap;
            }
        };

        public static GestureMode LINE = new GestureMode() {

            public Coordinate getModelSource(Coordinate superGetModelSource,
                    SpecifyRoadFeaturesTool tool) {
                return superGetModelSource;
            }

            public Shape getShape(Point2D source, Point2D destination,
                    SpecifyRoadFeaturesTool tool) {
                return new Line2D.Double(source, destination);
            }

            public Map layerToSpecifiedFeaturesMap(
                    Map superLayerToSpecifiedFeaturesMap,
                    SpecifyRoadFeaturesTool tool)
                    throws NoninvertibleTransformException {
                Geometry gesture = lineGesture(tool);
                for (Iterator i = superLayerToSpecifiedFeaturesMap.keySet()
                        .iterator(); i.hasNext();) {
                    Layer layer = (Layer) i.next();
                    Collection features = (Collection) superLayerToSpecifiedFeaturesMap
                            .get(layer);
                    for (Iterator j = features.iterator(); j.hasNext();) {
                        Feature feature = (Feature) j.next();
                        if (!feature.getGeometry().intersects(gesture)) {
                            j.remove();
                        }
                    }
                    if (features.isEmpty()) {
                        i.remove();
                    }
                }

                return superLayerToSpecifiedFeaturesMap;
            }

        };

        public static GestureMode POINT = new GestureMode() {

            public Coordinate getModelSource(Coordinate superGetModelSource,
                    SpecifyRoadFeaturesTool tool) {
                return tool.getModelDestination();
            }

            public Shape getShape(Point2D source, Point2D destination,
                    SpecifyRoadFeaturesTool tool) throws Exception {
                return null;
            }

            public Map layerToSpecifiedFeaturesMap(
                    Map superLayerToSpecifiedFeaturesMap,
                    SpecifyRoadFeaturesTool tool) {
                return superLayerToSpecifiedFeaturesMap;
            }
        };

        private GestureMode() {
        }

        public abstract Coordinate getModelSource(
                Coordinate superGetModelSource, SpecifyRoadFeaturesTool tool);

        public abstract Shape getShape(Point2D source, Point2D destination,
                SpecifyRoadFeaturesTool tool) throws Exception;

        public abstract Map layerToSpecifiedFeaturesMap(
                Map superLayerToSpecifiedFeaturesMap,
                SpecifyRoadFeaturesTool tool)
                throws NoninvertibleTransformException;
    }

    protected WorkbenchContext getContext() {
        return context;
    }

    protected static Geometry lineGesture(SpecifyRoadFeaturesTool tool)
            throws NoninvertibleTransformException {
        Geometry gesture = tool.wasClick() ? EnvelopeUtil.toGeometry(tool
                .getBoxInModelCoordinates()) : new GeometryFactory()
                .createLineString(new Coordinate[] { tool.getModelSource(),
                        tool.getModelDestination()});
        return gesture;
    }
}
