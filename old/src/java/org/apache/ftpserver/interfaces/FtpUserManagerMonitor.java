package org.apache.ftpserver.interfaces;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FtpUserManagerMonitor {
    void generalError(String message, Exception ex);
    void info(String message);
}
