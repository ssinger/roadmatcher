package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.vertextransfer.VertexTransferStatistics;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_FileUtil;
import com.vividsolutions.jcs.jump.FUTURE_StandardReaderWriterFileDataSource;
import com.vividsolutions.jcs.plugin.issuelog.IssueLog;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;

public class ExportResultPackagePlugIn extends AbstractSaveAsPlugIn implements
		ThreadedPlugIn {
	private static final String EXTENSION = "zip";

	public String getExtension() {
		return EXTENSION;
	}

	protected String getMenuItemName() {
		return "Export Package...";
	}

	protected String[] getMenuPath() {
		return new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
				RoadMatcherToolboxPlugIn.RESULT_MENU_NAME };
	}

	protected File getInitialFile(File currentDirectory, PlugInContext context) {
		return defaultFilename(session(context), getLastFile(context
				.getWorkbenchContext()) != null ? getLastFile(
				context.getWorkbenchContext()).getParentFile()
				: currentDirectory);
	}

	public static File defaultFilename(ConflationSession session, File directory) {
		return new File(directory, session.getName() + "_result." + EXTENSION);
	}

	protected String getFileFilterDescription() {
		return "Result Packages";
	}

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		if (!GenerateResultLayerPlugIn
				.warnAboutNodeConstraintViolations(context)) {
			return false;
		}
		return super.execute(context);
	}

	protected void saveAs(final File file, final TaskMonitor monitor,
			final PlugInContext context) throws Exception {
		GenerateResultLayerPlugIn
				.warnAboutUnknownOrInconsistentRoadSegments(context);
		new ResultPackageExporter().exportResultPackage(session(context),
				IssueLog.instance(context.getLayerManager()).getIssues(), file,
				monitor);
	}

}