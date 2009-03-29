package com.vividsolutions.jcs.jump;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.Fmt;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.AbstractFeatureTextWriter;

public class FUTURE_InstallStandardFeatureTextWritersPlugIn {
	public static void fix(WorkbenchContext context) {
		for (Iterator i = context.getFeatureTextWriterRegistry().iterator(); i
				.hasNext();) {
			AbstractFeatureTextWriter writer = (AbstractFeatureTextWriter) i
					.next();
			if (writer.getShortDescription().equals("CL")) {
				i.remove();
			}
		}
		context.getFeatureTextWriterRegistry().register(
				new AbstractFeatureTextWriter(false, "CL", "Coordinate List") {
					public String write(Feature feature) {
						StringBuffer s = new StringBuffer();
						String className = StringUtil
								.classNameWithoutQualifiers(feature
										.getGeometry().getClass().getName());
						s.append(className + "\n");
						Coordinate[] coordinates = feature.getGeometry()
								.getCoordinates();
						for (int i = 0; i < coordinates.length; i++) {
							s.append("[" + Fmt.fmt(i, 10) + "] ");
							s.append(coordinates[i].x
									+ ", "
									+ coordinates[i].y
									+ (Double.isNaN(coordinates[i].z) ? ""
											: ", " + coordinates[i].z) + "\n");
						}
						return s.toString().trim();
					}
				});
	}
}