package org.apache.ftpserver;

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface WriterMonitor {
    void responseException(String message, IOException ex);
}
