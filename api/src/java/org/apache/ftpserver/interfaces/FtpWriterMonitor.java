package org.apache.ftpserver.interfaces;

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FtpWriterMonitor {
    void responseException(String message, IOException ex);
}
