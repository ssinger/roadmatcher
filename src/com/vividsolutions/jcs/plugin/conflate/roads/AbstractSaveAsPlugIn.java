package com.vividsolutions.jcs.plugin.conflate.roads;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import com.vividsolutions.jcs.jump.FUTURE_FileUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
public abstract class AbstractSaveAsPlugIn extends AbstractFileChooserPlugIn {
	public void initialize(PlugInContext context) throws Exception {
		RoadMatcherExtension.addMainMenuItemWithJava14Fix(context, this,
				getMenuPath(), getMenuItemName(), false, null,
				createEnableCheck(context));
	}
	protected MultiEnableCheck createEnableCheck(PlugInContext context) {
		return super
				.createEnableCheck(context)
				.add(
						context
								.getCheckFactory()
								.createWindowWithLayerViewPanelMustBeActiveCheck())
				.add(
						SpecifyRoadFeaturesTool
								.createConflationSessionMustBeStartedCheck(context
										.getWorkbenchContext()));
	}
	public boolean execute(final PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		JFileChooser chooser = createChooser(context);
		if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(context
				.getWorkbenchFrame())) {
			return false;
		}
		chooser.setSelectedFile(FUTURE_FileUtil.addExtensionIfNone(chooser
				.getSelectedFile(), getExtension()));
		setLastFile(chooser.getSelectedFile(), context.getWorkbenchContext());
		if (!(this instanceof ThreadedPlugIn)) {
			saveAs(getLastFile(context.getWorkbenchContext()),
					new DummyTaskMonitor(), context);
		}
		return true;
	}
	protected JFileChooser createChooser(final PlugInContext context) {
		JFileChooser chooser = GUIUtil
				.createJFileChooserWithOverwritePrompting();
		chooser.setDialogTitle(getName());
		chooser.setSelectedFile(getInitialFile(chooser.getCurrentDirectory(),
				context));
		GUIUtil.removeChoosableFileFilters(chooser);
		FileFilter filter = GUIUtil.createFileFilter(
				getFileFilterDescription(), new String[]{getExtension()});
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
		chooser.setFileFilter(filter);
		return chooser;
	}
	public void run(TaskMonitor monitor, PlugInContext context)
			throws Exception {
		saveAs(getLastFile(context.getWorkbenchContext()), monitor, context);
	}
	protected abstract void saveAs(File file, TaskMonitor monitor,
			PlugInContext context) throws Exception;
}