package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jcs.jump.FUTURE_StandardReaderWriterFileDataSource;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;

public class SourcePackageManifest {
	public static class Dataset {
		private String shortName;

		private DatasetFile inputDataset;

		private DatasetFile contextDataset;

		private DatasetFile nodeConstraintsDataset;

		public DatasetFile getContextDataset() {
			return contextDataset;
		}

		public void setContextDataset(DatasetFile contextDataset) {
			this.contextDataset = contextDataset;
		}

		public DatasetFile getInputDataset() {
			return inputDataset;
		}

		public void setInputDataset(DatasetFile inputDataset) {
			this.inputDataset = inputDataset;
		}

		public DatasetFile getNodeConstraintsDataset() {
			return nodeConstraintsDataset;
		}

		public void setNodeConstraintsDataset(DatasetFile nodeConstraintsDataset) {
			this.nodeConstraintsDataset = nodeConstraintsDataset;
		}

		public String getShortName() {
			return shortName;
		}

		public void setShortName(String shortName) {
			this.shortName = shortName;
		}
	}

	private List datasets = new ArrayList();

	public List getDatasets() {
		return Collections.unmodifiableList(datasets);
	}

	public void addDataset(Dataset dataset) {
		datasets.add(dataset);
	}

	public Dataset getDataset(int i) {
		return (Dataset) datasets.get(i);
	}

	public int indexOf(String shortName) {
		if (shortName.equals(getDataset(0).getShortName())) {
			return 0;
		}
		if (shortName.equals(getDataset(1).getShortName())) {
			return 1;
		}
		throw new RuntimeException(FUTURE_StringUtil.substitute(
				ErrorMessages.sourcePackageManifest_unrecognizedShortName,
				new Object[] { shortName }));
	}

	public static class DatasetFile {
		private String fileName;

		private String format;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String filename) {
			this.fileName = filename;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		private static Map FORMAT_TO_DATA_SOURCE_CLASS_MAP = CollectionUtil
				.createMap(new Object[] {
						"shape",
						FUTURE_StandardReaderWriterFileDataSource.Shapefile.class,
						"wkt", StandardReaderWriterFileDataSource.WKT.class,
						"jml", StandardReaderWriterFileDataSource.JML.class,
						"fmegml",
						StandardReaderWriterFileDataSource.FMEGML.class });

		public DataSource createDataSource(File directory) {
			if (!FORMAT_TO_DATA_SOURCE_CLASS_MAP.containsKey(getFormat())) {
				throw new RuntimeException(
						FUTURE_StringUtil
								.substitute(
										ErrorMessages.sourcePackageManifest_unrecognizedFormat,
										new Object[] {
												getFormat(),
												StringUtil
														.toCommaDelimitedString(FORMAT_TO_DATA_SOURCE_CLASS_MAP
																.keySet()) }));
			}
			try {
				DataSource dataSource = (DataSource) ((Class) FORMAT_TO_DATA_SOURCE_CLASS_MAP
						.get(getFormat().toLowerCase())).newInstance();
				dataSource.setProperties(Collections.singletonMap(
						DataSource.FILE_KEY, new File(directory, getFileName())
								.getPath()));
				return dataSource;
			} catch (InstantiationException e) {
				FUTURE_Assert.shouldNeverReachHere(e);
			} catch (IllegalAccessException e) {
				FUTURE_Assert.shouldNeverReachHere(e);
			}
			return null;
		}
	}
}