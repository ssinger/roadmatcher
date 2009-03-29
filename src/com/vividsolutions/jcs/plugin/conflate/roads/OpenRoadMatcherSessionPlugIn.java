package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import bsh.EvalError;
import bsh.Interpreter;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_LayerManager;
import com.vividsolutions.jcs.jump.FUTURE_MonitoredBufferedInputStream;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jcs.plugin.issuelog.IssueLog;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class OpenRoadMatcherSessionPlugIn extends AbstractOpenPlugIn implements
		ThreadedPlugIn {
	protected String getMenuItemName() {
		return "Open Session...";
	}

	public static class VersionException extends Exception {

		public VersionException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	public static ConflationSession open(File file, final TaskMonitor monitor)
			throws Exception {
		ConflationSession session;
		FileInputStream fileInputStream = new FileInputStream(file);
		try {
			BufferedInputStream bufferedInputStream = new FUTURE_MonitoredBufferedInputStream(
					fileInputStream, file.length(), monitor);
			try {
				GZIPInputStream gzipInputStream = new GZIPInputStream(
						bufferedInputStream);
				try {
					ObjectInputStream objectInputStream = new ObjectInputStream(
							gzipInputStream);
					String version = null;
					try {
						try {
							version = (String) objectInputStream.readObject();
						} catch (InvalidClassException e) {
							version = "1.1.1";
							throw e;
						}
						session = (ConflationSession) objectInputStream
								.readObject();
					} catch (InvalidClassException e) {
						throw new VersionException(
								"Unable to load session. RoadMatcher "
										+ version
										+ " sessions are not compatible with this version ("
										+ RoadMatcherExtension.VERSION + ")", e);
					} finally {
						objectInputStream.close();
					}
				} finally {
					gzipInputStream.close();
				}
			} finally {
				bufferedInputStream.close();
			}
		} finally {
			fileInputStream.close();
		}
		buildGraph(session.getSourceNetwork(0));
		buildGraph(session.getSourceNetwork(1));
		session.setFile(file);
		return session;
	}

	public static final String ON_SESSION_LOAD_KEY = OpenRoadMatcherSessionPlugIn.class
			.getName()
			+ " - ON SESSION LOAD";

	private static void buildGraph(RoadNetwork network) {
		for (Iterator i = network.roadSegmentsApparentlyIntersecting(
				network.getApparentEnvelope()).iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			network.getGraph().add(roadSegment);
		}
	}

	protected void open(File file, TaskMonitor monitor,
			final PlugInContext context) throws Exception {
		try {
			openProper(file, monitor, context);
		} catch (VersionException e) {
			JOptionPane.showMessageDialog(context.getWorkbenchFrame(),
					StringUtil.split(e.getMessage(), 80),
					RoadMatcherToolboxPlugIn.MENU_NAME,
					JOptionPane.ERROR_MESSAGE);
			context.getWorkbenchFrame().log(StringUtil.stackTrace(e));
		}
	}

	private void openProper(File file, TaskMonitor monitor,
			final PlugInContext context) throws Exception,
			InstantiationException, IllegalAccessException {
		final TaskFrame newTaskFrame = context.getWorkbenchFrame()
				.addTaskFrame();
		final ConflationSession session = open(file, monitor);
		addLayersForOriginalFeatureCollections(session, newTaskFrame
				.getLayerManager(), context.getWorkbenchContext());
		addLayerForContextFeatureCollection(session, newTaskFrame
				.getLayerManager(), context.getWorkbenchContext());
		ToolboxModel.instance(newTaskFrame.getLayerManager(),
				context.getWorkbenchContext()).initialize(session);
		//Compare classes by name, because after serialization the classes may
		//not be identical [Jon Aquino 2004-01-28]
		if (!session.getConsistencyRule().getClass().getName().equals(
				((ConsistencyConfiguration) ((Class) ApplicationOptionsPlugIn
						.options(context.getWorkbenchContext()).get(
								ConsistencyConfiguration.CURRENT_CLASS_KEY,
								ConsistencyConfiguration.DEFAULT_CLASS))
						.newInstance()).getRule().getClass().getName())) {
			session.setConsistencyRule(ToolboxModel.instance(context)
					.getConsistencyConfiguration().getRule());
			ToolboxModel.instance(context).updateResultStates(monitor);
		}
		new ConflationSessionInitializer().initialize(session, newTaskFrame
				.getTask(), context.getWorkbenchContext());
		if (session.getBlackboard().get(AbstractSaveSessionPlugIn.ISSUES_KEY) != null) {
			//Call #invokeLater to avoid ConcurrentModificationException
			//in LayerManager#fireCategoryChanged [Jon Aquino 2004-03-12]
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					IssueLog
							.instance(newTaskFrame.getLayerManager())
							.getLayer()
							.setFeatureCollection(
									(FeatureCollection) session
											.getBlackboard()
											.get(
													AbstractSaveSessionPlugIn.ISSUES_KEY));
				}
			});
		}
	}

	public static void addLayersForOriginalFeatureCollections(
			final ConflationSession session, LayerManager layerManager,
			WorkbenchContext workbenchContext) {
		//Add 0 after 1 so 0 appears above 1 in the layer list
		//[Jon Aquino 2004-02-09]
		addLayerForOriginalFeatureCollection(1, HighlightManager.instance(
				workbenchContext).getColourScheme().getDefaultColour1(),
				layerManager, session);
		addLayerForOriginalFeatureCollection(0, HighlightManager.instance(
				workbenchContext).getColourScheme().getDefaultColour0(),
				layerManager, session);
	}

	private static void addLayerForOriginalFeatureCollection(int index,
			Color colour, LayerManager layerManager, ConflationSession session) {
		layerManager.addLayerable(StandardCategoryNames.WORKING,
				hide(new Layer(session.getSourceNetwork(index).getName(),
						colour, session.getOriginalFeatureCollection(index),
						layerManager)));
	}

	public static void addLayerForContextFeatureCollection(
			final ConflationSession session, LayerManager layerManager,
			WorkbenchContext workbenchContext) {
		//Add 0 after 1 so 0 appears above 1 in the layer list
		//[Jon Aquino 2004-02-09]
		addLayerForContextFeatureCollection("1, 2", 1, HighlightManager
				.instance(workbenchContext).getColourScheme()
				.getDefaultColour1(), layerManager, session);
		addLayerForContextFeatureCollection("1, 1.9", 0, HighlightManager
				.instance(workbenchContext).getColourScheme()
				.getDefaultColour0(), layerManager, session);
	}

	private static void addLayerForContextFeatureCollection(String linePattern,
			int index, Color colour, LayerManager layerManager,
			ConflationSession session) {
		if (session.getContextFeatureCollection(index).isEmpty()) {
			return;
		}
		layerManager.addLayerable(StandardCategoryNames.WORKING, dash(
				linePattern, new Layer("Context for "
						+ session.getSourceNetwork(index).getName(), colour,
						session.getContextFeatureCollection(index),
						layerManager)));
	}

	private static Layerable dash(String linePattern, Layer layer) {
		layer.getBasicStyle().setRenderingLinePattern(true);
		layer.getBasicStyle().setLinePattern(linePattern);
		return layer;
	}

	private static Layer hide(final Layer layer) {
		FUTURE_LayerManager.createBlockToDisableEventsTemporarily(
				layer.getLayerManager(), new Block() {
					public Object yield() {
						layer.setVisible(false);
						return null;
					}
				}).yield();
		return layer;
	}

	protected String getFileFilterDescription() {
		return SaveSessionAsPlugIn.DESCRIPTION;
	}

	public String getExtension() {
		return SaveSessionAsPlugIn.EXTENSION;
	}

	/**
	 * @param layerManager
	 *            possibly null
	 * @param context
	 *            possibly null
	 */
	public static Interpreter createInterpreter(
			final ConflationSession session, final LayerManager layerManager,
			final WorkbenchContext context) {
		Interpreter interpreter = new Interpreter();
		try {
			interpreter.setClassLoader(OpenRoadMatcherSessionPlugIn.class
					.getClassLoader());
			interpreter.set("wc", context);
			interpreter.set("layerManager", layerManager);
			interpreter.set("session", session);
			interpreter
					.eval("import com.vividsolutions.jcs.plugin.conflate.roads.*;");
			interpreter
					.eval("import com.vividsolutions.jcs.plugin.conflate.roads.CreateThemingLayerPlugIn.Value;");
			interpreter.eval("import com.vividsolutions.jts.geom.*");
			interpreter.eval("import com.vividsolutions.jump.feature.*");
			interpreter.eval("setAccessibility(true)");
		} catch (EvalError e) {
			throw new RuntimeException(e);
		}
		return interpreter;
	}
}