package com.vividsolutions.jcs.jump;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.ui.TreeUtil;

public class FUTURE_TreeUtil {

    public static Block createBlockToSelectNodeTemporarily(final Object node,
            final JTree tree, final Block block) {
        return new Block() {
            public Object yield() {
                TreePath[] originalSelectionPaths = tree.getSelectionPaths();
                try {
                    tree.setSelectionPath(TreeUtil.findTreePath(node, tree
                            .getModel()));
                    block.yield();
                } finally {
                    tree.setSelectionPaths(originalSelectionPaths);
                }
                return originalSelectionPaths;
            }
        };
    }

}
