package org.apache.ftpserver;

import org.apache.ftpserver.interfaces.FtpFileMonitor;
import org.apache.avalon.framework.logger.Logger;

import java.io.File;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonFileMonitor implements FtpFileMonitor {

    Logger logger;

    public AvalonFileMonitor(Logger logger) {
        this.logger = logger;
    }

    public void fileUploaded(FtpUser user, File fl) {
        logger.info("File upload : " + user.getName() + " - " + fl.getAbsolutePath());
    }

    public void fileDownloaded(FtpUser user, File fl) {
        logger.info("File download : " + user.getName() + " - " + fl.getAbsolutePath());
    }

    public void fileDeleted(FtpUser user, File fl) {
        logger.info("File delete : " + user.getName() + " - " + fl.getAbsolutePath());
    }

}
