package com.vividsolutions.jcs.conflate.roads.model;

import com.vividsolutions.jump.util.Block;

public interface Optimizable {
	public abstract void doOptimizedOp(Block op);
}