package com.vividsolutions.jcs.plugin.issuelog;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.sun.corba.se.connection.GetEndPointInfoAgainException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.DelegatingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;

public class IssueLog {
	public static final class AttributeNames {
		public static final String COMMENT = "Comment";

		public static final String CREATION_DATE = "CreateDate";

		public static final String DESCRIPTION = "Desc";

		private static final String GEOMETRY = "Geometry";

		public static final String STATUS = "Status";

		public static final String TYPE = "Type";

		public static final String UPDATE_DATE = "UpdateDate";

		public static final String USER_NAME = "User";
	}

	public static final class AttributeValues {
		public static final String CLOSED_STATUS = "Closed";

		public static final String ERROR_TYPE = "Error";

		public static final String OPEN_STATUS = "Open";

		public static final String WARNING_TYPE = "Warning";

		public static final String COMMENT_TYPE = "Comment";
	}

	private IssueLog(LayerManager layerManager) {
		this.layerManager = layerManager;
	}

	private FeatureSchema createFeatureSchema() {
		FeatureSchema schema = new FeatureSchema();
		schema.addAttribute(AttributeNames.GEOMETRY, AttributeType.GEOMETRY);
		schema.addAttribute(AttributeNames.TYPE, AttributeType.STRING);
		schema.addAttribute(AttributeNames.DESCRIPTION, AttributeType.STRING);
		schema.addAttribute(AttributeNames.COMMENT, AttributeType.STRING);
		schema.addAttribute(AttributeNames.STATUS, AttributeType.STRING);
		schema.addAttribute(AttributeNames.USER_NAME, AttributeType.STRING);
		schema.addAttribute(AttributeNames.CREATION_DATE, AttributeType.DATE);
		schema.addAttribute(AttributeNames.UPDATE_DATE, AttributeType.DATE);
		return schema;
	}

	public Feature createIssue(final Geometry geometry, final String type,
			final String description, final String comment, final String status) {
		final Feature issue = new BasicFeature(getIssues().getFeatureSchema());
		issue.setGeometry(geometry);
		issue.setAttribute(AttributeNames.CREATION_DATE, new Date());
		layerManager.deferFiringEvents(new Runnable() {
			public void run() {
				updateIssue(issue, type, description, comment, status);
			}
		});
		return issue;
	}

	public void addIssue(Feature issue) {
		getLayer().getFeatureCollectionWrapper().add(issue);
	}

	public void deleteIssue(Feature issue) {
		getLayer().getFeatureCollectionWrapper().remove(issue);
	}

	public FeatureCollection getIssues() {
		return getLayer().getFeatureCollectionWrapper().getUltimateWrappee();
	}

	public Layer getLayer() {
		if (layerManager.getLayer(LAYER_NAME) != null) {
			return layerManager.getLayer(LAYER_NAME);
		}
		final Layer layer = new Layer(LAYER_NAME, Color.orange,
				new FeatureDataset(createFeatureSchema()), layerManager);
		layerManager.deferFiringEvents(new Runnable() {
			public void run() {
				layer.getBasicStyle().setEnabled(false);
				layer.addStyle(new IssueStyle());
				layer.setDrawingLast(true);
			}
		});
		layerManager.addLayer(StandardCategoryNames.QA, layer);
		return layer;
	}

	public static class IssueStyle extends DelegatingStyle {
		public boolean isEnabled() {
			return true;
		}

		public void paint(Feature f, Graphics2D g, Viewport viewport)
				throws Exception {
			if (f.getString(AttributeNames.STATUS).equals(
					AttributeValues.CLOSED_STATUS)) {
				setStyle(closedStyle);
			} else if (f.getString(AttributeNames.TYPE).equals(
					AttributeValues.ERROR_TYPE)) {
				setStyle(errorStyle);
			} else {
				setStyle(defaultStyle);
			}
			super.paint(f, g, viewport);
		}

		private Style closedStyle = createStyle(Color.lightGray);

		private Style defaultStyle = createStyle(Color.orange);

		private Style errorStyle = createStyle(Color.red);

		private BasicStyle createStyle(Color color) {
			BasicStyle style = new BasicStyle();
			style.setAlpha(150);
			style.setRenderingFill(false);
			style.setRenderingFillPattern(true);
			style.setFillPattern(WKTFillPattern.createDiagonalStripePattern(4,
					2, false, true));
			style.setRenderingLine(true);
			style.setFillColor(color);
			style.setLineColor(color);
			style.setLineWidth(8);
			return style;
		}
	}

	public void updateIssue(Feature issue, String type, String description,
			String comment, String status) {
		issue.setAttribute(AttributeNames.TYPE, type);
		issue.setAttribute(AttributeNames.DESCRIPTION, description);
		issue.setAttribute(AttributeNames.COMMENT, comment);
		issue.setAttribute(AttributeNames.STATUS, status);
		issue.setAttribute(AttributeNames.USER_NAME, userName);
		issue.setAttribute(AttributeNames.UPDATE_DATE, new Date());
		layerManager.fireFeaturesChanged(Collections.singleton(issue),
				FeatureEventType.ATTRIBUTES_MODIFIED, getLayer());
	}

	private LayerManager layerManager;

	private List statusCodes = Arrays.asList(new String[] {
			AttributeValues.OPEN_STATUS, AttributeValues.CLOSED_STATUS });

	private List typeCodes = Arrays.asList(new String[] {
			AttributeValues.COMMENT_TYPE, AttributeValues.WARNING_TYPE,
			AttributeValues.ERROR_TYPE });

	private String userName = System.getProperty("user.name");

	public static IssueLog instance(LayerManager layerManager) {
		String key = IssueLog.class.getName() + " - INSTANCE";
		if (layerManager.getBlackboard().get(key) == null) {
			layerManager.getBlackboard().put(key, new IssueLog(layerManager));
		}
		return (IssueLog) layerManager.getBlackboard().get(key);
	}

	//Keep a space in the name. This has the following beneficial
	//side-effect: When RoadMatcher saves the issue log layer
	//(as "IssueLog", no space), if the user ever brings it in in
	//the future, JUMP will not treat it as the issue log. Good
	//because RoadMatcher truncates the attribute names to
	//11 characters. [Jon Aquino 2004-05-05]
	private static final String LAYER_NAME = "Issue Log";

	public List getStatusCodes() {
		return Collections.unmodifiableList(statusCodes);
	}

	public List getTypeCodes() {
		return Collections.unmodifiableList(typeCodes);
	}

	public String getUserName() {
		return userName;
	}
}