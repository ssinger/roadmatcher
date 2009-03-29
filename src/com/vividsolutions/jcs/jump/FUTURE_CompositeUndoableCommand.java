package com.vividsolutions.jcs.jump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.UndoableCommand;

public class FUTURE_CompositeUndoableCommand extends UndoableCommand {

	public FUTURE_CompositeUndoableCommand(String name) {
		super(name);
	}

	public FUTURE_CompositeUndoableCommand add(UndoableCommand undoableCommand) {
		undoableCommands.add(undoableCommand);
		return this;
	}

	private List undoableCommands = new ArrayList();

	protected void dispose() {
		for (Iterator i = undoableCommands.iterator(); i.hasNext();) {
			UndoableCommand undoableCommand = (UndoableCommand) i.next();
			FUTURE_UndoableCommand._dispose(undoableCommand);
		}
	}

	public void execute() {
		for (Iterator i = undoableCommands.iterator(); i.hasNext();) {
			UndoableCommand undoableCommand = (UndoableCommand) i.next();
			undoableCommand.execute();
		}
	}

	public void unexecute() {
		for (Iterator i = CollectionUtil.reverse(
				new ArrayList(undoableCommands)).iterator(); i.hasNext();) {
			UndoableCommand undoableCommand = (UndoableCommand) i.next();
			undoableCommand.unexecute();
		}
	}

	public List getUndoableCommands() {
		return Collections.unmodifiableList(undoableCommands);
	}
}