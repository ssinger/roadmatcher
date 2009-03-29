package com.vividsolutions.jcs.plugin.conflate.roads;


public class ZeroLengthException extends Exception {
    public ZeroLengthException() {
        super(ErrorMessages.adjustEndpointOperation_zeroLength);
    }
}