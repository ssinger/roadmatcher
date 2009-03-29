package com.vividsolutions.jcs.jump;
import java.util.Collection;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.Fmt;
import com.vividsolutions.jump.util.StringUtil;
public class FUTURE_StringUtil {
    public static String substitute(String string, Object[] substitutions) {
        for (int i = 0; i < substitutions.length; i++) {
            string = StringUtil.replaceAll(string, "$" + (i + 1),
                    substitutions[i].toString());
        }
        return string;
    }
    public static String format(double d) {
        //Fmt's handling of sigfigs seems a little buggy (try 3 for example),
        //but it behaves reasonably at 6. [Jon Aquino]
        return Fmt.fmt(d, 0, 6);
    }
    public static String reverse(String s) {
        return ((new StringBuffer(s)).reverse()).toString();
    }
    public static String replace(String original, String oldSubstring,
            String newSubstring, boolean all) {
        return original.length() == 0 || oldSubstring.length() == 0 ? original
                : StringUtil.replace(original, oldSubstring, newSubstring, all);
    }
    public static Collection toUpperCase(Collection strings) {
        return CollectionUtil.collect(strings, new Block() {
            public Object yield(Object string) {
                return ((String)string).toUpperCase();
            }
        });
    }
}