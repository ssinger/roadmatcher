package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import com.vividsolutions.jcs.jump.FUTURE_Blackboard;
import com.vividsolutions.jcs.jump.FUTURE_OptionsDialog;
import com.vividsolutions.jcs.jump.FUTURE_XML2Java;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jts.util.AssertionFailedException;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
public class ApplicationOptionsPlugIn extends AbstractOptionsPlugIn {
	protected OptionsDialog createDialog(WorkbenchContext context) {
		OptionsDialog dialog = FUTURE_OptionsDialog.construct(context
				.getWorkbench().getFrame(), getName(), true);
		dialog.setTitle(getName());
		dialog.addTab("Consistency", new ConsistencyRuleOptionsPanel(context));
		dialog.addTab("Confirmation", new ConfirmationOptionsPanel(context));
		dialog.addTab("AutoAdjust", new AutoAdjustOptionsPanel(context));
		return dialog;
	}
	private static final File FILE = new File("roads-options.xml");
	public void initialize(final PlugInContext context) throws Exception {
		if (FILE.exists()) {
			try {
				ApplicationOptionsPlugIn.options(context.getWorkbenchContext())
						.load(FILE);
			} catch (Exception x) {
				x.printStackTrace(System.err);
				context.getWorkbenchFrame().log(StringUtil.stackTrace(x));
			}
		}
		context.getWorkbenchFrame().addComponentListener(
				new ComponentAdapter() {
					public void componentHidden(ComponentEvent e) {
						try {
							FUTURE_Blackboard.java2XMLableClone(
									ApplicationOptionsPlugIn.options(context
											.getWorkbenchContext())).save(FILE);
						} catch (Exception x) {
							x.printStackTrace(System.err);
							context.getWorkbenchFrame().log(
									StringUtil.stackTrace(x));
						}
					}
				});
		RoadMatcherExtension.addMainMenuItemWithJava14Fix(context, this,
				new String[]{RoadMatcherToolboxPlugIn.MENU_NAME}, getName()
						+ "...", false, null, null);
	}
	public static FUTURE_Blackboard options(WorkbenchContext context) {
		String KEY = ApplicationOptionsPlugIn.class.getName() + " - BLACKBOARD";
		if (context.getBlackboard().get(KEY) == null) {
			context.getBlackboard().put(KEY, new FUTURE_Blackboard() {
				protected XML2Java xml2Java() {
					return new FUTURE_XML2Java() {
						{
							//Redefine the CustomConverter for Class so that
							//this extension's ClassLoader will be used.
							//[Jon Aquino 2004-02-20]
							addCustomConverter(Class.class,
									new CustomConverter() {
										public Object toJava(String value) {
											try {
												return Class.forName(value);
											} catch (ClassNotFoundException e) {
												//Don't throw an exception;
												//otherwise, other properties
												//will not be loaded. Besides,
												//it's not critical.
												//[Jon Aquino 2004-04-29]
												new AssertionFailedException(
														"Can get here during development while class names are changing. ("
																+ value + ")")
														.printStackTrace(System.err);
												return null;
											}
										}
										public String toXML(Object object) {
											return ((Class) object).getName();
										}
									});
						}
					};
				}
			});
		}
		return (FUTURE_Blackboard) context.getBlackboard().get(KEY);
	}
}