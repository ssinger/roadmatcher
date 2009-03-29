package com.vividsolutions.jcs.jump;
import java.io.BufferedOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.io.OutputStream;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
public class FUTURE_MonitoredBufferedOutputStream extends BufferedOutputStream {
    private Timer timer = new Timer();
    /**
     * @param estimatedFileSize
     *                 in bytes; -1 if unknown
     */
    public FUTURE_MonitoredBufferedOutputStream(OutputStream outputStream,
            final long estimatedFileSize, final TaskMonitor monitor) {
        this(outputStream, new Block() {
            public Object yield(Object bytesWrittenObject) {
                int bytesWritten = ((Integer)bytesWrittenObject).intValue();
                String message = bytesWritten + " bytes written";
                if (estimatedFileSize > 0 && estimatedFileSize > bytesWritten) {
                    //size is the size of the file being overwritten. That
                    //file may very well be smaller than the data being
                    //written. Hence the check. [Jon Aquino 1/13/2004]
                    message += " ("
                            + (int) (bytesWritten * 100d / estimatedFileSize)
                            + "%)";
                }
                monitor.report(message);
                return null;
            }
        });
    }
    public FUTURE_MonitoredBufferedOutputStream(OutputStream outputStream,
            final Block block) {
        super(outputStream);
        timer.schedule(new TimerTask() {
            public void run() {
                block.yield(new Integer(bytesWritten));
            }
        }, 0, 1000);
    }
    public void close() throws IOException {
        timer.cancel();
        super.close();
    }
    private int bytesWritten = 0;
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        bytesWritten += len;
    }
}