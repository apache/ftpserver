package org.apache.ftpserver;

import org.apache.ftpserver.interfaces.FtpUserManagerMonitor;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonFtpUserManagerMonitor implements FtpUserManagerMonitor {

    Logger logger;

    public AvalonFtpUserManagerMonitor(Logger logger) {
        this.logger = logger;
    }

    public void generalError(String message, Exception ex) {
        logger.error(message, ex);
    }

    public void info(String message) {
        logger.info(message);
    }
}
