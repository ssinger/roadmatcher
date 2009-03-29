package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * @deprecated Use a modifiable QuadTree instead
 */
public class LinearScanIndex implements SpatialIndex, Serializable {

    private ArrayList itemEnvelopes = new ArrayList();
    private ArrayList items = new ArrayList();
    private Envelope envelope = null;
    public void insert(Envelope itemEnvelope, Object item) {
        getEnvelope().expandToInclude(itemEnvelope);
        itemEnvelopes.add(itemEnvelope);
        items.add(item);
    }

    public boolean remove(Envelope itemEnvelope, Object item) {
        int i = items.indexOf(item);
        items.remove(i);
        Object obj = itemEnvelopes.remove(i);
        envelope = null;
        return obj != null;
    }

    public Envelope getEnvelope() {
        if (envelope == null) {
            envelope = new Envelope();
            for (Iterator i = itemEnvelopes.iterator(); i.hasNext(); ) {
                Envelope itemEnvelope = (Envelope) i.next();
                envelope.expandToInclude(itemEnvelope);
            }
        }
        return envelope;
    }

    public List query(Envelope searchEnvelope) {
        if (!getEnvelope().intersects(searchEnvelope)) { return new ArrayList(); }
        ArrayList results = new ArrayList();
        for (int i = 0; i < itemEnvelopes.size(); i++) {
            Envelope itemEnvelope = (Envelope) itemEnvelopes.get(i);
            if (itemEnvelope.intersects(searchEnvelope)) {
                results.add(items.get(i));
            }
        }
        return results;
    }

	public void query(Envelope arg0, ItemVisitor arg1) {
		// TODO Auto-generated method stub
		
	}

}
