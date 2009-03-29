package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleFeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;
public class DefinePathsTool extends SpecifyNClosestRoadFeaturesTool {
    private int lastClickCount;
    public DefinePathsTool(WorkbenchContext context) {
        super(0, 2, true, true, null, "define-paths-tool-button.png",
                Color.cyan, context, GestureMode.LINE);
    }
    public void activate(final LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        int lineWidth0 = 18;
        int lineWidth1 = 12;
        installRenderer(0, lineWidth0, 80, 160, layerViewPanel,
                createEndShape(lineWidth0 * 2));
        installRenderer(1, lineWidth1, 80, 160, layerViewPanel,
                createEndShape(lineWidth1 * 2));
    }
    private Shape createEndShape(float extent) {
        GeneralPath endShape = new GeneralPath();
        endShape.moveTo(0, 0);
        endShape.lineTo(-extent * .5f, extent / 2);
        endShape.lineTo(-extent * .5f, -extent / 2);
        endShape.lineTo(0, 0);
        return endShape;
    }
    public void mouseReleased(MouseEvent e) {
        lastClickCount = e.getClickCount();
        super.mouseReleased(e);
    }
    private SortedSet candidateNextDirectedEdges(List path) {
        final double endAngle = endAngle(path);
        TreeSet candidateDirectedEdges = new TreeSet(new Comparator() {
            private double angle(DirectedEdge directedEdge) {
                return Angle.diff(endAngle, directedEdge.getAngle());
            }
            public int compare(Object directedEdge1, Object directedEdge2) {
                return toCompareResult(angle((DirectedEdge) directedEdge1)
                        - angle((DirectedEdge) directedEdge2));
            }
            private int toCompareResult(double d) {
                return d > 0 ? 1 : d < 0 ? -1 : 0;
            }
        });
        for (Iterator i = PathUtilities.endNode(path).getOutEdges().iterator(); i.hasNext();) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            if (features(path).contains(feature(directedEdge))) {
                continue;
            }
            candidateDirectedEdges.add(directedEdge);
        }
        final double ANGLE_TOLERANCE = Angle.toRadians(30);
        if (!candidateDirectedEdges.isEmpty()
                && Angle.diff(endAngle, ((DirectedEdge) candidateDirectedEdges
                        .iterator().next()).getAngle()) > ANGLE_TOLERANCE) {
            return new TreeSet(Collections.singleton(candidateDirectedEdges
                    .iterator().next()));
        }
        for (Iterator i = candidateDirectedEdges.iterator(); i.hasNext();) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            if (Angle.diff(endAngle, directedEdge.getAngle()) > ANGLE_TOLERANCE) {
                i.remove();
            }
        }
        return candidateDirectedEdges;
    }
    private Map closestOneIfClick(Map layerToSpecifiedFeaturesMap) {
        if (!wasClick()) {
            return layerToSpecifiedFeaturesMap;
        }
        if (layerToSpecifiedFeaturesMap.size() < 2) {
            return layerToSpecifiedFeaturesMap;
        }
        SourceFeature a = (SourceFeature) ((Collection) layerToSpecifiedFeaturesMap
                .values().toArray()[0]).iterator().next();
        SourceFeature b = (SourceFeature) ((Collection) layerToSpecifiedFeaturesMap
                .values().toArray()[1]).iterator().next();
        SourceFeature closest = distanceToClickPoint(a) < distanceToClickPoint(b) ? a
                : b;
        return Collections.singletonMap(toolboxModel().getSourceLayer(
                closest.getRoadSegment().getNetworkID()), Collections
                .singleton(closest));
    }
    private double distanceToClickPoint(SourceFeature f) {
        Assert.isTrue(wasClick());
        return f.getGeometry()
                .distance(
                        f.getGeometry().getFactory().createPoint(
                                getModelDestination()));
    }
    private double endAngle(List path) {
        return Angle.normalize(Math.PI
                + ((DirectedEdge) path.get(path.size() - 1)).getSym()
                        .getAngle());
    }
    private double startAngle(List path) {
        return Angle.normalize(Math.PI
                + ((DirectedEdge) path.get(0)).getAngle());
    }
    private Feature feature(DirectedEdge directedEdge) {
        return ((SourceRoadSegment) (directedEdge).getEdge()).getFeature();
    }
    private List features(Collection directedEdges) {
        return new ArrayList(CollectionUtil.collect(directedEdges, new Block() {
            public Object yield(Object directedEdge) {
                return feature((DirectedEdge) directedEdge);
            }
        }));
    }
    private Stack find(SourceFeature targetFeature, Stack path) {
        if (PathUtilities.startNode(path).getCoordinate().distance(
                PathUtilities.endNode(path).getCoordinate()) > 1.5 * PathUtilities.startNode(path)
                .getCoordinate().distance(
                        targetFeature.getGeometry().getCoordinate())) {
            return null;
        }
        //Allow the user to select any neighbouring road segment, regardless
        //of the angle it makes i.e. regardless of whether it is a candidate
        //[Jon Aquino 2004-03-02]
        for (Iterator i = PathUtilities.endNode(path).getOutEdges().iterator(); i.hasNext();) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            if (feature(directedEdge) == targetFeature) {
                path.push(directedEdge);
                return path;
            }
        }
        for (Iterator i = candidateNextDirectedEdges(path).iterator(); i
                .hasNext();) {
            DirectedEdge candidateNextDirectedEdge = (DirectedEdge) i.next();
            path.push(candidateNextDirectedEdge);
            if (find(targetFeature, path) != null) {
                return path;
            }
            path.pop();
        }
        return null;
    }
    protected void gestureFinished(Map layerToSpecifiedFeaturesMap)
            throws Exception {
        reportNothingToUndoYet();
        if (lastClickCount == 2) {
            PathUtilities.changePathUndoablyTo(new List[]{new ArrayList(), new ArrayList()},
                    getName(), getPanel());
            return;
        }
        if (!PathUtilities.checkPathRoadSegmentsInNetwork(getPanel(), getContext())) {
            return;
        }
        List[] newPath = newPath(layerToSpecifiedFeaturesMap);
        if (!new MatchPathsOperation().allRoadSegmentsUnknown(PathUtilities
                .pathMatch(newPath[0], newPath[1]))) {
            warnUser(ErrorMessages.definePathsTool_nonUnknownRoadSegments,
                    getContext());
            return;
        }
        PathUtilities.changePathUndoablyTo(newPath, getName(), getPanel());
    }
    private void installRenderer(final int i, final int lineWidth,
            final int lineAlpha, final int endShapeAlpha,
            final LayerViewPanel layerViewPanel, final Shape endShape) {
        //Cache the toolbox model for later, when the renderer is still
        //alive but the LayerViewPanel is not in the active internal frame.
        //[Jon Aquino 2004-03-16]
        final ToolboxModel toolboxModel = toolboxModel();
        layerViewPanel.getRenderingManager().putAboveLayerables(PathUtilities.contentID(i),
                new Renderer.Factory() {
                    public Renderer create() {
                        return new SimpleFeatureCollectionRenderer(
                                PathUtilities.contentID(i), layerViewPanel) {
                            Stroke endShapeStroke = new BasicStroke();
                            Color color;
                            {
                                setStyles(Collections.singleton(createStyle(
                                        lineWidth, new Block() {
                                            public Object yield() {
                                                return color;
                                            }
                                        })));
                            }
                            protected void paint(Graphics2D g) throws Exception {
                                if (!toolboxModel.isInitialized()) {
                                    return;
                                }
                                if (PathUtilities.path(i, layerViewPanel).isEmpty()) {
                                    return;
                                }
                                setLayerToFeaturesMap(Collections.singletonMap(
                                        toolboxModel.getSourceLayer(i),
                                        features(PathUtilities.path(i, layerViewPanel))));
                                color = GUIUtil.alphaColor(toolboxModel
                                        .getSourceLayer(i).getBasicStyle()
                                        .getLineColor(), lineAlpha);
                                g.setColor(GUIUtil.alphaColor(color,
                                        endShapeAlpha));
                                g.setStroke(endShapeStroke);
                                paintEndShape(
                                        PathUtilities.startNode(PathUtilities.path(i, layerViewPanel)),
                                        startAngle(PathUtilities.path(i, layerViewPanel)), g,
                                        endShape);
                                paintEndShape(PathUtilities.endNode(PathUtilities.path(i, layerViewPanel)),
                                        endAngle(PathUtilities.path(i, layerViewPanel)), g,
                                        endShape);
                                super.paint(g);
                            }
                        };
                    }
                });
    }
    private Style createStyle(final int lineWidth, final Block colorGetter) {
        return new Style() {
            Stroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND);
            public void paint(Feature f, Graphics2D g, Viewport viewport)
                    throws Exception {
                StyleUtil.paint(f.getGeometry(), g, viewport, false, null,
                        null, true, stroke, (Color) colorGetter.yield());
            }
            public void initialize(Layer layer) {
            }
            public Object clone() {
                throw new UnsupportedOperationException();
            }
            public void setEnabled(boolean enabled) {
                throw new UnsupportedOperationException();
            }
            public boolean isEnabled() {
                return true;
            }
        };
    }
    private void paintEndShape(RoadNode node, double angle, Graphics2D g,
            Shape endShape) throws NoninvertibleTransformException {
        if (!getPanel().getViewport().getEnvelopeInModelCoordinates().contains(
                node.getCoordinate())) {
            return;
        }
        Point2D p = getPanel().getViewport().toViewPoint(node.getCoordinate());
        g.translate(p.getX(), p.getY());
        g.rotate(-angle);
        try {
            g.fill(endShape);
        } finally {
            g.rotate(angle);
            g.translate(-p.getX(), -p.getY());
        }
    }
    private List[] newPath(Map layerToSpecifiedFeaturesMap) {
        List[] newPaths = new List[]{new ArrayList(PathUtilities.path(0, getPanel())),
                new ArrayList(PathUtilities.path(1, getPanel()))};
        Collection features = new ArrayList();
        //Take first road segment from each network and ignore any others
        //[Jon Aquino 2004-02-27]
        for (Iterator i = closestOneIfClick(layerToSpecifiedFeaturesMap)
                .values().iterator(); i.hasNext();) {
            Collection featuresForLayer = (Collection) i.next();
            features.addAll(featuresForLayer);
        }
        for (Iterator i = features.iterator(); i.hasNext();) {
            SourceFeature feature = (SourceFeature) i.next();
            updatePath(feature, newPaths);
        }
        return newPaths;
    }
    private List pathForFirst(Collection features, List[] paths) {
        return paths[((SourceFeature) features.iterator().next())
                .getRoadSegment().getNetworkID()];
    }
    private Stack createStack(Collection collection) {
        Stack newPath = new Stack();
        newPath.addAll(collection);
        return newPath;
    }
    private void updatePath(List path, SourceFeature targetFeature) {
        if (path.isEmpty()) {
            path.add(targetFeature.getRoadSegment().getDirEdge(0));
            return;
        }
        if (features(path).contains(targetFeature)) {
            shorter(
                    path.subList(0, features(path).indexOf(targetFeature)),
                    path.subList(features(path).indexOf(targetFeature) + 1,
                            path.size())).clear();
            return;
        }
        List newPath = find(targetFeature, createStack(path));
        if (newPath == null) {
            newPath = find(targetFeature, createStack(PathUtilities.reverse(path)));
        }
        if (newPath == null) {
            warnUser(FUTURE_StringUtil.substitute(
                    ErrorMessages.definePathsTool_pathRouteCannotBeDetermined,
                    new Object[]{targetFeature.getRoadSegment().getNetwork()
                            .getName()}), getContext());
            return;
        }
        path.clear();
        path.addAll(newPath);
    }
    private List shorter(List path0, List path1) {
        return length(path0) < length(path1) ? path0 : path1;
    }
    private double length(List path) {
        double length = 0;
        for (Iterator i = path.iterator(); i.hasNext();) {
            DirectedEdge directedEdge = (DirectedEdge) i.next();
            length += ((SourceRoadSegment) directedEdge.getEdge()).getLine()
                    .getLength();
        }
        return length;
    }
    private void updatePath(SourceFeature targetFeature, List[] paths) {
        updatePath(paths[targetFeature.getRoadSegment().getNetworkID()],
                targetFeature);
    }
}