package com.vividsolutions.jcs.jump;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
public class FUTURE_MonitoredBufferedInputStream extends BufferedInputStream {
    private Timer timer = new Timer();
    /**
     * @param estimatedFileSize
     *                   in bytes; -1 if unknown
     */
    public FUTURE_MonitoredBufferedInputStream(InputStream inputStream,
            final long size, final TaskMonitor monitor) {
        this(inputStream, new Block() {
            public Object yield(Object bytesReadObject) {
                int bytesRead = ((Integer) bytesReadObject).intValue();
                String message = bytesRead + " bytes read";
                if (size > 0) {
                    message += " (" + (int) (bytesRead * 100d / size) + "%)";
                }
                monitor.report(message);
                return null;
            }
        });
    }
    public FUTURE_MonitoredBufferedInputStream(InputStream inputStream,
            final Block block) {
        super(inputStream);
        timer.schedule(new TimerTask() {
            public void run() {
                block.yield(new Integer(bytesRead));
            }
        }, 0, 1000);
    }
    public void close() throws IOException {
        timer.cancel();
        super.close();
    }
    private int bytesRead = 0;
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        bytesRead += len;
        return result;
    }
}