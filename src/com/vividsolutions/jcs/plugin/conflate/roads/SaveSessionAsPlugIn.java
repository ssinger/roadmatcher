package com.vividsolutions.jcs.plugin.conflate.roads;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
public class SaveSessionAsPlugIn extends AbstractSaveSessionPlugIn {
	public static final String DESCRIPTION = "Roads Session";
	private static final String LAST_SELECTED_FILE_KEY = SaveSessionAsPlugIn.class + " - LAST SELECTED FILE";
	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		if (session(context).getFile() != null) {
			fileChooser(context).setSelectedFile(session(context).getFile());
		} else {
			fileChooser(context).setSelectedFile(
					defaultFilename(session(context), fileChooser(context)
							.getCurrentDirectory()));
		}
		if (JFileChooser.APPROVE_OPTION != fileChooser(context).showSaveDialog(
				context.getWorkbenchFrame())) {
			return false;
		}
		File file = fileChooser(context).getSelectedFile();
		if (GUIUtil.getExtension(file) == "") {
			String path = file.getAbsolutePath();
			if (!path.endsWith(".")) {
				path += ".";
			}
			path += EXTENSION;
			file = new File(path);
		}
		session(context).setName(GUIUtil.nameWithoutExtension(file));
		context.getTask().setName(session(context).getName());
		session(context).setFile(file);
		PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(
				LAST_SELECTED_FILE_KEY, file.getPath());
		return true;
	}
	public static File defaultFilename(ConflationSession session, File directory) {
		return new File(directory, session.getName() + "." + EXTENSION);
	}
	private JFileChooser fileChooser(PlugInContext context) {
		if (fileChooser == null) {
			fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
			fileChooser.setDialogTitle("Save Roads Session");
			GUIUtil.removeChoosableFileFilters(fileChooser(context));
			fileChooser.addChoosableFileFilter(FILE_FILTER);
			fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
			fileChooser.setFileFilter(FILE_FILTER);
			if (PersistentBlackboardPlugIn.get(context.getWorkbenchContext())
					.get(LAST_SELECTED_FILE_KEY) != null) {
				fileChooser.setCurrentDirectory(new File(
						(String) PersistentBlackboardPlugIn.get(
								context.getWorkbenchContext()).get(
								LAST_SELECTED_FILE_KEY)).getParentFile());
			}
		}
		return fileChooser;
	}
	private JFileChooser fileChooser;
	public static final String EXTENSION = "rms";
	public static final FileFilter FILE_FILTER = GUIUtil.createFileFilter(
			DESCRIPTION, new String[]{EXTENSION});
	protected String getMenuText() {
		return getName() + "...";
	}
}