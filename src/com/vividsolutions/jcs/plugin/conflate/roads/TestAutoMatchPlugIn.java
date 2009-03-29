package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.match.RoadMatchOptions;
import com.vividsolutions.jcs.conflate.roads.match.RoadMatcherProcess;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class TestAutoMatchPlugIn extends ThreadedBasePlugIn {


  private RoadMatchOptions matchOptions = new RoadMatchOptions();
  private MultiInputDialog dialog;

  public TestAutoMatchPlugIn() {
  }
  public boolean execute(PlugInContext context) throws Exception {
    dialog = createDialog(context);
    dialog.setVisible(true);
    if (!dialog.wasOKPressed()) {
      return false;
    }
    getDialogValues(dialog);
    return true;
  }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception
    {
      monitor.report("Creating Road Networks");
      ConflationSession session =
          ToolboxModel.instance(context).getSession();
      RoadMatcherProcess rm = new RoadMatcherProcess(session.getSourceNetwork(0),
              session.getSourceNetwork(1));
      matchOptions.setStandaloneEnabled(false);
      rm.match(matchOptions, monitor);
      ToolboxModel.instance(context).updateResultStates(monitor);
    }

    public void initialize(PlugInContext context) throws Exception {
        context
                .getFeatureInstaller()
                .addMainMenuItem(
                        this,
                        new String[]{RoadMatcherToolboxPlugIn.MENU_NAME, "Test"},
                        getName() + "...",
                        false,
                        null,
                        null);
    }
    private static String overlapMatchDistanceField = "Overlap Match Max Distance";
    private static String lengthDifferenceField = "Overlap Match Max Length Difference";
    private static String overlapDifferenceField = "Overlap Match Overlap Difference";

    private MultiInputDialog createDialog(PlugInContext context)
    {
      MultiInputDialog dialog = new MultiInputDialog(context
              .getWorkbenchFrame(), getName(), true);
      dialog.addDoubleField(overlapMatchDistanceField, matchOptions.getEdgeMatchOptions().getDistanceTolerance(), 10,
                         "");
      dialog.addDoubleField(lengthDifferenceField, matchOptions.getEdgeMatchOptions().getLengthDifferenceTolerance(), 10,
                         "");
      dialog.addDoubleField(overlapDifferenceField, matchOptions.getEdgeMatchOptions().getOverlapDifference(), 10,
                         "");
      return dialog;
    }

    private void getDialogValues(MultiInputDialog dialog)
    {
      matchOptions.getEdgeMatchOptions().setDistanceTolerance(dialog.getDouble(overlapMatchDistanceField));
      matchOptions.getEdgeMatchOptions().setLengthDifferenceTolerance(dialog.getDouble(lengthDifferenceField));
      matchOptions.getEdgeMatchOptions().setOverlapDifference(dialog.getDouble(overlapDifferenceField));
    }
}