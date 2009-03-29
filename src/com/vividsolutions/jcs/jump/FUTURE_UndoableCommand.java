package com.vividsolutions.jcs.jump;

import com.vividsolutions.jump.workbench.model.UndoableCommand;

public class FUTURE_UndoableCommand {

    public static void _dispose(UndoableCommand undoableCommand) {
        FUTURE_LangUtil.invokePrivateMethod("dispose", undoableCommand,
                UndoableCommand.class, new Object[] {}, new Class[] {});
    }

}
