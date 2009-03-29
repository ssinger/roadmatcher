package com.vividsolutions.jcs.jump;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;

public class FUTURE_QuasimodeTool extends QuasimodeTool {

	public FUTURE_QuasimodeTool(CursorTool defaultTool) {
		super(defaultTool);
		FUTURE_LangUtil.setPrivateField("keyListener", keyListener, this,
				QuasimodeTool.class);
	}

	/**
	 * You cannot overwrite a ModifierKeySpec using #add; you must explicitly
	 * remove it with this method.
	 */
	public QuasimodeTool remove(ModifierKeySpec keySpec) {
		((Map) FUTURE_LangUtil.getPrivateField("keySpecToToolMap", this,
				QuasimodeTool.class)).remove(keySpec);
		return this;
	}

	private KeyListener keyListener = new KeyListener() {
		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
			keyStateChanged(e);
		}

		public void keyReleased(KeyEvent e) {
			keyStateChanged(e);
		}

		private void keyStateChanged(KeyEvent e) {
			setTool(e);
		}
	};

	private CursorTool getTool(KeyEvent e) {
		return (CursorTool) FUTURE_LangUtil.invokePrivateMethod("getTool",
				this, QuasimodeTool.class, new Object[] { e },
				new Class[] { KeyEvent.class });
	}

	private void setTool(KeyEvent e) {
		setCursor(getTool(e).getCursor());
		getPanel().setCursor(getCursor());
		setCurrentKeyEvent(e);
		// Fix: Always set the tool on key press or release.
		// Otherwise tooltips (which are dynamically set -- see
		// WorkbenchToolbar#addCursorTool) will often point to an inactive
		// quasimode. Don't bother checking if tool is already active as
		// #setDelegate does that. [Jon Aquino 2004-08-05]
		setDelegate(getTool(e));
	}

	private void setCurrentKeyEvent(KeyEvent e) {
		FUTURE_LangUtil.setPrivateField("currentKeyEvent", e, this,
				QuasimodeTool.class);
	}

	private LayerViewPanel getPanel() {
		return (LayerViewPanel) FUTURE_LangUtil.getPrivateField("panel", this,
				QuasimodeTool.class);
	}

	private void setCursor(Cursor cursor) {
		FUTURE_LangUtil.setPrivateField("cursor", cursor, this,
				QuasimodeTool.class);
	}
}