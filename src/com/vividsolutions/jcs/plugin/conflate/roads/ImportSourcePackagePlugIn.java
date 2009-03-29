package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.jump.FUTURE_FileUtil;
import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jcs.jump.FUTURE_XML2Java;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;

public class ImportSourcePackagePlugIn extends AbstractOpenPlugIn implements
		ThreadedPlugIn {
	private boolean autoMatch;
	
	public String getName() {
		return "New Session From Source Package";
	}

	private boolean autoAdjust;

	public String getExtension() {
		return "zip";
	}

	protected String getFileFilterDescription() {
		return "Source Packages";
	}

	public boolean execute(PlugInContext context) throws Exception {
		if (!super.execute(context)) {
			return false;
		}
		if (!zipContains("manifest\\.xml", getLastFile(context
				.getWorkbenchContext()))) {
			context.getWorkbenchFrame().warnUser(
					ErrorMessages.importSourcePackagePlugIn_noManifest);
			return false;
		}
		askWhetherToRunAutoMatchAndAutoAdjust(zipContains(".*\\.rmprofile",
				getLastFile(context.getWorkbenchContext())), context
				.getWorkbenchFrame());
		return true;
	}

	private boolean zipContains(String regex, File zip) throws IOException {
		ZipFile zipFile = new ZipFile(zip);
		try {
			for (Enumeration entries = zipFile.entries(); entries
					.hasMoreElements();) {
				String zipEntryName = ((ZipEntry) entries.nextElement())
						.getName();
				if (zipEntryName.toLowerCase().matches(regex)) {
					return true;
				}
			}
			return false;
		} finally {
			zipFile.close();
		}
	}

	private SourcePackageImporter sourcePackageImporter = new SourcePackageImporter();

	protected void open(final File file, final TaskMonitor monitor,
			final PlugInContext context) throws Exception {
		final TaskFrame newTaskFrame = context.getWorkbenchFrame()
				.addTaskFrame();
		//Turn on the new TaskFrame's UndoableEditReceiver for the
		//AutoAdjust operation [Jon Aquino 2004-06-04]
		enableUndoableEditReceiver(new Block() {
			public Object yield() {
				try {
					sourcePackageImporter.importSourcePackage(file,
							ToolboxModel
									.instance(context.getWorkbenchContext())
									.getConsistencyConfiguration(), monitor,
							new Block() {
								public Object yield(Object session,
										Object profile) {
									try {
										NewSessionPlugIn.initializeTask(
												(ConflationSession) session,
												(Profile) profile, autoMatch,
												autoAdjust, monitor, context
														.getWorkbenchContext());
										return null;
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							});
					OpenRoadMatcherSessionPlugIn
							.addLayersForOriginalFeatureCollections(
									ToolboxModel.instance(
											context.getWorkbenchContext())
											.getSession(), newTaskFrame
											.getLayerManager(), context
											.getWorkbenchContext());
					OpenRoadMatcherSessionPlugIn
							.addLayerForContextFeatureCollection(ToolboxModel
									.instance(context.getWorkbenchContext())
									.getSession(), newTaskFrame
									.getLayerManager(), context
									.getWorkbenchContext());
					return null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}, newTaskFrame);
	}

	private void askWhetherToRunAutoMatchAndAutoAdjust(
			boolean profileSpecified, WorkbenchFrame workbenchFrame)
			throws Exception {
		final PerformAutomaticConflationPanel mainPanel = new PerformAutomaticConflationPanel();
		mainPanel.init(profileSpecified);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("<HTML>"
				+ StringUtil.split(
						new PerformAutomaticConflationWizardPanel()
								.getInstructions(), 60)
						.replaceAll("\n", "<BR>") + "</HTML>"),
				BorderLayout.NORTH);
		panel.add(new JPanel(new GridBagLayout()) {
			{
				add(mainPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(13, 40, 0, 0), 0, 0));
			}
		}, BorderLayout.CENTER);
		JOptionPane.showOptionDialog(workbenchFrame, panel,
				"Perform Automatic Conflation?", JOptionPane.OK_OPTION,
				JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "Continue" }, "Continue");
		autoMatch = mainPanel.isAutoMatchSpecified();
		autoAdjust = mainPanel.isAutoAdjustSpecified();
	}

	private void enableUndoableEditReceiver(Block block, TaskFrame taskFrame) {
		taskFrame.getLayerManager().getUndoableEditReceiver().startReceiving();
		try {
			block.yield();
		} finally {
			taskFrame.getLayerManager().getUndoableEditReceiver()
					.stopReceiving();
		}
	}

}