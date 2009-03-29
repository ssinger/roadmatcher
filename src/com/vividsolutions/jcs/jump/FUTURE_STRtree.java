package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.index.strtree.AbstractSTRtree;
import com.vividsolutions.jts.index.strtree.STRtree;

public class FUTURE_STRtree extends STRtree {
    public boolean isBuilt() {
        return ((Boolean)FUTURE_LangUtil.getPrivateField("built", this, AbstractSTRtree.class)).booleanValue();
    }
}
