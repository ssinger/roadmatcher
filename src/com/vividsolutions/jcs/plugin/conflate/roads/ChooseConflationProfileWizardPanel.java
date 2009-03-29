package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.*;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jcs.jump.FUTURE_AbstractWizardPanel;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.ColumnBasedTableModel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
//Change the superclass to JPanel or AbstractWizardPanel, depending on
//whether or not you are using the JBuilder GUI designer
//[Jon Aquino 2004-04-21]
public class ChooseConflationProfileWizardPanel extends
        FUTURE_AbstractWizardPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JRadioButton noProfileRadioButton = new JRadioButton();
    private JRadioButton useExistingProfileRadioButton = new JRadioButton();
    private JScrollPane scrollPane = new JScrollPane();
    private class MyTableModel extends ColumnBasedTableModel {
        public int getRowCount() {
            return getProfileFiles().size();
        }
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        public MyTableModel() {
            setColumns(Arrays.asList(new Column[]{new Column("Name",
                    String.class) {
                public Object getValueAt(int rowIndex) {
                    return ((File) getProfileFiles().get(rowIndex)).getName();
                }
                public void setValueAt(Object value, int rowIndex) {
                    throw new UnsupportedOperationException();
                }
            }}));
        }
        public void fireTableStructureChanged() {
            fireTableChanged(new TableModelEvent(this));
        }
    }
    private JTable table = new JTable(new MyTableModel()) {
        {
            addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {
                }
                public void mouseMoved(MouseEvent e) {
                    if (table.rowAtPoint(e.getPoint()) != -1) {
                        setToolTipText(((File) getProfileFiles().get(
                                table.rowAtPoint(e.getPoint()))).getPath());
                    }
                }
            });
        }
    };
    private Map dataMap;
    public static final String SELECTED_PROFILE_KEY = ChooseConflationProfileWizardPanel.class
            .getName()
            + " - SELECTED PROFILE";
    public static final String NO_PROFILE_KEY = ChooseConflationProfileWizardPanel.class
            .getName()
            + " - NO PROFILE";
    private WorkbenchContext context;
    public ChooseConflationProfileWizardPanel(WorkbenchContext context) {
        this.context = context;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        table.setShowGrid(false);
        table.setTableHeader(null);
        initialize(noProfileRadioButton);
        initialize(useExistingProfileRadioButton);
        table.getSelectionModel().addListSelectionListener(
                createListSelectionListener());
        //Listen for mouse clicks too, as user may click on a selected row,
        //which will not trigger ListSectionListener#valueChanged.
        //[Jon Aquino 2004-04-26]
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                createListSelectionListener().valueChanged(null);
            }
        });
        updateComponents();
    }
    private ListSelectionListener createListSelectionListener() {
        return new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                try {
                    if (table.getRowCount() == 0) {
                        return;
                    }
                    useExistingProfileRadioButton.setSelected(true);
                    //Strange -- #doClick didn't work after adding first item
                    //to list, but #setSelected(true) did. [Jon Aquino]
                } finally {
                    updateComponents();
                }
            }
        };
    }
    private void initialize(JRadioButton radioButton) {
        buttonGroup.add(radioButton);
        radioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateComponents();
            }
        });
    }
    private void updateComponents() {
        removeButton.setEnabled(table.getSelectedRowCount() > 0);
        useExistingProfileRadioButton.setEnabled(table.getRowCount() > 0);
        if (!useExistingProfileRadioButton.isEnabled()
                && useExistingProfileRadioButton.isSelected()) {
            noProfileRadioButton.setSelected(true);
        }
        if (useExistingProfileRadioButton.isSelected()
                && table.getRowCount() > 0 && table.getSelectedRowCount() == 0) {
            table.getSelectionModel().setSelectionInterval(0, 0);
        }
        table
                .setBackground(useExistingProfileRadioButton.isSelected() ? new JTable()
                        .getBackground()
                        : getBackground());
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(
                useExistingProfileRadioButton.isSelected() ? new JTable()
                        .getBackground() : getBackground());
        fireInputChanged();
    }
    private Collection getSelectedProfileFiles() {
        Collection selectedProfileFiles = new ArrayList();
        int selectedRows[] = table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            selectedProfileFiles.add(getProfileFiles().get(selectedRows[i]));
        }
        return selectedProfileFiles;
    }
    private ButtonGroup buttonGroup = new ButtonGroup();
    private JPanel buttonPanel = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JButton addButton = new JButton();
    private JButton removeButton = new JButton();
    public static final String PROFILE_NAME = ChooseConflationProfileWizardPanel.class
            .getName()
            + " - PROFILE NAME";
    public static final String WIZARD_PROFILE_FILES = ChooseConflationProfileWizardPanel.class
            .getName()
            + " - WIZARD PROFILE FILES";
    void jbInit() throws Exception {
        noProfileRadioButton.setText("No profile");
        this.setLayout(gridBagLayout1);
        useExistingProfileRadioButton.setText("Use existing profile:");
        setPreferredSize(new Dimension(500, 200));
        buttonPanel.setLayout(gridBagLayout2);
        addButton.setText("Add To List...");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addButton_actionPerformed(e);
            }
        });
        removeButton.setText("Delete From List");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeButton_actionPerformed(e);
            }
        });
        this.add(noProfileRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(useExistingProfileRadioButton, new GridBagConstraints(0, 1, 1,
                1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(scrollPane, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        0, 0, 0, 0), 0, 0));
        this.add(buttonPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(
                        0, 4, 0, 0), 0, 0));
        buttonPanel.add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 4, 0), 0, 0));
        buttonPanel.add(removeButton, new GridBagConstraints(0, 1, 1, 1, 0.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        scrollPane.getViewport().add(table, null);
    }
    public void enteredFromLeft(Map dataMap) {
        this.dataMap = dataMap;
        updateTable();
        noProfileRadioButton.setSelected(true);
        updateComponents();
    }
    private MyTableModel getTableModel() {
        return (MyTableModel) table.getModel();
    }
    public void exitingToRight() throws Exception {
        if (!noProfileRadioButton.isSelected()) {
            if (!getSelectedProfileFile().exists()) {
                throw new Exception("File not found: "
                        + getSelectedProfileFile());
            }
            if (!getSelectedProfileFile().isFile()) {
                throw new Exception("Not a file: " + getSelectedProfileFile());
            }
            try {
                Profile.createProfile(getSelectedProfileFile());
            } catch (Exception e) {
                throw new Exception("A problem occurred while reading "
                        + getSelectedProfileFile() + ": "
                        + WorkbenchFrame.toMessage(e), e);
            }
        }
        dataMap.put(SELECTED_PROFILE_KEY,
                noProfileRadioButton.isSelected() ? new Profile("A", "B") : Profile
                        .createProfile(getSelectedProfileFile()));
        dataMap.put(NO_PROFILE_KEY, Boolean.valueOf(noProfileRadioButton
                .isSelected()));
        dataMap.put(PROFILE_NAME, noProfileRadioButton.isSelected() ? "none"
                : (getSelectedProfileFile()).getName());
    }
    private File getSelectedProfileFile() {
        return (File) getProfileFiles().get(table.getSelectedRow());
    }
    public String getInstructions() {
        return "Choose a profile to specify the initial settings for the conflation session.";
    }
    public boolean isInputValid() {
        return noProfileRadioButton.isSelected()
                || table.getSelectedRowCount() == 1;
    }
    public String getNextID() {
        return SelectInputLayersWizardPanel.class.getName();
    }
    void addButton_actionPerformed(ActionEvent e) {
        File newProfileFile = ChooseConflationProfileWizardPanel
                .prompt(
                        "Add Profile To List",
                        SwingUtilities
                                .windowForComponent(ChooseConflationProfileWizardPanel.this),
                        context);
        if (newProfileFile == null) {
            return;
        }
        if (getProfileFiles().contains(newProfileFile)) {
            //Don't just return -- we want the file to get selected.
            //[Jon Aquino 2004-04-26]
            getProfileFiles().remove(newProfileFile);
        }
        getProfileFiles().add(newProfileFile);
        updateTable();
        table.getSelectionModel().setSelectionInterval(
                getProfileFiles().indexOf(newProfileFile),
                getProfileFiles().indexOf(newProfileFile));
        updateComponents();
    }
    private void updateTable() {
        sortProfileFiles();
        getTableModel().fireTableStructureChanged();
    }
    private void sortProfileFiles() {
        Collections.sort(getProfileFiles(), new Comparator() {
            public int compare(Object o1, Object o2) {
                return compareNames((File) o1, (File) o2);
            }
            private int compareNames(File a, File b) {
                return a.getName().toLowerCase().compareTo(
                        b.getName().toLowerCase());
            }
        });
    }
    void removeButton_actionPerformed(ActionEvent e) {
        for (Iterator i = getSelectedProfileFiles().iterator(); i.hasNext();) {
            File selectedProfileFile = (File) i.next();
            getProfileFiles().remove(selectedProfileFile);
        }
        updateTable();
        updateComponents();
    }
    private List getProfileFiles() {
        return dataMap == null ? Collections.EMPTY_LIST : (List) dataMap
                .get(WIZARD_PROFILE_FILES);
    }
    public static File prompt(String title, Component parent,
            WorkbenchContext context) {
        JFileChooser chooser = GUIUtil
                .createJFileChooserWithExistenceChecking();
        chooser.setDialogTitle(title);
        chooser.setMultiSelectionEnabled(false);
        if (new SaveProfileAsPlugIn().getLastFile(context) != null) {
            chooser.setSelectedFile(new SaveProfileAsPlugIn()
                    .getLastFile(context));
        }
        GUIUtil.removeChoosableFileFilters(chooser);
        FileFilter filter = GUIUtil.createFileFilter("Profiles",
                new String[]{new SaveProfileAsPlugIn().getExtension()});
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        chooser.setFileFilter(filter);
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(parent)) {
            return null;
        }
        File selectedFile = toJava2XMLCompatibleFile(chooser.getSelectedFile());
        new SaveProfileAsPlugIn().setLastFile(selectedFile, context);
        return selectedFile;
    }
    private static File toJava2XMLCompatibleFile(File file) {
        //The file chooser was returning Win32ShellFolder2's, which aren't
        //compatible with Java2XML [Jon Aquino 2004-04-23]
        return new File(file.getPath());
    }
}