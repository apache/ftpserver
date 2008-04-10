package org.apache.ftpserver.test;


import java.io.File;

public class TestUtil {

    public static File getBaseDir() {
        // check Maven system prop first and use if set
        String basedir = System.getProperty("basedir");
        if(basedir != null) {
            return new File(basedir);
        } else {
            return new File(".");
        }
    }
}