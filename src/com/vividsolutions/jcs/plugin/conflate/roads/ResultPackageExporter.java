package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.VertexTransferStatistics;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_FeatureCollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_FileUtil;
import com.vividsolutions.jcs.jump.FUTURE_StandardReaderWriterFileDataSource;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.FileUtil;

public class ResultPackageExporter {
	public void exportResultPackage(final ConflationSession session,
			final FeatureCollection issues, final File file,
			final TaskMonitor monitor) throws IOException {
		FUTURE_FileUtil.createTemporaryDirectory(new Block() {
			public Object yield(Object temporaryDirectory) {
				try {
					saveTempFiles(session, issues, ((File) temporaryDirectory),
							monitor);
					FUTURE_FileUtil.zip(
							((File) temporaryDirectory).listFiles(), file, -1,
							monitor);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		});
	}

	private void saveShapefile(FeatureCollection featureCollection, File file,
			TaskMonitor monitor) throws Exception {
		DataSource dataSource = new FUTURE_StandardReaderWriterFileDataSource.Shapefile();
		dataSource.setProperties(Collections.singletonMap(DataSource.FILE_KEY,
				file.getPath()));
		final FeatureSchema schema = uppercaseSchema(featureCollection
				.getFeatureSchema());
		Connection connection = dataSource.getConnection();
		try {
			connection.executeUpdate(null, new FeatureCollectionWrapper(
					featureCollection) {
				public FeatureSchema getFeatureSchema() {
					return schema;
				}
			}

			, monitor);
		} finally {
			connection.close();
		}
	}

	private FeatureSchema uppercaseSchema(FeatureSchema schema) {
		FeatureSchema uppercaseSchema = new FeatureSchema();
		for (int i = 0; i < schema.getAttributeCount(); i++) {
			uppercaseSchema.addAttribute(schema.getAttributeName(i)
					.toUpperCase(), schema.getAttributeType(i));
		}
		return uppercaseSchema;
	}

	private String geometryAttributeName(int i, ConflationSession session) {
		return session.getSourceNetwork(i).getFeatureCollection()
				.getFeatureSchema().getAttributeName(
						session.getSourceNetwork(i).getFeatureCollection()
								.getFeatureSchema().getGeometryIndex());
	}

	private FeatureCollection createResultFeatureCollection(
			ConflationSession session) {
		return new GenerateResultLayerPlugIn().createResultFeatureCollection(
				new VertexTransferStatistics(), session, new Block() {
					public Object yield(Object segment) {
						return Boolean.valueOf(((SourceRoadSegment) segment)
								.getState().indicates(SourceState.INCLUDED));
					}
				});
	}

	private FeatureCollection createSourceFeatureCollection(int i,
			ConflationSession session) {
		return createSourceFeatureCollection(session.getSourceNetwork(i)
				.getFeatureCollection(), FUTURE_CollectionUtil.add(
				geometryAttributeName(i, session), new ArrayList(ResultOptions
						.get(session).getDatasetAttributesToInclude(i))));
	}

	private FeatureCollection createSourceFeatureCollection(
			FeatureCollection featureCollection, Collection attributesToInclude) {
		return new ResultPackageSourceFeatureCollection(featureCollection,
				attributesToInclude, true);
	}

	private void saveTempFiles(ConflationSession session,
			FeatureCollection issues, File temporaryDirectory,
			TaskMonitor monitor) throws Exception {
		monitor.report("Saving issue log");
		// Set NaN z's to -9999; otherwise, get "NaN" strings in the GML.
		// I can't think of a better alternative - Bret Champoux says that
		// Martin Davis said that the GML spec does not allow a mix of
		// 2D and 3D coordinates in a Geometry. [Jon Aquino 2004-10-05]
		saveIssueLogGML(setNaNZsTo9999(issues), new File(temporaryDirectory,
				"IssueLog.gml"), monitor);
		// Set NaN z's to -9999; otherwise, they will be set to 0 in the
		// shapefile, which Graeme Leeming says is dangerous because TRIM
		// elevations at sea level will be 0. [Jon Aquino 2004-10-05]
		monitor.report("Saving result network");
		saveShapefile(setNaNZsTo9999(createResultFeatureCollection(session)),
				new File(temporaryDirectory, "Result.shp"), monitor);
		monitor.report("Saving " + session.getSourceNetwork(0).getName()
				+ " network");
		saveShapefile(
				setNaNZsTo9999(createSourceFeatureCollection(0, session)),
				new File(temporaryDirectory, "Src"
						+ session.getSourceNetwork(0).getName() + ".shp"),
				monitor);
		monitor.report("Saving " + session.getSourceNetwork(1).getName()
				+ " network");
		saveShapefile(
				setNaNZsTo9999(createSourceFeatureCollection(1, session)),
				new File(temporaryDirectory, "Src"
						+ session.getSourceNetwork(1).getName() + ".shp"),
				monitor);
	}

	private FeatureCollection setNaNZsTo9999(FeatureCollection featureCollection) {
		// Clone the geometries; otherwise we will be modifying the networks.
		// [Jon Aquino 2004-10-05]
		return new FeatureDataset(CollectionUtil.collect(
				((FeatureCollection) FUTURE_FeatureCollectionUtil.clone(true,
						featureCollection)).getFeatures(), new Block() {
					public Object yield(Object feature) {
						((Feature) feature).getGeometry().apply(
								new CoordinateFilter() {
									public void filter(Coordinate coordinate) {
										coordinate.z = Double
												.isNaN(coordinate.z) ? -9999
												: coordinate.z;
									}
								});
						return feature;
					}
				}), featureCollection.getFeatureSchema());
	}

	private void saveIssueLogGML(FeatureCollection issues, File file,
			TaskMonitor monitor) throws IOException {
		FileUtil.setContents(file.getPath(), new IssueLogGMLWriter()
				.gml(issues));
	}
}