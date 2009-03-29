package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.jump.FUTURE_StyleUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;
public class OneDragSimpleAdjustEndpointTool extends
		RoadSegmentEndpointGrabberTool {
	public OneDragSimpleAdjustEndpointTool(
			final SimpleAdjustmentMethod adjustmentMethod) {
		super(new SimpleAdjustEndpointTool(adjustmentMethod) {
			protected Renderer createSpecifiedRoadSegmentRenderer(
					LayerViewPanel layerViewPanel) {
				return new SimpleRenderer(
						SimpleAdjustEndpointTool.SPECIFIED_ROAD_SEGMENT_KEY,
						layerViewPanel) {
					protected void paint(Graphics2D g) throws Exception {
					}
				};
			}
			protected Shape getShape(Point2D source, Point2D destination)
					throws Exception {
				try {
					return FUTURE_StyleUtil
							._toShape(
									adjustmentMethod
											.adjust(
													getRoadSegment()
															.getApparentLine(),
													getRoadSegment(),
													getClosestEndpointWithinTolerance()
															.equals(
																	getRoadSegment()
																			.getApparentStartCoordinate()) ? SimpleAdjustmentMethod.Terminal.START
															: SimpleAdjustmentMethod.Terminal.END,
													modelDestination,
													getPanel()), getPanel()
											.getViewport());
				} catch (Exception e) {
                    e.printStackTrace(System.err);
                    getWorkbench().getFrame().log(StringUtil.stackTrace(e));
					return super.getShape(source, destination);
				}
			}
		}, new Block() {
			public Object yield(Object roadSegmentTool, Object roadSegment) {
				((SimpleAdjustEndpointTool) roadSegmentTool)
						.setRoadSegment((SourceRoadSegment) roadSegment);
				return null;
			}
		});
	}
	public Icon getIcon() {
		return icon;
	}
	public String getName() {
		return "Adjust Endpoint";
	}
	private Cursor cursor = FUTURE_GUIUtil
			.createCursorFromIcon("adjust-endpoint-tool-button.png");
	private Icon icon = SpecifyRoadFeaturesTool
			.createIcon("adjust-endpoint-tool-button.png");
}
