package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_AttributeMapping;
import com.vividsolutions.jump.util.StringUtil;

public class ResultOptions implements Serializable {


	private List[] datasetAttributesToInclude = new List[] { new ArrayList(),
			new ArrayList() };

	public List getDatasetAttributesToInclude(int i) {
		return Collections.unmodifiableList(datasetAttributesToInclude[i]);
	}

	public String getDataset0AttributesToInclude() {
		return StringUtil
				.toCommaDelimitedString(getDatasetAttributesToInclude(0));
	}

	public String getDataset1AttributesToInclude() {
		return StringUtil
				.toCommaDelimitedString(getDatasetAttributesToInclude(1));
	}

	private ConflationSession session;

	public ResultOptions setDatasetAttributesToInclude(int i,
			List datasetAttributesToInclude) {
		this.datasetAttributesToInclude[i] = datasetAttributesToInclude;
		return this;
	}

	public void setDataset0AttributesToInclude(
			String dataset0AttributesToInclude) {
		setDatasetAttributesToInclude(0, StringUtil
				.fromCommaDelimitedString(dataset0AttributesToInclude));
	}

	public void setDataset1AttributesToInclude(
			String dataset1AttributesToInclude) {
		setDatasetAttributesToInclude(1, StringUtil
				.fromCommaDelimitedString(dataset1AttributesToInclude));
	}

	private VertexTransferProperties vertexTransferProperties = new VertexTransferProperties();

	private static final String RESULT_OPTIONS_KEY = SourceAttributesOptionsPanel.class
			.getName()
			+ " - RESULT OPTIONS";

	public VertexTransferProperties getVertexTransferProperties() {
		return vertexTransferProperties;
	}

	public static ResultOptions get(ConflationSession session) {
		return (ResultOptions) session.getBlackboard().get(RESULT_OPTIONS_KEY,
				new ResultOptions());
	}

	public static void set(ResultOptions resultOptions,
			ConflationSession session) {
		session.getBlackboard().put(RESULT_OPTIONS_KEY, resultOptions);
	}

	public static List allAttributes(int i, ConflationSession session) {
		return GenerateResultLayerPlugIn
				.withoutConflationAttributeNames(FUTURE_AttributeMapping
						.nonSpatialAttributeNames(session.getSourceNetwork(i)
								.getFeatureCollection().getFeatureSchema()));
	}

	public void setSession(ConflationSession session) {
		this.session = session;
	}

	/**
	 * @param vertexTransferProperties
	 *                     The vertexTransferProperties to set.
	 */
	public void setVertexTransferProperties(
			VertexTransferProperties vertexTransferProperties) {
		this.vertexTransferProperties = vertexTransferProperties;
	}
}