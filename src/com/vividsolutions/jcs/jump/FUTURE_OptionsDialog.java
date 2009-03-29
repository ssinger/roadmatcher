package com.vividsolutions.jcs.jump;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
public class FUTURE_OptionsDialog {
    public static OptionsDialog construct(Frame frame, String title,
            boolean modal) {
        final OptionsDialog dialog = (OptionsDialog) FUTURE_LangUtil
                .invokePrivateConstructor(OptionsDialog.class, new Object[]{
                        frame, title, Boolean.valueOf(modal)}, new Class[]{
                        Frame.class, String.class, boolean.class});
        return dialog;
    }
    public static List getTabs(OptionsDialog dialog) {
        List tabs = new ArrayList();
        JTabbedPane tabbedPane = (JTabbedPane) GUIUtil.getDescendantOfClass(
                JTabbedPane.class, dialog);
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabs.add(tabbedPane.getComponentAt(i));
        }
        return tabs;
    }
    public static void setMinWidth(int minWidth, OptionsDialog dialog) {
        JPanel strut = new JPanel();
        strut.setPreferredSize(new Dimension(minWidth, 0));
        dialog.getContentPane().add(strut, BorderLayout.NORTH);
        dialog.pack();
        //A hack that suprisingly works -- surprising because there's already
        //a strut in the north region, and BorderLayout expects *one* component
        //per region. Fix this hack when we move this method into JUMP.
        //[Jon Aquino 2004-05-12]
    }
}