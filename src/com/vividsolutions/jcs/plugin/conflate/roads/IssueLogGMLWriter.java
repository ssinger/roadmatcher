package com.vividsolutions.jcs.plugin.conflate.roads;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.vividsolutions.jcs.plugin.issuelog.IssueLog;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.GMLGeometryWriter;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class IssueLogGMLWriter {
	private GMLGeometryWriter gmlGeometryWriter = new GMLGeometryWriter();

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public String gml(FeatureCollection issues) {
		StringBuffer buffer = new StringBuffer();
		appendHeader(buffer);
		for (Iterator i = issues.iterator(); i.hasNext();) {
			Feature issue = (Feature) i.next();
			append(issue, buffer);
		}
		appendFooter(buffer);
		return buffer.toString();
	}

	private void appendHeader(StringBuffer buffer) {
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<rm:issueLog \n");
		buffer
				.append("	xmlns:rm=\"http://www.vividsolutions.com/roadMatcher\"\n");
		buffer.append("    	xmlns:gml=\"http://www.opengis.net/gml\"\n");
		buffer
				.append("    	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n");
		buffer
				.append("	xsi:schemaLocation=\"http://www.vividsolutions.com/roadMatcher issueLog.xsd\">\n");
		buffer.append("    <gml:boundedBy>\n");
		buffer.append("        <gml:null/>\n");
		buffer.append("    </gml:boundedBy>\n");
	}

	private void append(Feature issue, StringBuffer buffer) {
		buffer.append("    <gml:featureMember>\n");
		buffer.append("        <rm:issue>\n");
		buffer.append("            <rm:Type>"
				+ issue.getAttribute(IssueLog.AttributeNames.TYPE)
				+ "</rm:Type>\n");
		buffer.append("            <rm:Desc>"
				+ GUIUtil.escapeHTML((String) issue
						.getAttribute(IssueLog.AttributeNames.DESCRIPTION),
						false, false) + "</rm:Desc>\n");
		buffer.append("            <rm:Comment>"
				+ GUIUtil.escapeHTML((String) issue
						.getAttribute(IssueLog.AttributeNames.COMMENT), false,
						false) + "</rm:Comment>\n");
		buffer.append("            <rm:Status>"
				+ issue.getAttribute(IssueLog.AttributeNames.STATUS)
				+ "</rm:Status>\n");
		buffer.append("            <rm:User>"
				+ issue.getAttribute(IssueLog.AttributeNames.USER_NAME)
				+ "</rm:User>\n");
		buffer.append("            <rm:CreateDate>"
				+ string((Date) issue
						.getAttribute(IssueLog.AttributeNames.CREATION_DATE))
				+ "</rm:CreateDate>\n");
		buffer.append("            <rm:UpdateDate>"
				+ string((Date) issue
						.getAttribute(IssueLog.AttributeNames.UPDATE_DATE))
				+ "</rm:UpdateDate>\n");
		buffer.append("            <gml:geometryProperty>\n");
		buffer.append("                "
				+ gmlGeometryWriter.write(issue.getGeometry()).replaceAll("\n",
						"\n                 ").trim() + "\n");
		buffer.append("            </gml:geometryProperty>\n");
		buffer.append("        </rm:issue>\n");
		buffer.append("    </gml:featureMember>\n");
	}

	private String string(Date date) {
		return dateFormat.format(date);
	}

	private void appendFooter(StringBuffer buffer) {
		buffer.append("</rm:issueLog>\n");
	}
}