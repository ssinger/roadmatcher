package com.vividsolutions.jcs.plugin.conflate.roads;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
public abstract class AbstractOpenPlugIn extends AbstractFileChooserPlugIn {

	public void initialize(PlugInContext context) throws Exception {
		RoadMatcherExtension.addMainMenuItemWithJava14Fix(context, this,
				getMenuPath(), getMenuItemName(), false, null,
				createEnableCheck(context));
	}

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		NewSessionPlugIn.warnIfJavaVersionOld(context);
		JFileChooser chooser = GUIUtil
				.createJFileChooserWithExistenceChecking();
		chooser.setDialogTitle(getName());
		chooser.setSelectedFile(getInitialFile(chooser.getCurrentDirectory(),
				context));
		GUIUtil.removeChoosableFileFilters(chooser);
		FileFilter filter = GUIUtil.createFileFilter(
				getFileFilterDescription(), new String[]{getExtension()});
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
		chooser.setFileFilter(filter);
		if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(context
				.getWorkbenchFrame())) {
			return false;
		}
		setLastFile(chooser.getSelectedFile(), context.getWorkbenchContext());
		if (!(this instanceof ThreadedPlugIn)) {
			open(getLastFile(context.getWorkbenchContext()),
					new DummyTaskMonitor(), context);
		}
		return true;
	}
	public void run(TaskMonitor monitor, PlugInContext context)
			throws Exception {
		open(getLastFile(context.getWorkbenchContext()), monitor, context);
	}

	protected abstract void open(File file, TaskMonitor monitor,
			PlugInContext context) throws Exception;

}