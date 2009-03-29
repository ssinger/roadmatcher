package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_EventFirer;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jcs.plugin.conflate.roads.WarpLocallyAdjustmentMethod.ModelBasedWarpZone;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;

import java.awt.*;
import java.util.Enumeration;

public class AdjustPanel extends JPanel {
	public static class ShiftAdjustmentTool extends
			OneDragSimpleAdjustEndpointTool {
		public ShiftAdjustmentTool() {
			super(new ShiftAdjustmentMethod());
		}
	}

	public static class WarpAdjustmentTool extends
			OneDragSimpleAdjustEndpointTool {
		public WarpAdjustmentTool() {
			super(new WarpAdjustmentMethod());
		}
	}

	public static class MoveEndpointAdjustmentTool extends
			OneDragSimpleAdjustEndpointTool {
		public MoveEndpointAdjustmentTool() {
			super(new MoveEndpointAdjustmentMethod());
		}
	}

	public static class MyExtendOrClipTool extends
			RoadSegmentEndpointGrabberTool {
		public MyExtendOrClipTool() {
			super(new ExtendOrClipTool(), new Block() {
				public Object yield(Object extendOrClipTool, Object roadSegment) {
					((ExtendOrClipTool) extendOrClipTool)
							.setMyRoadSegment((SourceRoadSegment) roadSegment);
					return null;
				}
			});
		}
	}

	public static class WarpLocallyAdjustmentTool extends
			OneDragSimpleAdjustEndpointTool {
		public WarpLocallyAdjustmentTool() {
			super(new WarpLocallyAdjustmentMethod.ModelBasedWarpZone() {
				private LayerViewPanel panel;

				public LineString adjust(LineString line,
						SourceRoadSegment segment, Terminal terminal,
						Coordinate newTerminalLocation, LayerViewPanel panel) {
					this.panel = panel;
					return super.adjust(line, segment, terminal,
							newTerminalLocation, panel);
				}

				public double getWarpZoneExtent() {
					return Double
							.parseDouble((String) ApplicationOptionsPlugIn
									.options(
											((WorkbenchFrame) SwingUtilities
													.getAncestorOfClass(
															WorkbenchFrame.class,
															panel))
													.getContext())
									.get(
											AdjustPanel.MODEL_BASED_WARP_ZONE_EXTENT_TEXT_KEY,
											AdjustPanel.DEFAULT_MODEL_BASED_WARP_ZONE_EXTENT_TEXT));
				}
			});
		}
	}

	public AdjustPanel(final WorkbenchContext context) {
		this.context = context;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(warpSelectedVerticesRadioButton);
		buttonGroup.add(warpLocallyRadioButton);
		buttonGroup.add(moveEndpointRadioButton);
		buttonGroup.add(warpRadioButton);
		buttonGroup.add(shiftRadioButton);
		buttonGroup.add(extendOrClipRadioButton);
		warpSelectedVerticesRadioButton
				.setSelected(WarpSelectedVerticesTool.class == currentToolClass());
		warpLocallyRadioButton
				.setSelected(WarpLocallyAdjustmentTool.class == currentToolClass());
		warpRadioButton
				.setSelected(WarpAdjustmentTool.class == currentToolClass());
		shiftRadioButton
				.setSelected(ShiftAdjustmentTool.class == currentToolClass());
		moveEndpointRadioButton
				.setSelected(MoveEndpointAdjustmentTool.class == currentToolClass());
		extendOrClipRadioButton
				.setSelected(MyExtendOrClipTool.class == currentToolClass());
		warpZoneExtentTextField.setText((String) ApplicationOptionsPlugIn
				.options(context).get(
						AdjustPanel.MODEL_BASED_WARP_ZONE_EXTENT_TEXT_KEY,
						AdjustPanel.DEFAULT_MODEL_BASED_WARP_ZONE_EXTENT_TEXT));
		warpZoneExtentTextField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						documentChanged();
					}

					private void documentChanged() {
						ApplicationOptionsPlugIn
								.options(context)
								.put(
										AdjustPanel.MODEL_BASED_WARP_ZONE_EXTENT_TEXT_KEY,
										warpZoneExtentTextField.getText()
												.trim());
					}

					public void insertUpdate(DocumentEvent e) {
						documentChanged();
					}

