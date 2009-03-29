package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jcs.conflate.linearpathmatch.LinearPath;
import com.vividsolutions.jcs.conflate.linearpathmatch.match.PathMatch;
import com.vividsolutions.jcs.conflate.roads.model.AbstractNodeConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.pathmatch.RoadPathTracer;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.jump.FUTURE_RenderingManager;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class PathUtilities {

	public static PathMatch pathMatch(List path0, List path1) {
		return new PathMatch(
				linearPath(correspondingNodesMatch(path0,
						path1) ? path0 : reverse(path0)), linearPath(path1));
	}

	private static boolean correspondingNodesMatch(
			List path0, List path1) {
		return AbstractNodeConsistencyRule
				.correspondingNodesMatchByAngle(startNode(path0)
						.getCoordinate(), endNode(path0).getCoordinate(),
						startNode(path1).getCoordinate(), endNode(path1)
								.getCoordinate());
	}

	private static LinearPath linearPath(List directedEdges) {
		return new LinearPath(CollectionUtil.collect(directedEdges,
				new Block() {
					public Object yield(Object directedEdge) {
						return RoadPathTracer
								.toLinearEdge((DirectedEdge) directedEdge);
					}
				}));
	}

	public static PathMatch pathMatch(WorkbenchContext context) {
		return pathMatch(PathUtilities.path(0, context.getLayerViewPanel()),
				PathUtilities.path(1, context.getLayerViewPanel()));
	}

	public static String contentID(final int i) {
		//Can't just use #getClass, because this method may be called
		//from an anonymous inner class, resulting in multiple
		//content ID's. [Jon Aquino 2004-03-16]
		return DefinePathsTool.class.getName() + " - PATH " + i;
	}

	public static void changePathUndoablyTo(final List[] newPaths, String name,
			final LayerViewPanel layerViewPanel) {
		final List[] oldPaths = new List[] {
				new ArrayList(path(0, layerViewPanel)),
				new ArrayList(path(1, layerViewPanel)) };
		AbstractPlugIn.execute(new UndoableCommand(name) {
			private void changePathsTo(List[] paths) {
				path(0, layerViewPanel).clear();
				path(1, layerViewPanel).clear();
				path(0, layerViewPanel).addAll(paths[0]);
				path(1, layerViewPanel).addAll(paths[1]);
				//If call ClearPathsPlugIn without defining any paths, there
				//won't be any renderers for the paths. Avoid the
				//AssertionFailedException thrown in
				//RenderingManager#createRenderer [Jon Aquino 2004-05-12]
				render(0);
				render(1);
			}

			private void render(int i) {
				if (!FUTURE_RenderingManager.contentIDs(
						layerViewPanel.getRenderingManager()).contains(
						contentID(i))) {
					return;
				}
				layerViewPanel.getRenderingManager().render(contentID(i));
			}

			public void execute() {
				changePathsTo(newPaths);
			}

			public void unexecute() {
				changePathsTo(oldPaths);
			}
		}, layerViewPanel);
	}

	public static List path(int i, LayerViewPanel panel) {
		final String PATHS_KEY = MatchPathsOperation.class.getName()
				+ " - PATHS";
		if (panel.getBlackboard().get(PATHS_KEY) == null) {
			panel.getBlackboard().put(PATHS_KEY,
					new List[] { new ArrayList(), new ArrayList() });
		}
		return ((List[]) panel.getBlackboard().get(PATHS_KEY))[i];
	}

	public static boolean checkPathRoadSegmentsInNetwork(LayerViewPanel panel,
			WorkbenchContext context) {
		return checkPathRoadSegmentsInNetwork(path(0, panel), context)
				&& checkPathRoadSegmentsInNetwork(path(1, panel), context);
	}

	private static boolean checkPathRoadSegmentsInNetwork(List path,
			WorkbenchContext context) {
		if (allPathRoadSegmentsInNetwork(path)) {
			return true;
		}
		context.getWorkbench().getFrame().warnUser(
				ErrorMessages.matchPathsPlugIn_pathRoadSegmentsNotInNetwork);
		return false;
	}

	private static boolean allPathRoadSegmentsInNetwork(List path) {
		for (Iterator i = path.iterator(); i.hasNext();) {
			DirectedEdge directedEdge = (DirectedEdge) i.next();
			if (!((SourceRoadSegment) directedEdge.getEdge()).isInNetwork()) {
				return false;
			}
		}
		return true;
	}

	public static List reverse(List path) {
		ArrayList reversePath = new ArrayList();
		for (int i = path.size() - 1; i >= 0; i--) {
			DirectedEdge directedEdge = (DirectedEdge) path.get(i);
			reversePath.add(directedEdge.getSym());
		}
		return reversePath;
	}

	public static RoadNode startNode(List path) {
		return (RoadNode) ((DirectedEdge) path.get(0)).getFromNode();
	}

	public static RoadNode endNode(List path) {
		return (RoadNode) ((DirectedEdge) path.get(path.size() - 1))
				.getToNode();
	}

}