package com.vividsolutions.jcs.plugin.conflate.roads;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.warp.AffineTransform;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.SwingUtilities;
public abstract class WarpLocallyAdjustmentMethod extends
        SimpleAdjustmentMethod {
    protected LineString warp(final LineString line, final Mode mode,
            final Coordinate newTerminalLocation, double warpZoneExtent) {
        return warp(line, mode, newTerminalLocation, pivot(line, mode,
                warpZoneExtent));
    }
    protected LineString warp(final LineString line, final Mode mode,
            final Coordinate newTerminalLocation, Coordinate pivot) {
        Coordinate[] newCoordinates = new Coordinate[line.getNumPoints()];
        int j = 0;
        AffineTransform warp = new AffineTransform(pivot, pivot, mode
                .coordinate(line), newTerminalLocation);
        boolean warping = true;
        for (Iterator i = mode.iterator(line); i.hasNext();) {
            Coordinate oldCoordinate = (Coordinate) i.next();
            if (warping && oldCoordinate.equals(pivot)) {
                warping = false;
            }
            //Set the terminal coordinates exactly [Jon Aquino 12/16/2003]
            newCoordinates[j] = j == 0 ? (Coordinate) newTerminalLocation
                    .clone() : !i.hasNext() ? (Coordinate) oldCoordinate
                    .clone() : warping ? warp.transform(oldCoordinate)
                    : (Coordinate) oldCoordinate.clone();
            j++;
        }
        return mode.createLineString(newCoordinates, line.getFactory());
    }
    private Coordinate pivot(LineString line, Mode mode, double warpZoneExtent) {
        Envelope warpZone = EnvelopeUtil.expand(new Envelope(mode
                .coordinate(line)), warpZoneExtent);
        for (Iterator i = mode.iterator(line); i.hasNext();) {
            Coordinate coordinate = (Coordinate) i.next();
            if (!warpZone.contains(coordinate) || !i.hasNext()) {
                return coordinate;
            }
        }
        Assert.shouldNeverReachHere();
        return null;
    }
    public static Mode toMode(Terminal terminal) {
        return terminal == Terminal.START ? Mode.START : Mode.END;
    }
    public static class ViewBasedWarpZone extends WarpLocallyAdjustmentMethod {
        private static final double WARP_ZONE_EXTENT_IN_INCHES = 1;
        private double warpZoneExtentInPixels = -1;
        public LineString adjust(LineString line, SourceRoadSegment segment, Terminal terminal,
                Coordinate newTerminalLocation, LayerViewPanel panel) {
            return warp(line, toMode(terminal), newTerminalLocation,
                    warpZoneExtentInPixels() / panel.getViewport().getScale());
        }
        private double warpZoneExtentInPixels() {
            if (warpZoneExtentInPixels == -1) {
                warpZoneExtentInPixels = WARP_ZONE_EXTENT_IN_INCHES
                        * Toolkit.getDefaultToolkit().getScreenResolution();
            }
            return warpZoneExtentInPixels;
        }
    }
    public static class ModelBasedWarpZone extends WarpLocallyAdjustmentMethod implements Serializable {
		private ConflationSession session;
		public LineString adjust(LineString line, SourceRoadSegment segment, Terminal terminal,
                Coordinate newTerminalLocation, LayerViewPanel panel) {
			session = segment.getNetwork().getSession();
            return warp(line, toMode(terminal), newTerminalLocation,
                    getWarpZoneExtent());
        }
		public double getWarpZoneExtent() {
			return AutoAdjustOptions.get(session).getSegmentAdjustmentLength();
		}        
    }
    public static abstract class Mode {
        public static Mode START = new Mode() {
            public LineString createLineString(Coordinate[] coordinates,
                    GeometryFactory factory) {
                return factory.createLineString(coordinates);
            }
            public Coordinate coordinate(LineString line) {
                return line.getCoordinateN(0);
            }
            public Iterator iterator(LineString line) {
                return Arrays.asList(line.getCoordinates()).iterator();
            }
        };
        public static Mode END = new Mode() {
            public Coordinate coordinate(LineString line) {
                return line.getCoordinateN(line.getNumPoints() - 1);
            }
            public Iterator iterator(LineString line) {
                return CollectionUtil.reverse(
                        new ArrayList(Arrays.asList(line.getCoordinates())))
                        .iterator();
            }
            public LineString createLineString(Coordinate[] coordinates,
                    GeometryFactory factory) {
                return factory.createLineString((Coordinate[]) CollectionUtil
                        .reverse(new ArrayList(Arrays.asList(coordinates)))
                        .toArray(new Coordinate[coordinates.length]));
            }
        };
        private Mode() {
        }
        public abstract Coordinate coordinate(LineString line);
        public abstract Iterator iterator(LineString line);
        public abstract LineString createLineString(Coordinate[] coordinates,
                GeometryFactory factory);
    }
}