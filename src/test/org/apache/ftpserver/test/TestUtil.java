package org.apache.ftpserver.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.TestCase;

import org.apache.ftpserver.util.IoUtils;

public class TestUtil {

    private static final int DEFAULT_PORT = 12321;

    /**
     * Attempts to find a free port or fallback to a default
     * @throws IOException 
     * 
     * @throws IOException
     */
    public static int findFreePort() throws IOException {
        int port = -1;
        ServerSocket tmpSocket = null;
        // first try the default port
        try {
            tmpSocket = new ServerSocket(DEFAULT_PORT);
            tmpSocket.bind(null);
            port = DEFAULT_PORT;
        } catch (IOException e) {
            // didn't work, try to find one dynamically
            try {
                int attempts = 0;

                while (port < 1024 && attempts < 1000) {
                    tmpSocket = new ServerSocket();
                    tmpSocket.bind(null);
                    port = tmpSocket.getLocalPort();
                }

            } catch (IOException e1) {
                throw new IOException("Failed to find a port to use for testing: "
                        + e1.getMessage());
            }
        } finally {
            if (tmpSocket != null) {
                try {
                    tmpSocket.close();
                } catch (IOException e) {
                    // ignore
                }
                tmpSocket = null;
            }
        }

        return port;
    }

    public static void assertFileEqual(byte[] expected, File file)
            throws Exception {
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;

        try {
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
            if (object.equals(expected)) {
                found = true;
                break;
            }
        }

        if (!found) {
            TestCase.fail("Expected value not in array");
        }
    }

    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        if (actual.length != expected.length) {
            TestCase.fail("Arrays are of different length");
        }

        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != expected[i]) {
                TestCase.fail("Arrays differ at position " + i);
            }
        }
    }
}
