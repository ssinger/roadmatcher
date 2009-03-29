package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.Statistics;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature.ConflationAttribute;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.VertexTransferStatistics;
import com.vividsolutions.jcs.jump.FUTURE_AttributeMapping;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringEndpointStyle;

public class GenerateResultLayerPlugIn extends AbstractPlugIn {
	private static final String SOURCE_ATTRIBUTE_NAME = "Source";

	private void addSegments(int i, FeatureDataset featureDataset,
			ConflationSession session, FUTURE_AttributeMapping mapping,
			double minLineSegmentLength, VertexTransferProperties properties,
			VertexTransferStatistics statistics, Block segmentFilter) {
		for (Iterator j = session.getSourceNetwork(i).getGraph().getEdges()
				.iterator(); j.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) j.next();
			if (!((Boolean) segmentFilter.yield(roadSegment)).booleanValue()) {
				continue;
			}
			BasicFeature newFeature = new BasicFeature(featureDataset
					.getFeatureSchema());
			newFeature.setGeometry(properties
					.isTransferringVerticesTo(roadSegment) ? properties
					.getVertexTransferOp().transfer(
							roadSegment.getMatchingRoadSegment()
									.getApparentLine(),
							roadSegment.getApparentLine(),
							minLineSegmentLength, statistics) : roadSegment
					.getApparentLine());
			//Transfer the non-reference first and the reference second, so
			//the reference overwrites the conflation attributes of the
			//non-reference [Jon Aquino 2004-04-28]
			mapping.transferAttributes(i != 0 ? matchingFeature(roadSegment)
					: null, i != 1 ? matchingFeature(roadSegment) : null,
					newFeature);
			mapping.transferAttributes(
					i == 0 ? roadSegment.getFeature() : null,
					i == 1 ? roadSegment.getFeature() : null, newFeature);
			newFeature.setAttribute(SOURCE_ATTRIBUTE_NAME, roadSegment
					.getNetwork().getName());
			featureDataset.add(newFeature);
		}
	}

	public FeatureCollection createResultFeatureCollection(
			VertexTransferStatistics statistics, ConflationSession session,
			Block segmentFilter) {
		return createResultFeatureCollection(statistics, ResultOptions
				.get(session), session, segmentFilter);
	}

	private FeatureCollection createResultFeatureCollection(
			VertexTransferStatistics statistics, ResultOptions options,
			ConflationSession session, Block segmentFilter) {
		FUTURE_AttributeMapping mapping = createAttributeMapping(options,
				session);
		FeatureDataset featureDataset = new FeatureDataset(
				insertSourceAttribute(mapping.createSchema(session
						.getSourceNetwork(0).getFeatureCollection()
						.getFeatureSchema(), session.getSourceNetwork(1)
						.getFeatureCollection().getFeatureSchema(), "GEOMETRY")));
		addSegments(0, featureDataset, session, mapping,
				minLineSegmentLength(session), options
						.getVertexTransferProperties(), statistics,
				segmentFilter);
		addSegments(1, featureDataset, session, mapping,
				minLineSegmentLength(session), options
						.getVertexTransferProperties(), statistics,
				segmentFilter);
		return featureDataset;
	}

	private FeatureSchema insertSourceAttribute(FeatureSchema schema) {
		FeatureSchema newSchema = new FeatureSchema();
		newSchema.addAttribute(SOURCE_ATTRIBUTE_NAME, AttributeType.STRING);
		for (int i = 0; i < schema.getAttributeCount(); i++) {
			newSchema.addAttribute(schema.getAttributeName(i), schema
					.getAttributeType(i));
		}
		return newSchema;
	}

	private FUTURE_AttributeMapping createAttributeMapping(
			ResultOptions resultOptions, ConflationSession session) {
		//Disambiguation of names present in both datasets A and B. But we are
		// not
		//disambiguating duplicate names *within* a dataset (well, we're
		// creating the
		//columns, but as the lookup is name-based rather than index-based, I
		// don't
		//think it's going to work). Anyway, low risk. [Jonathan Aquino
		// 2004-07-06]
		List oldAttributeNamesA = attributeNames(0, false, resultOptions);
		List newAttributeNamesA = FUTURE_AttributeMapping.ensureUnique(
				attributeNames(0, true, resultOptions), Collections
						.singleton(SOURCE_ATTRIBUTE_NAME));
		List oldAttributeNamesB = attributeNames(1, false, resultOptions);
		//Set the taken names to the new A names without the *result*
		//conflation attributes. Thus, for example, A's "MaxDist" and
		//B's "MaxDist" will map to the same field rather than two different
		//fields which would look funny [Jon Aquino 2004-05-05]
		List newAttributeNamesB = FUTURE_AttributeMapping.ensureUnique(
				attributeNames(1, true, resultOptions), FUTURE_CollectionUtil
						.add(SOURCE_ATTRIBUTE_NAME, without(newAttributeNamesA,
								resultConflationAttributeNames(true))));
		return new FUTURE_AttributeMapping(oldAttributeNamesA,
				newAttributeNamesA, oldAttributeNamesB, newAttributeNamesB);
	}

	private List attributeNames(int i, boolean shapefileNames,
			ResultOptions resultOptions) {
		return (List) FUTURE_CollectionUtil.concatenate(
				resultConflationAttributeNames(shapefileNames), resultOptions
						.getDatasetAttributesToInclude(i));
	}

	private Collection resultConflationAttributeNames(boolean shapefileNames) {
		// We used to leave out Length, but now we're including all conflation
		// attributes [Jon Aquino 2004-11-01]
		return conflationAttributeNames(shapefileNames);
	}

	public static List withoutConflationAttributeNames(List attributeNames) {
		//Handle unlikely scenario of attribute name matching
		//conflation attribute name [Jon Aquino 2004-05-04]
		return without(attributeNames, conflationAttributeNames(false));
	}

	private static List without(List attributeNames,
			final Collection attributeNamesToSkip) {
		return (List) CollectionUtil.select(attributeNames, new Block() {
			public Object yield(Object attributeName) {
				try {
					return Boolean.valueOf(!attributeNamesToSkip
							.contains(attributeName));
				} finally {
					attributeNamesToSkip.remove(attributeName);
				}
			}
		});
	}

	public static Collection conflationAttributeNames(
			final boolean shapefileNames) {
		return CollectionUtil.collect(SourceFeature.CONFLATION_ATTRIBUTES,
				new Block() {
					public Object yield(Object conflationAttributeObject) {
						ConflationAttribute conflationAttribute = ((SourceFeature.ConflationAttribute) conflationAttributeObject);
						return shapefileNames ? conflationAttribute
								.getShapefileName() : conflationAttribute
								.getName();
					}
				});
	}

	private FeatureCollection createVertexTransferVectorsFeatureCollection(
			VertexTransferStatistics statistics) {
		FeatureSchema schema = new FeatureSchema();
		schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
		schema.addAttribute("LENGTH", AttributeType.DOUBLE);
		FeatureDataset featureDataset = new FeatureDataset(schema);
		for (Iterator i = statistics.getVectors().iterator(); i.hasNext();) {
			LineString transferVector = (LineString) i.next();
			Feature feature = new BasicFeature(schema);
			feature.setAttribute("GEOMETRY", transferVector);
			feature.setAttribute("LENGTH", new Double(transferVector
					.getLength()));
			featureDataset.add(feature);
		}
		return featureDataset;
	}

	//Because we are caching the dialog, the parameters are reset whenever
	//RoadMatcher is restarted, but while it is running the parameters are
	//remembered. [Jon Aquino 2004-09-28]
	private GenerateResultLayerDialog dialog;

	public boolean execute(final PlugInContext context) throws Exception {
		// Yes we are modifying the model (by removing and adding a layer), so
		// #reportNothingToUndoYet shouldn't be called, but we don't want to
		// truncate the undo history. [Jon Aquino]
		reportNothingToUndoYet(context);
		if (!warnAboutNodeConstraintViolations(context)) {
			return false;
		}
		dialog.getPanel().initialize(ToolboxModel.instance(context));
		dialog.setVisible(true);
		if (!dialog.wasOKPressed()) {
			return false;
		}
		VertexTransferStatistics statistics = new VertexTransferStatistics();
		Layer resultLayer = generateResultLayer(statistics, context,
				new Block() {
					public Object yield(Object segment) {
						return Boolean.valueOf(dialog.getPanel().allows(
								(SourceRoadSegment) segment));
					}
				});
		Layer vertexTransferVectorsLayer = dialog.getPanel()
				.isSpecifyingVertexTransferVectorsLayer() ? generateVertexTransferVectorsLayer(
				resultLayer, statistics, context)
				: null;
		reportStatistics(resultLayer, context.getOutputFrame(), ResultOptions
				.get(ToolboxModel.instance(context).getSession())
				.getVertexTransferProperties(), statistics, ToolboxModel
				.instance(context).getSession());
		return true;
	}

	public static boolean warnAboutNodeConstraintViolations(
			PlugInContext context) {
		Block wktConverter = new Block() {
			public Object yield(Object coordinates) {
				StringBuffer stringBuffer = new StringBuffer();
				for (Iterator i = ((Collection) coordinates).iterator(); i
						.hasNext();) {
					Coordinate coordinate = (Coordinate) i.next();
					stringBuffer.append("POINT (").append(coordinate.x).append(
							" ").append(coordinate.y).append(") ");
				}
				return stringBuffer.toString();
			}
		};
		ArrayList nodeConstraintViolationCoordinates = new ArrayList();
		nodeConstraintViolationCoordinates
				.addAll(nodeConstraintViolationCoordinates(ToolboxModel
						.instance(context).getSession().getSourceNetwork(0)));
		nodeConstraintViolationCoordinates
				.addAll(nodeConstraintViolationCoordinates(ToolboxModel
						.instance(context).getSession().getSourceNetwork(1)));
		if (nodeConstraintViolationCoordinates.isEmpty()) {
			return true;
		}
		context.getOutputFrame().createNewDocument();
		context.getOutputFrame().addHeader(1, "Node Constraint Violations");
		context.getOutputFrame()
				.addText(
						(String) wktConverter
								.yield(nodeConstraintViolationCoordinates));
		context
				.getWorkbenchFrame()
				.warnUser(
						FUTURE_StringUtil
								.substitute(
										ErrorMessages.generateResultLayerPlugIn_nodeConstraintsViolated,
										new Object[] { nodeConstraintViolationCoordinates
												.size()
												+ "" }));
		return false;
	}

	private static Collection nodeConstraintViolationCoordinates(
			RoadNetwork sourceNetwork) {
		ArrayList nodeConstraintViolationCoordinates = new ArrayList();
		for (Iterator i = sourceNetwork.getGraph().edgeIterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			if (roadSegment.isStartNodeConstrained()
					&& !FUTURE_LineString.first(roadSegment.getLine()).equals(
							FUTURE_LineString.first(roadSegment
									.getApparentLine()))) {
				nodeConstraintViolationCoordinates.add(FUTURE_LineString
						.first(roadSegment.getApparentLine()));
			}
			if (roadSegment.isEndNodeConstrained()
					&& !FUTURE_LineString.last(roadSegment.getLine()).equals(
							FUTURE_LineString.last(roadSegment
									.getApparentLine()))) {
				nodeConstraintViolationCoordinates.add(FUTURE_LineString
						.last(roadSegment.getApparentLine()));
			}
		}
		return nodeConstraintViolationCoordinates;
	}

	private Layer generateVertexTransferVectorsLayer(Layer resultLayer,
			VertexTransferStatistics statistics, PlugInContext context) {
		Layer vertexTransferVectorsLayer = context
				.getLayerManager()
				.addLayer(
						"Output",
						ToolboxModel
								.markAsForConflation(new Layer(
										vertexTransferVectorsLayerName(resultLayer),
										new Color(255, 0, 0, 255),
										createVertexTransferVectorsFeatureCollection(statistics),
										context.getLayerManager())));
		vertexTransferVectorsLayer.setVisible(false);
		vertexTransferVectorsLayer
				.addStyle(new ArrowLineStringEndpointStyle.OpenEnd());
		return vertexTransferVectorsLayer;
	}

	private String vertexTransferVectorsLayerName(Layer resultLayer) {
		//If JUMP put extra info at the end of the Result layer's name
		//e.g. "(2)", put the same info at the end of the Vertex Transfer
		//Vectors layer, so the user can see that they go together.
		//[Jon Aquino 2004-03-24]
		return "Vertex Transfer Vectors"
				+ resultLayer.getName().substring(RESULT_LAYER_NAME.length());
	}

	private Layer generateResultLayer(VertexTransferStatistics statistics,
			final PlugInContext context, Block segmentFilter) {
		return context.getLayerManager().addLayer(
				"Output",
				ToolboxModel.markAsForConflation(new Layer(RESULT_LAYER_NAME,
						Color.black, createResultFeatureCollection(statistics,
								ToolboxModel.instance(context).getSession(),
								segmentFilter), context.getLayerManager())));
	}

	private void reportStatistics(Layer resultLayer, HTMLFrame frame,
			VertexTransferProperties properties,
			VertexTransferStatistics statistics, ConflationSession session) {
		frame.createNewDocument();
		frame.addHeader(1, "Vertex Transfer Results");
		frame.addField("Result Layer:", resultLayer.getName());
		frame.addField("Time:", new Date() + "");
		frame.addField("Transferring from "
				+ session.getSourceNetwork(0).getName() + " to "
				+ session.getSourceNetwork(1).getName() + ":",
				yesOrNo(properties.isTransferringVerticesFrom0To1()));
		frame.addField("Transferring from "
				+ session.getSourceNetwork(1).getName() + " to "
				+ session.getSourceNetwork(0).getName() + ":",
				yesOrNo(properties.isTransferringVerticesFrom1To0()));
		frame.addField("Algorithm:", StringUtil.toFriendlyName(properties
				.getVertexTransferOpClass().getName(), "VertexTransferOp"));
		frame.addText("");
		frame.addField("Average Distance:", ""
				+ FUTURE_StringUtil.format(statistics.getAverageDistance()));
		frame.addField("Maximum Distance:", ""
				+ FUTURE_StringUtil.format(statistics.getMaxDistance()));
		frame.addField("Vertices Transferred:", "" + statistics.getTransfers());
		frame.addField("Vertices Discarded (short segments):", ""
				+ statistics.getDiscardsDueToShortSegments());
		frame.addField("Vertices Discarded (out of order):", ""
				+ statistics.getDiscardsDueToVerticesOutOfOrder());
	}

	private String yesOrNo(boolean b) {
		return b ? "Yes" : "No";
	}

	public void initialize(PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.RESULT_MENU_NAME },
						"Generate Layer...",
						false,
						null,
						new MultiEnableCheck()
								.add(
										context
												.getCheckFactory()
												.createWindowWithLayerViewPanelMustBeActiveCheck())
								.add(
										SpecifyRoadFeaturesTool
												.createConflationSessionMustBeStartedCheck(context
														.getWorkbenchContext())));
		//Because we are caching the dialog, the parameters are reset whenever
		//RoadMatcher is restarted, but while it is running the parameters are
		//remembered. [Jon Aquino 2004-09-28]
		dialog = new GenerateResultLayerDialog(context.getWorkbenchFrame(),
				getName(), context.getWorkbenchContext());
	}

	private Feature matchingFeature(SourceRoadSegment roadSegment) {
		return roadSegment.getMatchingRoadSegment() != null ? roadSegment
				.getMatchingRoadSegment().getFeature() : null;
	}

	private double minLineSegmentLength(ConflationSession session) {
		return AdjustEndpointOperation.minLineSegmentLength(session);
	}

	public static void warnAboutUnknownOrInconsistentRoadSegments(
			final PlugInContext context) {
		Statistics statistics = ToolboxModel.instance(context).getSession()
				.getStatistics();
		int unknownCount = statistics.get(0).getUnknownCount()
				+ statistics.get(1).getUnknownCount();
		int inconsistentCount = statistics.get(0).getResultStatistics()
				.getInconsistentCount()
				+ statistics.get(1).getResultStatistics()
						.getInconsistentCount();
		if (unknownCount + inconsistentCount == 0) {
			return;
		}
		context
				.getWorkbenchFrame()
				.warnUser(
						FUTURE_StringUtil
								.substitute(
										ErrorMessages.exportResultPlugIn_unknownOrInconsistent,
										new Object[] { (unknownCount + inconsistentCount)
												+ "" }));
	}

	private static final String RESULT_LAYER_MARKER_KEY = GenerateResultLayerPlugIn.class
			.getName()
			+ " - RESULT LAYER MARKER";

	private static final String RESULT_LAYER_NAME = "Result";
}