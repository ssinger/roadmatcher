package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.NetworkStatistics;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class StatisticsPlugIn extends AbstractPlugIn {
	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		ConflationSession session = ToolboxModel.instance(context).getSession();
		NetworkStatistics a = session.getStatistics().get(0);
		NetworkStatistics b = session.getStatistics().get(1);
		context.getOutputFrame().createNewDocument();
		context.getOutputFrame().addHeader(1, "RoadMatcher Session Statistics");
		context.getOutputFrame().append("<table border=0>");
		context
				.getOutputFrame()
				.append(
						"<tr><td colspan=2 align=center>Count</td><td colspan=2 align=center>Length</td><td></td></tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=center>" + nameHTML(0, context) + "</td>");
		context.getOutputFrame().append(
				"<td align=center>" + nameHTML(1, context) + "</td>");
		context.getOutputFrame().append(
				"<td align=center>" + nameHTML(0, context) + "</td>");
		context.getOutputFrame().append(
				"<td align=center>" + nameHTML(1, context) + "</td>");
		context.getOutputFrame().append("<td></td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right><b>" + a.getTotalCount() + "</b></td>");
		context.getOutputFrame().append(
				"<td align=right><b>" + b.getTotalCount() + "</b></td>");
		context.getOutputFrame().append(
				"<td align=right><b>" + format(a.getTotalLength())
						+ "</b></td>");
		context.getOutputFrame().append(
				"<td align=right><b>" + format(b.getTotalLength())
						+ "</b></td>");
		context.getOutputFrame().append("<td><b>Total</b></td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getUnknownCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getUnknownCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getUnknownLength()) + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getUnknownLength()) + "</td>");
		context.getOutputFrame().append("<td>Unknown</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getStandaloneCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getStandaloneCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getStandaloneLength()) + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getStandaloneLength()) + "</td>");
		context.getOutputFrame().append("<td>Standalone</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getMatchedReferenceCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getMatchedReferenceCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getMatchedReferenceLength())
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getMatchedReferenceLength())
						+ "</td>");
		context.getOutputFrame().append("<td>Matched (Ref)</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getMatchedNonReferenceCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getMatchedNonReferenceCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getMatchedNonReferenceLength())
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getMatchedNonReferenceLength())
						+ "</td>");
		context.getOutputFrame().append("<td>Matched (Non-Ref)</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getRetiredCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getRetiredCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getRetiredLength()) + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getRetiredLength()) + "</td>");
		context.getOutputFrame().append("<td>Retired</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr><td colspan=5></td></tr>");
		context.getOutputFrame().append(
				"<tr><td colspan=5><b>Result States:</b></td></tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getResultStatistics().getPendingCount()
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getResultStatistics().getPendingCount()
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>"
						+ format(a.getResultStatistics().getPendingLength())
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>"
						+ format(b.getResultStatistics().getPendingLength())
						+ "</td>");
		context.getOutputFrame().append("<td>Pending</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>"
						+ a.getResultStatistics().getInconsistentCount()
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>"
						+ b.getResultStatistics().getInconsistentCount()
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>"
						+ format(a.getResultStatistics()
								.getInconsistentLength()) + "</td>");
		context.getOutputFrame().append(
				"<td align=right>"
						+ format(b.getResultStatistics()
								.getInconsistentLength()) + "</td>");
		context.getOutputFrame().append("<td>Inconsistent</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right><b>"
						+ a.getResultStatistics().getIntegratedCount()
						+ "</b></td>");
		context.getOutputFrame().append(
				"<td align=right><b>"
						+ b.getResultStatistics().getIntegratedCount()
						+ "</b></td>");
		context.getOutputFrame().append(
				"<td align=right><b>"
						+ format(a.getResultStatistics().getIntegratedLength())
						+ "</b></td>");
		context.getOutputFrame().append(
				"<td align=right><b>"
						+ format(b.getResultStatistics().getIntegratedLength())
						+ "</b></td>");
		context.getOutputFrame().append("<td><b>Integrated</b></td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr><td colspan=5></td></tr>");
		context.getOutputFrame().append(
				"<tr><td colspan=5><b>Matches:</b></td></tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getAutoMatchedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getAutoMatchedCount() + "</td>");
		context.getOutputFrame()
				.append(
						"<td align=right>" + format(a.getAutoMatchedLength())
								+ "</td>");
		context.getOutputFrame()
				.append(
						"<td align=right>" + format(b.getAutoMatchedLength())
								+ "</td>");
		context.getOutputFrame().append("<td>AutoMatched</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getManuallyMatchedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getManuallyMatchedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getManuallyMatchedLength())
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getManuallyMatchedLength())
						+ "</td>");
		context.getOutputFrame().append("<td>Manually Matched</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr><td colspan=5></td></tr>");
		context.getOutputFrame().append(
				"<tr><td colspan=5><b>Adjustments:</b></td></tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getAutoAdjustedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getAutoAdjustedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getAutoAdjustedLength())
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getAutoAdjustedLength())
						+ "</td>");
		context.getOutputFrame().append("<td>AutoAdjusted</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getManuallyAdjustedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getManuallyAdjustedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getManuallyAdjustedLength())
						+ "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getManuallyAdjustedLength())
						+ "</td>");
		context.getOutputFrame().append("<td>Manually Adjusted</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td align=right>" + a.getUnadjustedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + b.getUnadjustedCount() + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(a.getUnadjustedLength()) + "</td>");
		context.getOutputFrame().append(
				"<td align=right>" + format(b.getUnadjustedLength()) + "</td>");
		context.getOutputFrame().append("<td>Unadjusted</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("<tr><td colspan=5></td></tr>");
		context.getOutputFrame().append("<tr>");
		context.getOutputFrame().append(
				"<td colspan=5>Nearness: "
						+ truncateAfterThreeDecimalPlaces(session
								.getStatistics().getNearness()) + "</td>");
		context.getOutputFrame().append("</tr>");
		context.getOutputFrame().append("</table>");
		context.getOutputFrame().surface();
		return true;
	}

	private String format(double x) {
		return "<font color=#A9A9A9>" + truncateAfterThreeDecimalPlaces(x)
				+ "</font>";
	}

	private String nameHTML(int i, PlugInContext context) {
		return "<font color="
				+ FUTURE_GUIUtil.toHTML(ToolboxModel.instance(context)
						.getSourceLayer(i).getBasicStyle().getFillColor())
				+ ">"
				+ ToolboxModel.instance(context).getSession().getSourceNetwork(
						i).getName() + "</font>";
	}

	public double truncateAfterThreeDecimalPlaces(double x) {
		return Math.rint(x * 1000) / 1000;
	}

	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.TOOLS_MENU_NAME },
						getName() + "...",
						false,
						null,
						new MultiEnableCheck()
								.add(
										context
												.getCheckFactory()
												.createWindowWithLayerViewPanelMustBeActiveCheck())
								.add(
										SpecifyRoadFeaturesTool
												.createConflationSessionMustBeStartedCheck(context
														.getWorkbenchContext())));
	}
}