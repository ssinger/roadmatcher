package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.vividsolutions.jcs.conflate.linearpathmatch.LinearPath;
import com.vividsolutions.jcs.conflate.linearpathmatch.match.PathMatch;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegmentSiblingUpdater;
import com.vividsolutions.jcs.conflate.roads.pathmatch.RoadPathZipper;
import com.vividsolutions.jcs.conflate.roads.pathmatch.RoadSplitPath;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
public class MatchPathsOperation {
    public static class MyTransaction extends Transaction {
        public MyTransaction(ToolboxModel toolboxModel,
                ErrorHandler errorHandler) {
            super(toolboxModel, errorHandler);
        }
        public Set getAddedAndModifiedRoadSegments() {
            return new HashSet(FUTURE_CollectionUtil.concatenate(CollectionUtil
                    .concatenate(getNetworkToAddedRoadSegmentsMap().values()),
                    CollectionUtil
                            .concatenate(getNetworkToModifiedRoadSegmentsMap()
                                    .values())));
        }
    }
    private void addToTransaction(RoadSplitPath path, Transaction transaction) {
        for (Iterator i = path.getAdded().iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            transaction.add(roadSegment);
        }
        for (Iterator i = path.getMatched().iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            transaction.markAsModified(roadSegment);
        }
        for (Iterator i = path.getDeleted().iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            transaction.remove(roadSegment);
        }
    }
    public boolean allRoadSegmentsUnknown(PathMatch pathMatch) {
        for (Iterator i = roadSegments(pathMatch).iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            if (roadSegment.getState() != SourceState.UNKNOWN) {
                return false;
            }
        }
        return true;
    }
    private RoadPathZipper createRoadPathZipper(PathMatch pathMatch,
            ToolboxModel toolboxModel) {
        final RoadPathZipper roadPathZipper = new RoadPathZipper(pathMatch);
        roadPathZipper.setSegmentLengthTolerance(toolboxModel.getSession()
                .getMatchOptions().getEdgeMatchOptions()
                .getLineSegmentLengthTolerance());
        roadPathZipper
                .setDistanceTolerance(toolboxModel.getSession()
                        .getMatchOptions().getEdgeMatchOptions()
                        .getDistanceTolerance());
        return roadPathZipper;
    }
    public static abstract class MyUndoableCommand extends UndoableCommand {
        public MyUndoableCommand(String name) {
            super(name);
        }
        private boolean executionSuccessful = false;
        public boolean isExecutionSuccessful() {
            return executionSuccessful;
        }
        protected void setExecutionSuccessful(boolean executionSuccessful) {
            this.executionSuccessful = executionSuccessful;
        }
        protected void setTransaction(MyTransaction transaction) {
            this.transaction = transaction;
        }
        public MyTransaction getTransaction() {
            return transaction;
        }
        private MyTransaction transaction;
    }
    public MyUndoableCommand createUndoableCommand(final PathMatch pathMatch,
            final LayerViewPanelContext layerViewPanelContext,
            final ToolboxModel toolboxModel) {
        Assert.isTrue(allRoadSegmentsUnknown(pathMatch));
        return new MyUndoableCommand("Match Paths") {
            public void execute() {
                if (!valid) {
                    return;
                }
                try {
                    roadPathZipper.zipper(true);
                } catch (Exception e) {
                    valid = false;
                    layerViewPanelContext
                            .warnUser(ErrorMessages.pathMatchTool_pathZipper_invalid
                                    + " ("
                                    + StringUtil.friendlyName(e.getClass())
                                    + ")");
                    toolboxModel.getContext().getWorkbench().getFrame().log(
                            StringUtil.stackTrace(e));
                    toolboxModel.getLayerManager().getUndoableEditReceiver()
                            .reportIrreversibleChange();
                    return;
                }
                valid = roadPathZipper.isValid();
                if (!valid) {
                    layerViewPanelContext
                            .warnUser(ErrorMessages.pathMatchTool_pathZipper_invalid);
                    toolboxModel.getLayerManager().getUndoableEditReceiver()
                            .reportIrreversibleChange();
                    return;
                }
                setTransaction(new MyTransaction(toolboxModel,
                        layerViewPanelContext));
                addToTransaction(path(0), getTransaction());
                addToTransaction(path(1), getTransaction());
                boolean modifyingNetworksOriginally = getTransaction()
                        .isModifyingNetworks();
                getTransaction().setModifyingNetworks(false);
                try {
                    getTransaction().execute();
                } finally {
                    getTransaction().setModifyingNetworks(
                            modifyingNetworksOriginally);
                }
                setExecutionSuccessful(true);
            }
            private RoadSplitPath path(int i) {
                return roadPathZipper.getSplitPaths()[i];
            }
            public void unexecute() {
                if (!valid) {
                    return;
                }
                for (Iterator i = roadSegments(pathMatch).iterator(); i
                        .hasNext();) {
                    SourceRoadSegment roadSegment = (SourceRoadSegment) i
                            .next();
                    roadSegment.setState(SourceState.UNKNOWN, null);
                }
                getTransaction().unexecute();
                for (Iterator i = getTransaction()
                        .getAddedAndModifiedRoadSegments().iterator(); i
                        .hasNext();) {
                    SourceRoadSegment roadSegment = (SourceRoadSegment) i
                            .next();
                    if (roadSegment instanceof SplitRoadSegment) {
                        SplitRoadSegmentSiblingUpdater
                                .update((SplitRoadSegment) roadSegment);
                    }
                }
            }
            private RoadPathZipper roadPathZipper = createRoadPathZipper(
                    pathMatch, toolboxModel);
            private boolean valid = true;
        };
    }
    private Layer layer(int i, final PathMatch pathMatch,
            final ToolboxModel toolboxModel) {
        return toolboxModel
                .getSourceLayer(((SourceRoadSegment) ((DirectedEdge) pathMatch
                        .getPath(i).getEdge(0).getContext()).getEdge())
                        .getNetworkID());
    }
    private Collection features(Collection roadSegments) {
        return CollectionUtil.collect(roadSegments, new Block() {
            public Object yield(Object roadSegment) {
                return ((SourceRoadSegment) roadSegment).getFeature();
            }
        });
    }
    private Set roadSegments(LinearPath linearPath) {
        HashSet roadSegments = new HashSet();
        for (int i = 0; i < linearPath.size(); i++) {
            roadSegments.add(((SourceRoadSegment) ((DirectedEdge) linearPath
                    .getEdge(i).getContext()).getEdge()));
        }
        return roadSegments;
    }
    private Set roadSegments(PathMatch pathMatch) {
        return new HashSet(FUTURE_CollectionUtil.concatenate(
                roadSegments(pathMatch.getPath(0)), roadSegments(pathMatch
                        .getPath(1))));
    }
}