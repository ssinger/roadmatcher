/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
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

package com.vividsolutions.jcs.debug;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.io.*;

/**
 * Allows capturing features during debugging
 * and outputting them afterwards.
 * <p>
 * To enable the debugging functions, the Java system property <code>debug</code>
 * must be set to <code>on</code> in the java command line, using the following
 * option:
 * <pre>
 *     -Ddebug=on
 * </pre>
 */
public class DebugFeature {

  public static final String MESG_ATTR_NAME = "MSG";

  private static final GeometryFactory fact = new GeometryFactory();

  private static Map tags = new HashMap();

  public static FeatureSchema createGeometryMsgFeatureSchema()
  {
    FeatureSchema featureSchema = new FeatureSchema();
    featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    featureSchema.addAttribute(MESG_ATTR_NAME, AttributeType.STRING);
    return featureSchema;
  }
  private static FeatureDataset getDebugFeatureDataset(Object tag)
  {
    if (tags.containsKey(tag))
        return (FeatureDataset) tags.get(tag);
    FeatureDataset fc = new FeatureDataset(createGeometryMsgFeatureSchema());
    tags.put(tag, fc);
    return fc;
  }

  public DebugFeature() {
  }

  public static void addLineSegment(Object tag, Coordinate p0, Coordinate p1, String msg)
  {
    if (! Debug.isDebugging()) return;
    FeatureDataset fd = getDebugFeatureDataset(tag);
    fd.add(createLineSegmentFeature(fd.getFeatureSchema(), p0, p1, msg));
  }

  public static Feature createLineSegmentFeature(FeatureSchema fs, Coordinate p0, Coordinate p1, String msg)
  {
    Feature feature = new BasicFeature(fs);
    LineString lineSeg = fact.createLineString(new Coordinate[] { p0, p1 });
    feature.setGeometry(lineSeg);
    feature.setAttribute(MESG_ATTR_NAME, msg);
    return feature;
  }

  public static void add(Object tag, Geometry geom, String msg)
  {
    if (! Debug.isDebugging()) return;
    FeatureDataset fd = getDebugFeatureDataset(tag);
    Feature feature = new BasicFeature(fd.getFeatureSchema());
    feature.setGeometry(geom);
    feature.setAttribute(MESG_ATTR_NAME, msg);
    fd.add(feature);
  }

  public static void saveFeatures(Object tag, String filename)
  {
    if (! Debug.isDebugging()) return;
    try {
      //saveShapefile(getDebugFeatureDataset(tag), filename);
      saveJMLfile(getDebugFeatureDataset(tag), filename);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  private static void saveShapefile(FeatureCollection fc, String filename)
      throws Exception
  {
    ShapefileWriter writer = new ShapefileWriter();
    DriverProperties dp = new DriverProperties();
    dp.set("File", filename);
    writer.write(fc, dp);
  }

  private static void saveJMLfile(FeatureCollection fc, String filename)
      throws Exception
  {
    JMLWriter writer = new JMLWriter();
    DriverProperties dp = new DriverProperties();
    dp.set("File", filename);
    writer.write(fc, dp);
  }

}
