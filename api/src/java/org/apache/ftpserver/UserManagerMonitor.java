package org.apache.ftpserver;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface UserManagerMonitor {
    void generalError(String message, Exception ex);
    void info(String message);
}
