package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
public class ConstraintChecker {
	public ConstraintChecker(LayerViewPanelContext panelContext) {
		this.panelContext = panelContext;
	}
	private boolean checkAdjustmentConstraints(boolean network0,
			boolean network1, ConflationSession session) {
		return (!network0 || checkAdjustmentConstraints(0, session))
				&& (!network1 || checkAdjustmentConstraints(1, session));
	}
	private boolean checkAdjustmentConstraints(int i, ConflationSession session) {
		if (!session.getSourceNetwork(i).isEditable()) {
			report(FUTURE_StringUtil.substitute(
					ErrorMessages.constraintChecker_adjustmentConstraintError,
					new String[]{session.getSourceNetwork(i).getName()}));
			return false;
		}
		if (session.isWarningAboutAdjustments(i)) {
			report(FUTURE_StringUtil
					.substitute(
							ErrorMessages.constraintChecker_adjustmentConstraintWarning,
							new String[]{session.getSourceNetwork(i).getName()}));
		}
		return true;
	}
	public boolean proceedWithAdjusting(Feature feature, LineString newLine,
			WorkbenchContext context) {
		return !(feature instanceof SourceFeature)
				|| proceedWithAdjusting(((SourceFeature) feature)
						.getRoadSegment(), newLine, ToolboxModel.instance(
						context).getSession());
	}
	public boolean proceedWithAdjusting(SourceRoadSegment roadSegment,
			LineString newLine, ConflationSession session) {
		return checkAdjustmentConstraints(roadSegment.getNetworkID() == 0,
				roadSegment.getNetworkID() == 1, session)
				&& checkNodeConstraints(roadSegment, newLine);
	}
	private boolean checkNodeConstraints(SourceRoadSegment roadSegment,
			LineString newLine) {
		boolean satisfiesNodeConstraints = satisfiesNodeConstraints(
				roadSegment, newLine);
		if (!satisfiesNodeConstraints) {
			report(ErrorMessages.constraintChecker_nodeConstraintError);
		}
		return satisfiesNodeConstraints;
	}
	private void report(String message) {
		panelContext.warnUser(message);
	}
	private LayerViewPanelContext panelContext;
	private boolean satisfiesNodeConstraints(SourceRoadSegment roadSegment,
			LineString newLine) {
		//Note that roadSegment and newLine may have different numbers of
		//coordinates [Jon Aquino 2004-04-28]
		//Use #apparentLine rather than #line, because user may have earlier
		//bypassed the node constraint by using JUMP tools [Jon Aquino
		// 2004-04-28]
		return (!roadSegment.isStartNodeConstrained() || FUTURE_LineString
				.first(roadSegment.getApparentLine()).equals(
						FUTURE_LineString.first(newLine)))
				&& (!roadSegment.isEndNodeConstrained() || FUTURE_LineString
						.last(roadSegment.getApparentLine()).equals(
								FUTURE_LineString.last(newLine)));
	}
	public boolean proceedWithAdjusting(Collection editTransactions,
			WorkbenchContext context) {
		for (Iterator i = editTransactions.iterator(); i.hasNext();) {
			EditTransaction editTransaction = (EditTransaction) i.next();
			if (!proceedWithAdjusting(editTransaction, context)) {
				return false;
			}
		}
		return true;
	}
	private boolean proceedWithAdjusting(EditTransaction editTransaction,
			WorkbenchContext context) {
		for (int i = 0; i < editTransaction.size(); i++) {
			if (!proceedWithAdjusting(editTransaction.getFeature(i),
					(LineString) editTransaction.getGeometry(i), context)) {
				return false;
			}
		}
		return true;
	}
	public boolean proceedWith(Adjustment adjustment, ConflationSession session) {
		for (int i = 0; i < adjustment.getOldApparentLines().size(); i++) {
			if (adjustment.getOldApparentLines().get(i) == adjustment
					.getNewApparentLines().get(i)) {
				continue;
			}
			if (!proceedWithAdjusting((SourceRoadSegment) adjustment
					.getRoadSegments().get(i), (LineString) adjustment
					.getNewApparentLines().get(i), session)) {
				return false;
			}
		}
		return true;
	}
}