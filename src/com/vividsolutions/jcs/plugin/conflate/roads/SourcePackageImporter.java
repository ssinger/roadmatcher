package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import bsh.Interpreter;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_FileUtil;
import com.vividsolutions.jcs.jump.FUTURE_XML2Java;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;

public class SourcePackageImporter {

	public ConflationSession importSourcePackage(File sourcePackage,
			ConsistencyConfiguration consistencyConfiguration,
			TaskMonitor monitor) throws IOException {
		final ConflationSession[] sessions = new ConflationSession[1];
		importSourcePackage(sourcePackage, consistencyConfiguration, monitor,
				new Block() {
					public Object yield(Object session, Object profile) {
						sessions[0] = (ConflationSession) session;
						return null;
					}
				});
		return sessions[0];
	}

	public void importSourcePackage(final File sourcePackage,
			final ConsistencyConfiguration consistencyConfiguration,
			final TaskMonitor monitor, final Block block) throws IOException {
		FUTURE_FileUtil.createTemporaryDirectory(new Block() {
			public Object yield(Object temporaryDirectoryObject) {
				try {
					final File temporaryDirectory = (File) temporaryDirectoryObject;
					FUTURE_FileUtil.unzip(sourcePackage, temporaryDirectory,
							monitor);
					Profile profile = createProfile(temporaryDirectory);
					return block.yield(createSession(GUIUtil
							.nameWithoutExtension(sourcePackage),
							temporaryDirectory,
							createManifest(temporaryDirectory), profile,
							monitor, consistencyConfiguration), profile);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private ConflationSession createSession(String sessionName,
			File temporaryDirectory, SourcePackageManifest manifest,
			Profile profile, TaskMonitor monitor,
			ConsistencyConfiguration consistencyConfiguration) throws Exception {
		monitor.report("Creating Road Networks");
		return NewSessionPlugIn.createSession(sessionName,
				createFeatureCollection(manifest.getDataset(
						manifest.indexOf(profile.dataset(0).getShortName()))
						.getInputDataset(), temporaryDirectory, monitor),
				createFeatureCollection(manifest.getDataset(
						manifest.indexOf(profile.dataset(1).getShortName()))
						.getInputDataset(), temporaryDirectory, monitor),
				createFeatureCollection(manifest.getDataset(
						manifest.indexOf(profile.dataset(0).getShortName()))
						.getNodeConstraintsDataset(), temporaryDirectory,
						monitor), createFeatureCollection(manifest.getDataset(
						manifest.indexOf(profile.dataset(1).getShortName()))
						.getNodeConstraintsDataset(), temporaryDirectory,
						monitor), createFeatureCollection(manifest.getDataset(
						manifest.indexOf(profile.dataset(0).getShortName()))
						.getContextDataset(), temporaryDirectory, monitor),
				createFeatureCollection(manifest.getDataset(
						manifest.indexOf(profile.dataset(1).getShortName()))
						.getContextDataset(), temporaryDirectory, monitor),
				profile, consistencyConfiguration);
	}

	private FeatureCollection createFeatureCollection(
			SourcePackageManifest.DatasetFile dataset, File temporaryDirectory,
			TaskMonitor monitor) throws Exception {
		if (dataset == null || dataset.getFileName().trim().length() == 0) {
			return AddNewLayerPlugIn.createBlankFeatureCollection();
		}
		monitor.report("Opening " + dataset.getFileName());
		Connection connection = dataset.createDataSource(temporaryDirectory)
				.getConnection();
		try {
			return connection.executeQuery(null, monitor);
		} finally {
			connection.close();
		}
	}

	private Profile createProfile(File temporaryDirectory,
			SourcePackageManifest manifest) throws Exception {
		File profileFile = findProfileFile(temporaryDirectory);
		return profileFile == null ? new Profile(manifest.getDataset(0)
				.getShortName(), manifest.getDataset(1).getShortName())
				: (Profile) new FUTURE_XML2Java(getClass().getClassLoader())
						.read(profileFile, Profile.class);
	}

	private File findProfileFile(File temporaryDirectory) {
		for (Iterator i = Arrays.asList(temporaryDirectory.listFiles())
				.iterator(); i.hasNext();) {
			File file = (File) i.next();
			if (file.getPath().toLowerCase().endsWith(".rmprofile")) {
				return file;
			}
		}
		return null;
	}

	private Profile createProfile(final File temporaryDirectory)
			throws Exception {
		return createProfile(temporaryDirectory,
				createManifest(temporaryDirectory));
	}

	private SourcePackageManifest createManifest(final File temporaryDirectory)
			throws Exception {
		return (SourcePackageManifest) new FUTURE_XML2Java(getClass()
				.getClassLoader()).read(new File(temporaryDirectory,
				"manifest.xml"), SourcePackageManifest.class);
	}
}