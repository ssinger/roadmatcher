package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.jump.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool.ModifierKeySpec;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DeleteVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.InsertVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveVertexTool;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;

public class RoadMatcherToolboxPlugIn extends FUTURE_ToolboxPlugIn {
	public static final String VIEW_MENU_NAME = "View";

	public static final String TOOLS_MENU_NAME = "Tools";

	private void add(CursorTool tool, WorkbenchToolBar editingToolBar,
			ToolboxDialog toolbox) {
		FUTURE_ToolboxDialog._registerButton(toolbox, editingToolBar
				.addCursorTool(tool).getButton());
	}

	private void addToButtonToIconMap(AbstractButton button, Map buttonToIconMap) {
		buttonToIconMap.put(button, button.getIcon());
	}

	public CursorTool createDeleteVertexTool(final WorkbenchContext context) {
		return new FUTURE_DeleteVertexTool(new EnableCheckFactory(context)) {
			public String getName() {
				//Can't use auto-naming mechanism because this is
				//an anonymous class [Jon Aquino 2004-03-25]
				return StringUtil.toFriendlyName(DeleteVertexTool.class
						.getName(), "Tool");
			}

			protected void commit(List verticesDeleted, List transactions) {
				if (!new ConstraintChecker(context.getWorkbench().getFrame())
						.proceedWithAdjusting(transactions, context)) {
					return;
				}
				if (!checkLineSegmentLengths(transactions,
						"Delete vertex anyway", "DELETE VERTEX TOOL", context)) {
					return;
				}
				super.commit(verticesDeleted, transactions);
			}
		};
	}

	public String getName() {
		return "RoadMatcher Toolbox";
	}

	public CursorTool createMoveVertexTool(final WorkbenchContext context) {
		return new FUTURE_MoveVertexTool(new EnableCheckFactory(context)) {
			public String getName() {
				//Can't use auto-naming mechanism because this is
				//an anonymous class [Jon Aquino 2004-03-25]
				return StringUtil.toFriendlyName(
						MoveVertexTool.class.getName(), "Tool");
			}

			protected void commit(Collection transactions) {
				if (!new ConstraintChecker(context.getWorkbench().getFrame())
						.proceedWithAdjusting(transactions, context)) {
					return;
				}
				if (!checkLineSegmentLengths(transactions,
						"Move vertex anyway", "MOVE VERTEX TOOL", context)) {
					return;
				}
				super.commit(transactions);
			}
		};
	}

