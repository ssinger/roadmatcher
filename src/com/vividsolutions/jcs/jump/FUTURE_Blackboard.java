package com.vividsolutions.jcs.jump;
import java.io.*;
import java.util.Iterator;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.java2xml.XML2Java;
public class FUTURE_Blackboard extends Blackboard {
    public void load(File file) throws Exception {
        FileReader fileReader = new FileReader(file);
        try {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                setProperties(((Blackboard) xml2Java().read(bufferedReader,
                        Blackboard.class)).getProperties());
            } finally {
                bufferedReader.close();
            }
        } finally {
            fileReader.close();
        }
    }
    protected XML2Java xml2Java() {
        return new FUTURE_XML2Java();
    }
    public void save(File file) throws Exception {
        FileWriter fileWriter = new FileWriter(file, false);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try {
                new FUTURE_Java2XML().write(this, "blackboard", bufferedWriter);
            } finally {
                bufferedWriter.close();
            }
        } finally {
            fileWriter.close();
        }
    }
    public static FUTURE_Blackboard java2XMLableClone(Blackboard blackboard) {
        FUTURE_Java2XML java2XML = new FUTURE_Java2XML();
        //Prevent Java2XML errors by removing un-Java2XML-able values
        //[Jon Aquino 2004-04-21]
        FUTURE_Blackboard java2XMLableClone = new FUTURE_Blackboard();
        for (Iterator i = blackboard.getProperties().keySet().iterator(); i
                .hasNext();) {
            String key = (String) i.next();
            try {
                //Unfortunately, objects with custom converters cannot be
                //passed directly into Java2XML#write, so check for them 
                //explicitly. [Jon Aquino 2004-04-23]
                if (!java2XML._hasCustomConverter(blackboard.get(key).getClass())) {
                    java2XML.write(blackboard.get(key), "test");
                }                
                java2XMLableClone.put(key, blackboard.get(key));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        return java2XMLableClone;
    }
}