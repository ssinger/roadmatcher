package com.vividsolutions.jcs.jump;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
public class FUTURE_FileUtil {
    public static File addExtensionIfNone(File file, String extension) {
        if (GUIUtil.getExtension(file).length() > 0) {
            return file;
        }
        String path = file.getAbsolutePath();
        if (!path.endsWith(".")) {
            path += ".";
        }
        path += extension;
        return new File(path);
    }
    public static File createTemporaryDirectory() throws IOException {
        File temporaryDirectory = File.createTempFile("temp", "");
        temporaryDirectory.delete();
        temporaryDirectory.mkdir();
        return temporaryDirectory;
    }
    public static void unzip(final File zipFile, File outputDirectory, final TaskMonitor monitor) throws IOException {
        Assert.isTrue(outputDirectory.isDirectory());
        Assert.isTrue(outputDirectory.exists());
        FileInputStream fileInputStream = new FileInputStream(zipFile);
        try {
            BufferedInputStream bufferedInputStream = new FUTURE_MonitoredBufferedInputStream(
                    fileInputStream, zipFile.length(), monitor);
            try {
                ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);
                try {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        unzip(new File(outputDirectory, zipEntry.getName()), zipInputStream);
                    }
                }
                finally {
                    zipInputStream.close();
                }
            }
            finally {
                bufferedInputStream.close();
            }
        }
        finally {
            fileInputStream.close();
        }
    }    
    /**
     * @param estimatedFileSize
     *                 in bytes; -1 if unknown
     */    
    public static void zip(File[] files, final File zipFile, long estimatedFileSize, final TaskMonitor monitor)
            throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
        try {
            BufferedOutputStream bufferedOutputStream = new FUTURE_MonitoredBufferedOutputStream(
                    fileOutputStream, estimatedFileSize, monitor);
            try {
                ZipOutputStream zipOutputStream = new ZipOutputStream(
                        bufferedOutputStream);
                try {
                    for (int i = 0; i < files.length; i++) {
                        zip(files[i], zipOutputStream);
                    }
                } finally {
                    zipOutputStream.close();
                }
            } finally {
                bufferedOutputStream.close();
            }
        } finally {
            fileOutputStream.close();
        }
    }

    private static void unzip(File file, ZipInputStream zipInputStream)
            throws IOException {
        byte bytes[] = new byte[512];
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            try {
                int length = 0;
                while ((length=zipInputStream.read(bytes)) != -1) {
                    bufferedOutputStream.write(bytes, 0, length);
                }
            }
            finally {
                bufferedOutputStream.close();
            }
        }
        finally {
            fileOutputStream.close();
        }
    }    
    private static void zip(File file, ZipOutputStream zipOutputStream)
            throws IOException {
        byte bytes[] = new byte[512];
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    fileInputStream);
            try {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);
                int length = 0;
                while ((length = bufferedInputStream.read(bytes)) != -1) {
                    zipOutputStream.write(bytes, 0, length);
                }
                zipOutputStream.closeEntry();
            } finally {
                bufferedInputStream.close();
            }
        } finally {
            fileInputStream.close();
        }
    }
    public static void createTemporaryDirectory(Block block) throws IOException {
        File temporaryDirectory = createTemporaryDirectory();
        try {
            block.yield(temporaryDirectory);
        } finally {
            File[] files = temporaryDirectory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].delete()) {
                    files[i].deleteOnExit();
                }
            }
            if (!temporaryDirectory.delete()) {
                temporaryDirectory.deleteOnExit();
            }
        }
    }    
}