	private boolean checkLineSegmentLengths(Collection transactions,
			String proceedButtonText, String doNotShowAgainID,
			final WorkbenchContext context) {
		for (Iterator i = transactions.iterator(); i.hasNext();) {
			EditTransaction transaction = (EditTransaction) i.next();
			if (!checkLineSegmentLengths(transaction, proceedButtonText,
					doNotShowAgainID, context)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkLineSegmentLengths(EditTransaction transaction,
			String proceedButtonText, String doNotShowAgainID,
			final WorkbenchContext context) {
		for (int i = 0; i < transaction.size(); i++) {
			if (!AdjustEndpointOperation.checkLineSegmentLength(
					proceedButtonText, (LineString) transaction.getGeometry(i),
					RoadMatcherToolboxPlugIn.this.getClass().getName() + " - "
							+ doNotShowAgainID + " - DO NOT SHOW AGAIN",
					context)) {
				return false;
			}
		}
		return true;
	}

	public CursorTool createInsertVertexTool(final WorkbenchContext context) {
		return new InsertVertexTool(new EnableCheckFactory(context)) {
			public String getName() {
				//Can't use auto-naming mechanism because this is
				//an anonymous class [Jon Aquino 2004-03-25]
				return StringUtil.toFriendlyName(InsertVertexTool.class
						.getName(), "Tool");
			}

			protected void gestureFinished(Geometry newGeometry,
					Coordinate newVertex, SegmentContext segment) {
				if (segment.getFeature() instanceof SourceFeature) {
					if (!new ConstraintChecker(context.getWorkbench()
							.getFrame()).proceedWithAdjusting(segment
							.getFeature(), (LineString) newGeometry, context)) {
						return;
					}
					if (!AdjustEndpointOperation
							.checkLineSegmentLength(
									"Insert vertex anyway",
									(LineString) newGeometry,
									RoadMatcherToolboxPlugIn.this.getClass()
											.getName()
											+ " - INSERT VERTEX TOOL - DO NOT SHOW AGAIN",
									context)) {
						return;
					}
				}
				super.gestureFinished(newGeometry, newVertex, segment);
			}
		};
	}

	private RetireSegmentTool createRetireTool0(WorkbenchContext context) {
		return new RetireSegmentTool(true, false, null,
				"retire-tool-button.png", Color.red, context);
	}

	private RetireSegmentTool createRetireTool1(WorkbenchContext context) {
		return new RetireSegmentTool(false, true, null,
				"retire-tool-button.png", Color.blue, context);
	}

	public ToolboxPanel getToolboxPanel() {
		return toolboxPanel;
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getWorkbenchContext().getBlackboard().put(INSTANCE_KEY, this);
		createMainMenuItem(new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
				RoadMatcherToolboxPlugIn.VIEW_MENU_NAME }, null, context
				.getWorkbenchContext());
		FeatureInstaller.childMenuItem(
				getName(),
				FeatureInstaller.childMenuItem(
						RoadMatcherToolboxPlugIn.VIEW_MENU_NAME,
						FeatureInstaller.childMenuItem(
								RoadMatcherToolboxPlugIn.MENU_NAME, context
										.getWorkbenchFrame().getJMenuBar())))
				.setText("Toolbox");
	}

	protected void initializeToolbox(final ToolboxDialog toolbox) {
		toolboxPanel = new ToolboxPanel(toolbox.getContext());
		toolbox.getCenterPanel().add(toolboxPanel, BorderLayout.CENTER);
		toolboxPanel.setButtonToIconMaps(installTools(toolbox));
		toolbox.setInitialLocation(new GUIUtil.Location(20, true, 20, false));
		//Don't use ToolboxStateManager -- we don't want the
		//Colour Scheme combobox to be context-sensitive.
		//[Jon Aquino 2004-02-19]
		toolbox.addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				toolboxPanel.updateComponents();
				toolbox.updateEnabledState();
			}
		});
		GUIUtil.addInternalFrameListener(toolbox.getContext().getWorkbench()
				.getFrame().getDesktopPane(), GUIUtil
				.toInternalFrameListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						toolboxPanel.updateComponents();
						toolbox.updateEnabledState();
					}
				}));
	}

	private FUTURE_QuasimodeTool installStandardQuasimodes(CursorTool tool) {
		return (FUTURE_QuasimodeTool) new FUTURE_QuasimodeTool(tool).add(
				new ModifierKeySpec(true, false, false), commitTool).add(
				new ModifierKeySpec(false, true, false), splitTool);
	}

	private Map[] installTools(final ToolboxDialog toolbox) {
		Map[] buttonToIconMaps = new Map[] { new HashMap(), new HashMap() };
		commitTool = createCommitTool(toolbox.getContext());
		splitTool = createCreateSplitNodeTool(toolbox.getContext());
		CursorTool commitTool0 = createCommitTool0(toolbox.getContext());
		CursorTool commitTool1 = createCommitTool1(toolbox.getContext());
		SmartCreateSplitNodeTool splitTool0 = createCreateSplitNodeTool0(toolbox
				.getContext());
		SmartCreateSplitNodeTool splitTool1 = createCreateSplitNodeTool1(toolbox
				.getContext());
		toolbox.add(installStandardQuasimodes(commitTool).add(
				new ModifierKeySpec(true, false, false), splitTool).add(
				new ModifierKeySpec(false, true, false), commitTool0).add(
				new ModifierKeySpec(true, true, false), commitTool1));
		toolbox.add(installStandardQuasimodes(new PathMatchTool(toolbox
				.getContext())));
		toolbox.getToolBar().addSeparator(); // --------------------------------
		WorkbenchToolBar.ToolConfig retireToolConfig = toolbox
				.add(installStandardQuasimodes(
						createRetireTool(toolbox.getContext())).add(
						new ModifierKeySpec(false, true, false),
						createRetireTool0(toolbox.getContext())).add(
						new ModifierKeySpec(true, true, false),
						createRetireTool1(toolbox.getContext())).add(
						new ModifierKeySpec(true, true, false), commitTool));
		toolbox.getToolBar().addSeparator(); // --------------------------------
		toolbox.add(installStandardQuasimodes(splitTool).add(
				new ModifierKeySpec(false, true, false), splitTool0).add(
				new ModifierKeySpec(true, true, false), splitTool1));
		toolbox.add(installStandardQuasimodes(new MoveSplitNodeTool()));
		toolbox.add(installStandardQuasimodes(createDeleteSplitNodeTool(toolbox
				.getContext())));
		toolbox.getToolBar().addSeparator(); // --------------------------------
		//Drop shift quasimode, since SelectPathTool uses Shift
		//[Jon Aquino 2004-02-27]
		toolbox.add(installStandardQuasimodes(
				new DefinePathsTool(toolbox.getContext())).remove(
				new ModifierKeySpec(false, true, false)));
		WorkbenchToolBar editingToolBar = new WorkbenchToolBar(toolbox
				.getContext(), toolbox.getContext().getWorkbench().getFrame()
				.getToolBar().getButtonGroup());
		toolbox.addToolBar(); // =================================
		addToButtonToIconMap(toolbox.add(
				installStandardQuasimodes(commitTool0).add(
						new ModifierKeySpec(true, false, false), splitTool)
						.add(new ModifierKeySpec(false, true, false),
								commitTool).add(
								new ModifierKeySpec(true, true, false),
								commitTool1)).getButton(), buttonToIconMaps[0]);
		addToButtonToIconMap(toolbox.add(
				installStandardQuasimodes(commitTool1).add(
						new ModifierKeySpec(true, false, false), splitTool)
						.add(new ModifierKeySpec(false, true, false),
								commitTool0).add(
								new ModifierKeySpec(true, true, false),
								commitTool)).getButton(), buttonToIconMaps[1]);
		toolbox.getToolBar().addSeparator(); // --------------------------------
		toolbox.add(installStandardQuasimodes(
				new RevertSegmentTool(toolbox.getContext())).remove(
				new ModifierKeySpec(true, false, false)).add(
				new ModifierKeySpec(true, false, false), new RevertAllTool())
				.add(new ModifierKeySpec(true, true, true),
						new RevertToOriginalSegmentTool(toolbox.getContext())));
		toolbox.getToolBar().addSeparator(); // --------------------------------
		addToButtonToIconMap(toolbox
				.add(
						installStandardQuasimodes(splitTool0).add(
								new ModifierKeySpec(false, true, false),
								splitTool).add(
								new ModifierKeySpec(true, true, false),
								splitTool1)).getButton(), buttonToIconMaps[0]);
		addToButtonToIconMap(toolbox
				.add(
						installStandardQuasimodes(splitTool1).add(
								new ModifierKeySpec(false, true, false),
								splitTool0).add(
								new ModifierKeySpec(true, true, false),
								splitTool)).getButton(), buttonToIconMaps[1]);
		toolbox
				.add(installStandardQuasimodes(new CreateIntersectionSplitNodeTool(
						toolbox.getContext())));
		editingToolBar.setBorder(null); // =================================
		editingToolBar.setFloatable(false);
		toolboxPanel.getNorthPanel().add(
				editingToolBar,
				new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(4, 0, 0, 0), 0, 0));
		EnableCheckFactory checkFactory = new EnableCheckFactory(toolbox
				.getContext());
		add(installStandardQuasimodes(new SelectFeaturesTool()).remove(
				new ModifierKeySpec(false, true, false)), editingToolBar,
				toolbox);
		add(installStandardQuasimodes(createInsertVertexTool(toolbox
				.getContext())), editingToolBar, toolbox);
		add(installStandardQuasimodes(createDeleteVertexTool(toolbox
				.getContext())), editingToolBar, toolbox);
		add(
				installStandardQuasimodes(createMoveVertexTool(toolbox
						.getContext())), editingToolBar, toolbox);
		editingToolBar.addSeparator(); // --------------------------------
		add(installStandardQuasimodes(new AutoConnectEndpointTool(toolbox
				.getContext())), editingToolBar, toolbox);
		add(installStandardQuasimodes(new AdjustEndpointTool(toolboxPanel
				.getAdjustPanel())), editingToolBar, toolbox);
		return buttonToIconMaps;
	}

	private CursorTool commitTool;

	private SmartCreateSplitNodeTool splitTool;

	private ToolboxPanel toolboxPanel;

	public static CommitOrPreciseMatchTool createCommitTool(
			WorkbenchContext context) {
		return new CommitOrPreciseMatchTool(CommitTool.BOTH_LAYERS, null,
				"commit-tool-button.png", Color.black, context) {
			public String getName() {
				return "Match/Commit Segment";
			}
		};
	}

	public static CursorTool createCommitTool0(final WorkbenchContext context) {
		return new CommitOrPreciseMatchTool(CommitTool.SOURCE_LAYER_0, null,
				"commit-tool-0-button.png", Color.red, context) {
			public String getName() {
				return "Match/Commit " + datasetName(0, context) + " Segment";
			}
		};
	}

	public static CursorTool createCommitTool1(final WorkbenchContext context) {
		return new CommitOrPreciseMatchTool(CommitTool.SOURCE_LAYER_1, null,
				"commit-tool-1-button.png", Color.blue, context) {
			public String getName() {
				return "Match/Commit " + datasetName(1, context) + " Segment";
			}
		};
	}

	public static SmartCreateSplitNodeTool createCreateSplitNodeTool(
			WorkbenchContext context) {
		return new SmartCreateSplitNodeTool(true, true, null,
				"create-split-node-tool-button.gif", Color.red, context) {
			public String getName() {
				return "Create Split Node";
			}
		};
	}

	public static SmartCreateSplitNodeTool createCreateSplitNodeTool0(
			final WorkbenchContext context) {
		return new SmartCreateSplitNodeTool(true, false, null,
				"create-split-node-tool-0-button.gif", Color.red, context) {
			public String getName() {
				return "Create " + datasetName(0, context) + " Split Node";
			}
		};
	}

	public static SmartCreateSplitNodeTool createCreateSplitNodeTool1(
			final WorkbenchContext context) {
		return new SmartCreateSplitNodeTool(false, true, null,
				"create-split-node-tool-1-button.gif", Color.blue, context) {
			public String getName() {
				return "Create " + datasetName(1, context) + " Split Node";
			}
		};
	}

	public static DeleteSplitNodeTool createDeleteSplitNodeTool(
			WorkbenchContext context) {
		return new DeleteSplitNodeTool(true, true, null,
				"delete-split-node-tool-button.png", Color.black, context);
	}

	public static RetireSegmentTool createRetireTool(WorkbenchContext context) {
		return new RetireSegmentTool(true, true, null,
				"retire-tool-button.png", Color.black, context);
	}

	public static String datasetName(int i, WorkbenchContext context) {
		String defaultDatasetName = "Layer " + i;
		if (context.getLayerManager() == null) {
			return defaultDatasetName;
		}
		if (!ToolboxModel.instance(context.getLayerManager(), context)
				.isInitialized()) {
			return defaultDatasetName;
		}
		return ToolboxModel.instance(context.getLayerManager(), context)
				.getSession().getSourceNetwork(i).getName();
	}

	public static RoadMatcherToolboxPlugIn instance(WorkbenchContext context) {
		return (RoadMatcherToolboxPlugIn) context.getBlackboard().get(
				INSTANCE_KEY);
	}

	private static final String INSTANCE_KEY = RoadMatcherToolboxPlugIn.class
			.getName()
			+ " - INSTANCE";

	public static final String MENU_NAME = "RoadMatcher";

	public static final String RESULT_MENU_NAME = "Result";
}