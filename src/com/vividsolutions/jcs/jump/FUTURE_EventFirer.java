package com.vividsolutions.jcs.jump;

import java.util.ArrayList;
import java.util.Iterator;

//Can replace InputChangedFirer
public class FUTURE_EventFirer {
    public static interface Listener {
        public void update(Object o);
    }
    private ArrayList listeners = new ArrayList();

    public void add(Listener listener) {
        listeners.add(listener);
    }

    public void remove(Listener listener) {
        listeners.remove(listener);
    }

    public void fire(Object o) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            Listener listener = (Listener) i.next();
            listener.update(o);
        }
    }
}
