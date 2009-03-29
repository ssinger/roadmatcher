package com.vividsolutions.jcs.jump;

import java.io.Serializable;

import com.vividsolutions.jump.util.Block;

public abstract class FUTURE_Block extends Block implements Serializable {
    public static final Block TRUE = new Block() {
        public Object yield() {
            return Boolean.TRUE;
		}
		public Object yield(Object arg) {
			return Boolean.TRUE;
		}
        public Object yield(Object arg1, Object arg2) {
            return Boolean.TRUE;
		}
	};

	public static Block inverse(final Block block) {
        return new Block() {
            public Object yield() {
                return inverse(block.yield());
            }
            public Object yield(Object arg) {
                return inverse(block.yield(arg));
            }
            public Object yield(Object arg1, Object arg2) {
                return inverse(block.yield(arg1, arg2));
            }
            private Boolean inverse(Object b) {
                return Boolean.valueOf(!((Boolean)b).booleanValue()); 
            }            
        };
    }
}