					public void removeUpdate(DocumentEvent e) {
						documentChanged();
					}
				});
		for (Enumeration e = buttonGroup.getElements(); e.hasMoreElements();) {
			JRadioButton radioButton = (JRadioButton) e.nextElement();
			radioButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FUTURE_GUIUtil.visitDescendants(SwingUtilities
							.windowForComponent(AdjustPanel.this), new Block() {
						public Object yield(Object component) {
							if (!(component instanceof WorkbenchToolBar)) {
								return null;
							}
							WorkbenchToolBar workbenchToolBar = (WorkbenchToolBar) component;
							for (Enumeration e = workbenchToolBar
									.getButtonGroup().getElements(); e
									.hasMoreElements();) {
								AbstractButton button = (AbstractButton) e
										.nextElement();
								if (button.getToolTipText().equals(
										StringUtil.toFriendlyName(
												AdjustEndpointTool.class
														.getName(), "Tool"))) {
									button.doClick();
								}
							}
							return null;
						}
					});
				}
			});
		}
	}

	private FUTURE_EventFirer eventFirer = new FUTURE_EventFirer();

	public Class currentToolClass() {
		return currentToolClass;
	}

	void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		warpSelectedVerticesRadioButton.setText("Warp selected vertices");
		warpSelectedVerticesRadioButton
				.setToolTipText("Warps up to the vertex you click on");
		warpRadioButton.setText("Warp entire segment");
		shiftRadioButton.setText("Shift entire segment");
		extendOrClipRadioButton.setText("Extend / Clip segment");
		moveEndpointRadioButton.setText("Move endpoint");
		warpRadioButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				warpRadioButton_actionPerformed(e);
			}
		});
		warpSelectedVerticesRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						warpSelectedVertices_actionPerformed(e);
					}
				});
		shiftRadioButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shiftRadioButton_actionPerformed(e);
			}
		});
		extendOrClipRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						extendOrClipRadioButton_actionPerformed(e);
					}
				});
		moveEndpointRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						moveEndpointRadioButton_actionPerformed(e);
					}
				});
		fillerPanel2.setPreferredSize(new Dimension(12, 12));
		label.setToolTipText("");
		label.setText("When a segment end is adjusted:");
		warpLocallyPanel.setLayout(gridBagLayout2);
		warpLocallyRadioButton.setText("Warp length of ");
		warpLocallyRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						warpLocallyRadioButton_actionPerformed(e);
					}
				});
		unitsLabel.setText(" units");
		this.add(warpRadioButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(shiftRadioButton, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(extendOrClipRadioButton, new GridBagConstraints(1, 8, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(moveEndpointRadioButton, new GridBagConstraints(1, 6, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(fillerPanel1, new GridBagConstraints(2, 10, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(fillerPanel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		this.add(label, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(warpLocallyPanel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		warpLocallyPanel.add(warpLocallyRadioButton, new GridBagConstraints(1,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		warpLocallyPanel.add(warpZoneExtentTextField, new GridBagConstraints(2,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		warpLocallyPanel.add(unitsLabel, new GridBagConstraints(3, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(warpSelectedVerticesRadioButton, new GridBagConstraints(1, 3,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	void warpSelectedVertices_actionPerformed(ActionEvent e) {
		currentToolClass = WarpSelectedVerticesTool.class;
		eventFirer.fire(this);
	}

	void shiftRadioButton_actionPerformed(ActionEvent e) {
		currentToolClass = ShiftAdjustmentTool.class;
		eventFirer.fire(this);
	}

	void extendOrClipRadioButton_actionPerformed(ActionEvent e) {
		currentToolClass = MyExtendOrClipTool.class;
		eventFirer.fire(this);
	}

	void moveEndpointRadioButton_actionPerformed(ActionEvent e) {
		currentToolClass = MoveEndpointAdjustmentTool.class;
		eventFirer.fire(this);
	}

	void warpLocallyRadioButton_actionPerformed(ActionEvent e) {
		currentToolClass = WarpLocallyAdjustmentTool.class;
		eventFirer.fire(this);
	}

	void warpRadioButton_actionPerformed(ActionEvent e) {
		currentToolClass = WarpAdjustmentTool.class;
		eventFirer.fire(this);
	}

	private WorkbenchContext context;

	private JPanel fillerPanel1 = new JPanel();

	private JPanel fillerPanel2 = new JPanel();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private JLabel label = new JLabel();

	private JRadioButton shiftRadioButton = new JRadioButton();

	private JRadioButton warpSelectedVerticesRadioButton = new JRadioButton();

	private JRadioButton extendOrClipRadioButton = new JRadioButton();

	private JRadioButton moveEndpointRadioButton = new JRadioButton();

	private JLabel unitsLabel = new JLabel();

	private JPanel warpLocallyPanel = new JPanel();

	private JRadioButton warpLocallyRadioButton = new JRadioButton();

	private JRadioButton warpRadioButton = new JRadioButton();

	private ValidatingTextField warpZoneExtentTextField = new ValidatingTextField(
			"",
			4,
			SwingConstants.RIGHT,
			new ValidatingTextField.CompositeValidator(
					new ValidatingTextField.Validator[] {
							ValidatingTextField.DOUBLE_VALIDATOR,
							new ValidatingTextField.GreaterThanOrEqualValidator(
									0) }),
			new ValidatingTextField.CompositeCleaner(
					new ValidatingTextField.Cleaner[] {
							new ValidatingTextField.BlankCleaner(
									AdjustPanel.DEFAULT_MODEL_BASED_WARP_ZONE_EXTENT_TEXT),
							new ValidatingTextField.Cleaner() {
								public String clean(String text) {
									return Double.parseDouble(text.trim()) != 0 ? text
											: AdjustPanel.DEFAULT_MODEL_BASED_WARP_ZONE_EXTENT_TEXT;
								}
							} }));

	private Class currentToolClass = WarpSelectedVerticesTool.class;

	public static final String MODEL_BASED_WARP_ZONE_EXTENT_TEXT_KEY = ModelBasedWarpZone.class
			.getName()
			+ " - WARP ZONE EXTENT";

	//Store as string; otherwise double-to-string conversion will make it
	//50.0, which takes up precious room in the edit box.
	//[Jon Aquino 2004-02-13]
	public static final String DEFAULT_MODEL_BASED_WARP_ZONE_EXTENT_TEXT = "50";

	protected FUTURE_EventFirer getEventFirer() {
		return eventFirer;
	}
}