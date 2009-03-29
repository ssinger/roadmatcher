package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.jump.FUTURE_InfoModel;
import com.vividsolutions.jcs.jump.FUTURE_OneLayerAttributeTab;
import com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox.TableTab;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;

public class QueryToolboxPanel extends JPanel {

  public QueryToolboxPanel(WorkbenchContext context) {
    this.context = context;
    try {
      jbInit();
    } catch (Exception ex) {
      Assert.shouldNeverReachHere();
    }
    datasetComboBox.setRenderer(new LayerNameRenderer() {

    {
      setIndicatingEditability(false);
      setIndicatingProgress(false, null);
    }
    });
    initializeViewComboBoxIfNecessary();
  }

  private void initializeViewComboBoxIfNecessary() {
    if (viewComboBox.getModel().getSize() > 0) {
      return;
    }
    viewComboBox.setModel(new DefaultComboBoxModel(filters(context)
        .toArray()));
  }

  public static List filters(WorkbenchContext context) {
    return (List) context.getBlackboard().get(
        QueryToolboxPanel.class.getName() + " - FILTERS",
        new ArrayList(Arrays.asList(new Object[] { new Block() {

      public Object yield(Object feature) {
        return Boolean.TRUE;
      }

      public String toString() {
        return "All Features";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean
            .valueOf(((SourceFeature) feature)
            .getRoadSegment().getResultState() == ResultState.INCONSISTENT);
      }

      public String toString() {
        return "Inconsistent";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean
            .valueOf(((SourceFeature) feature)
            .getRoadSegment().getState() == SourceState.UNKNOWN);
      }

      public String toString() {
        return "Unknown";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean
            .valueOf(((SourceFeature) feature)
            .getRoadSegment().getState() == SourceState.MATCHED_REFERENCE);
      }

      public String toString() {
        return "Matched (Ref)";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean
            .valueOf(((SourceFeature) feature)
            .getRoadSegment().getState() == SourceState.MATCHED_NON_REFERENCE);
      }

      public String toString() {
        return "Matched (NonRef)";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean
            .valueOf(((SourceFeature) feature)
            .getRoadSegment().getState() == SourceState.STANDALONE);
      }

      public String toString() {
        return "Standalone";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean.valueOf(((SourceFeature) feature)
                               .getRoadSegment().isAdjusted());
      }

      public String toString() {
        return "Adjusted";
      }
    }, new Block() {

      public Object yield(Object feature) {
        return Boolean
            .valueOf(((SourceFeature) feature)
            .getRoadSegment().getState() == SourceState.RETIRED);
      }

      public String toString() {
        return "Retired";
      }
    }, new Block() {

      public Object yield(Object feature) {
        String comment = ((SourceFeature) feature)
            .getRoadSegment().getComment();
        return Boolean
            .valueOf(comment != null && comment.length() > 0);
      }

      public String toString() {
        return "Comments";
      }
    } })));
  }

  void datasetComboBox_actionPerformed(ActionEvent e) {
    if (ignoringDatasetComboBoxEvents) {
      return;
    }
    if (!checkTaskFrameProxyActive()) {
      return;
    }
    updateComponents();
  }

  void viewComboBox_actionPerformed(ActionEvent e) {
    if (!checkTaskFrameProxyActive()) {
      return;
    }
    updateComponents();
  }

  private boolean checkTaskFrameProxyActive() {
    if (!(context.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrameProxy)) {
      context.getWorkbench().getFrame().warnUser(
          ErrorMessages.queryToolboxPanel_taskFrameMustBeActive);
      return false;
    }
    if (SpecifyRoadFeaturesTool.createConflationSessionMustBeStartedCheck(
        context).check(null) != null) {
      context
          .getWorkbench()
          .getFrame()
          .warnUser(
          ErrorMessages.queryToolboxPanel_taskFrameWithConflationSessionMustBeActive);
      return false;
    }
    return true;
  }

  public TableTab getTableTab() {
    return tableTab;
  }

