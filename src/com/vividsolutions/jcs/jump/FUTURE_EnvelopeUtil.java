package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.geom.Envelope;

public class FUTURE_EnvelopeUtil {
    public static double maxExtent(Envelope envelope) {
        return Math.max(envelope.getHeight(), envelope.getWidth());
    }
}
