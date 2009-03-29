package com.vividsolutions.jcs.plugin.issuelog;

import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class CreateOrEditIssuePlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		if (issueAtClick(context) != null) {
			return new EditIssuePlugIn().setIssue(issueAtClick(context))
					.execute(context);
		}
		if (LayerViewPanel.intersects((Geometry) LangUtil.ifNull(
				new FenceLayerFinder(context).getFence(), new GeometryFactory()
						.createPoint((Coordinate) null)), EnvelopeUtil
				.toGeometry(FeatureAtClickFinder.clickBuffer(context)))) {
			return new CreateIssuePlugIn().setGeometry(
					new FenceLayerFinder(context).getFence()).execute(context);
		}
		if (LayerViewPanel.intersects(selectedNonIssueGeometries(context),
				EnvelopeUtil.toGeometry(FeatureAtClickFinder
						.clickBuffer(context)))) {
			return new CreateIssuePlugIn().setGeometry(
					selectedNonIssueGeometries(context)).execute(context);
		}
		context.getLayerViewPanel().getContext().warnUser(
				"No issue, fence, or selection here");
		return false;
	}

	/**
	 * Works for partially-selected features too.
	 */
	private Geometry selectedNonIssueGeometries(final PlugInContext context) {
		Collection selectedNonIssueGeometries = new ArrayList();
		for (Iterator i = nonIssueLayers(context).iterator(); i.hasNext();) {
			Layer nonIssueLayer = (Layer) i.next();
			for (Iterator j = context.getLayerViewPanel().getSelectionManager()
					.getSelectedItems(nonIssueLayer).iterator(); j.hasNext();) {
				Geometry selectedNonIssueGeometry = (Geometry) j.next();
				// Martin decided that we will not use #union, which is more
				// computationally expensive (and can drift in pathological
				// cases). [Jon Aquino 2004-11-09]
				selectedNonIssueGeometries.add(selectedNonIssueGeometry);
			}
		}
		return new GeometryFactory().buildGeometry(selectedNonIssueGeometries);
	}

	private Collection nonIssueLayers(final PlugInContext context) {
		return CollectionUtil.select(context.getLayerViewPanel()
				.getSelectionManager().getLayersWithSelectedItems(),
				new Block() {

					public Object yield(Object layer) {
						return layer != IssueLog.instance(context
								.getLayerManager()) ? Boolean.TRUE
								: Boolean.FALSE;
					}
				});
	}

	private Feature issueAtClick(PlugInContext context)
			throws NoninvertibleTransformException {
		return FeatureAtClickFinder.featureAtClick(IssueLog.instance(
				context.getLayerManager()).getIssues(), context);
	}

	public String getName() {
		return "Create/Edit Issue";
	}

}