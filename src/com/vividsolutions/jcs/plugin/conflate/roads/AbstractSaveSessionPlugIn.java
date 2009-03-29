package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.*;
import java.util.zip.GZIPOutputStream;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_MonitoredBufferedOutputStream;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jcs.plugin.issuelog.IssueLog;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;

//TODO: refactor this class to subclass AbstractSaveAsPlugIn
//[Jon Aquino 2004-05-11]
public abstract class AbstractSaveSessionPlugIn extends ThreadedBasePlugIn {
	public static final String ISSUES_KEY = AbstractSaveSessionPlugIn.class
			.getName()
			+ " - ISSUES";

	public void run(TaskMonitor monitor, PlugInContext context)
			throws Exception {
		//Save to a temp file first, to ensure that no exceptions occur.
		//[Jon Aquino 2004-01-27]
		//TODO: do this temp-file thing for other kinds of save too.
		//Extract a method into FileUtil. [Jon Aquino 2004-05-11]
		File tempFile = File.createTempFile("rds", null, session(context)
				.getFile().getParentFile());
		save(session(context), tempFile, session(context).getFile().length(),
				context, monitor);
		if (session(context).getFile().exists()) {
			Assert.isTrue(session(context).getFile().delete());
		}
		Assert.isTrue(tempFile.renameTo(session(context).getFile()));
	}

	private void save(ConflationSession session, File file,
			final long estimatedFileSize, PlugInContext context,
			final TaskMonitor monitor) throws Exception {
		save(session, issues(context), file, estimatedFileSize, monitor);
		ToolboxModel.instance(context).getSourceLayer(0)
				.setFeatureCollectionModified(false);
		ToolboxModel.instance(context).getSourceLayer(1)
				.setFeatureCollectionModified(false);
		IssueLog.instance(context.getLayerManager()).getLayer()
				.setFeatureCollectionModified(false);
	}

	public static void save(ConflationSession session,
			FeatureCollection issues, File file, final long estimatedFileSize,
			final TaskMonitor monitor) throws FileNotFoundException,
			IOException {
		session.getBlackboard().put(ISSUES_KEY, issues);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		try {
			BufferedOutputStream bufferedOutputStream = new FUTURE_MonitoredBufferedOutputStream(
					fileOutputStream, estimatedFileSize, monitor);
			try {
				GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
						bufferedOutputStream);
				try {
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(
							gzipOutputStream);
					try {
						objectOutputStream
								.writeObject(RoadMatcherExtension.VERSION);
						objectOutputStream.writeObject(session);
					} finally {
						objectOutputStream.close();
					}
				} finally {
					gzipOutputStream.close();
				}
			} finally {
				bufferedOutputStream.close();
			}
		} finally {
			fileOutputStream.close();
		}
	}

	private FeatureCollection issues(PlugInContext context) {
		return IssueLog.instance(context.getLayerManager()).getIssues();
	}

	private FeatureCollection featureCollection(Layer layer) {
		return layer != null ? layer.getFeatureCollectionWrapper()
				.getUltimateWrappee() : null;
	}

	protected ConflationSession session(PlugInContext context) {
		return ToolboxModel.instance(context).getSession();
	}

	public void initialize(PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME },
						getMenuText(),
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
	}

	protected abstract String getMenuText();
}