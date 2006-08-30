package org.apache.ftpserver.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

import org.apache.ftpserver.util.IoUtils;

public class TestUtil {

    public static void assertFileEqual(byte[] expected, File file ) throws Exception {
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;
        
        try{
            baos = new ByteArrayOutputStream();
            fis = new FileInputStream(file);
            
            IoUtils.copy(fis, baos, 1024);
            
            byte[] actual = baos.toByteArray();
            
            assertArraysEqual(expected, actual);
        } finally {
            IoUtils.close(fis);
            IoUtils.close(baos);
        }
    }
    
    public static void assertInArrays(Object expected, Object[] actual) {
        boolean found = false;
        for (int i = 0; i < actual.length; i++) {
            Object object = actual[i];
            if(object.equals(expected)) {
                found = true;
                break;
            }
        }
        
        if(!found) {
            TestCase.fail("Expected value not in array");
        }
    }
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        if(actual.length != expected.length) {
            TestCase.fail("Arrays are of different length");
        }
        
        for (int i = 0; i < actual.length; i++) {
            if(actual[i] != expected[i]) {
                TestCase.fail("Arrays differ at position " + i);
            }
        }
    }
}