  private void ignoreDatasetComboBoxEvents(Block block) {
    boolean originallyIgnoringDatasetComboBoxEvents = ignoringDatasetComboBoxEvents;
    try {
      ignoringDatasetComboBoxEvents = true;
      block.yield();
    } finally {
      ignoringDatasetComboBoxEvents = originallyIgnoringDatasetComboBoxEvents;
    }
  }

  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    northPanel.setLayout(gridBagLayout1);
    datasetLabel.setToolTipText("");
    datasetLabel.setText("Dataset:");
    viewLabel.setText("View:");
    datasetComboBox.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        datasetComboBox_actionPerformed(e);
      }
    });
    viewComboBox.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        viewComboBox_actionPerformed(e);
      }
    });
    this.add(tableTab, BorderLayout.CENTER);
    this.add(northPanel, BorderLayout.NORTH);
    northPanel.add(datasetLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
        0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(datasetComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0,
        0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(viewLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
        2, 4, 2, 4), 0, 0));
    northPanel.add(fillerPanel, new GridBagConstraints(4, 0, 1, 1, 1.0,
        0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(viewComboBox, new GridBagConstraints(3, 0, 1, 1, 0.0,
        0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 4), 0, 0));
  }

  private QueryToolboxPanel preserveDatasetComboBoxSelection(Block block) {
    int i = datasetComboBox.getSelectedIndex();
    try {
      block.yield();
    } finally {
      datasetComboBox.setSelectedIndex(i);
    }
    return this;
  }

  public void updateComponents() {
    //Initialize view combobox lazily, to give plugins a chance to insert
    //their own filters. [Jon Aquino 2004-10-18]
    initializeViewComboBoxIfNecessary();
    //TableTab#setLayer requires that a TaskFrameProxy be active [Jon
    // Aquino
    // 2004-02-09]
    if (!(context.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrameProxy)) {
      return;
    }
    if (!ToolboxModel.instance(context.getLayerManager(), context)
        .isInitialized()) {
      return;
    }
    ignoreDatasetComboBoxEvents(new Block() {

      public Object yield() {
        preserveDatasetComboBoxSelection(new Block() {

          public Object yield() {
            datasetComboBox.setModel(new DefaultComboBoxModel(
                new Object[] {
              ToolboxModel.instance(
                  context.getLayerManager(),
                  context).getSourceLayer(0),
              ToolboxModel.instance(
              context.getLayerManager(),
                context).getSourceLayer(1) }));
                return null;
          }
        });
        datasetComboBox.setSelectedIndex(Math.max(0, datasetComboBox
            .getSelectedIndex()));
        return null;
      }
    });
    tableTab.setLayer((Layer) datasetComboBox.getSelectedItem(), context);
    tableTab.setCriterionForAddingFeatures(context, (Block) viewComboBox
        .getSelectedItem());
    if (GUIUtil.getDescendantOfClass(EnableableToolBar.class, tableTab) != null) {
      //Won't get here if toolbox has not yet been opened
      //[Jon Aquino 2004-02-11]
      ((EnableableToolBar) GUIUtil.getDescendantOfClass(
          EnableableToolBar.class, tableTab)).updateEnabledState();
    }
  }

  private JTable table() {
    return ((JTable) GUIUtil.getDescendantOfClass(JTable.class, tableTab));
  }

  private BorderLayout borderLayout1 = new BorderLayout();

  private WorkbenchContext context;

  private JComboBox datasetComboBox = new JComboBox();

  private JLabel datasetLabel = new JLabel();

  private JPanel fillerPanel = new JPanel();

  private JComboBox viewComboBox = new JComboBox();

  private JLabel viewLabel = new JLabel();

  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  private boolean ignoringDatasetComboBoxEvents = false;

  private JPanel northPanel = new JPanel();

  private TableTab tableTab = new TableTab(null) {
    protected FUTURE_OneLayerAttributeTab createAttributeTab(
        WorkbenchContext context) {
      return new FUTURE_OneLayerAttributeTab(new InfoModel() {
        public LayerTableModel getTableModel(Layer layer) {
          if (!FUTURE_InfoModel
              .protectedGetLayerToTableModelMap(this)
              .containsKey(layer)) {
            FUTURE_InfoModel.protectedGetLayerToTableModelMap(this)
                .put(layer, new LayerTableModel(layer) {
              public boolean isCellEditable(int rowIndex,
                  int columnIndex) {
                return false;
              }
            });
          }

          return (LayerTableModel) FUTURE_InfoModel
              .protectedGetLayerToTableModelMap(this).get(layer);
        }
        }, context, (TaskFrame) context.getWorkbench().getFrame()
            .getActiveInternalFrame(), context);
    }
  };
}