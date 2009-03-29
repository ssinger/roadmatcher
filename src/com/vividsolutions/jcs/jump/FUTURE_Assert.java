package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;

public class FUTURE_Assert {
    //Hmm ... changing the return value I have since found will break backwards
    //compatibility (get Method Not Found errors) :-( [Jon Aquino 12/8/2003]
    public static Object throwAssertionFailure() {
        Assert.shouldNeverReachHere();
        return null;
    }
    public static int shouldNeverReachHere2() {
        Assert.shouldNeverReachHere();
        return -1;
    }
	public static void shouldNeverReachHere(Exception e) {
		Assert.shouldNeverReachHere(StringUtil.stackTrace(e));
	}    
}
