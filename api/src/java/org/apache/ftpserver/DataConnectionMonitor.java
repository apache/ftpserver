package org.apache.ftpserver;

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface DataConnectionMonitor {
    void socketCloseException(String message, IOException ex);
    void serverSocketOpenException(String message, IOException ex);
    void socketException(String message, IOException ex);
}
