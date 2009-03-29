package com.vividsolutions.jcs.jump;

/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.Viewport;


public class FUTURE_DotLineStringEndpointStyle extends FUTURE_LineStringEndpointStyle {
    private int width = 4;
    private Stroke stroke = new BasicStroke(2);

    protected FUTURE_DotLineStringEndpointStyle(String name, boolean start, String iconFile) {
        super(name, null, start);
        Assert.isTrue(iconFile == null, "To do: create Icon from filename");
    }
    
    protected Color getColor() {
        return lineColorWithAlpha;
    }

    protected void paint(Point2D terminal, Point2D next, Viewport viewport,
        Graphics2D graphics) throws NoninvertibleTransformException {
        graphics.setColor(getColor());
        graphics.setStroke(stroke);
        graphics.fill(toShape(terminal));
    }

    private Shape toShape(Point2D viewPoint) {
        return new Rectangle2D.Double(viewPoint.getX() - (getWidth() / 2d),
            viewPoint.getY() - (getWidth() / 2d), getWidth(), getWidth());
    }

    protected int getWidth() {
        return width;
    }

    public static class Start extends FUTURE_DotLineStringEndpointStyle {
        public Start() {
            super("Start-Dot", true, null);
        }
    }

    public static class End extends FUTURE_DotLineStringEndpointStyle {
        public End() {
            super("End-Dot", false, null);
        }
    }    
}
