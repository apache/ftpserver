package org.apache.ftpserver;

import org.apache.ftpserver.interfaces.FtpRemoteHandlerMonitor;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonFtpRemoteHandlerMonitor implements FtpRemoteHandlerMonitor {

    Logger logger;

    public AvalonFtpRemoteHandlerMonitor(Logger logger) {
        this.logger = logger;
    }

    public void remoteAdminLoginRequestError(Exception ex) {
        logger.error("RemoteHandler.login()", ex);
    }

    public void remoteLoginAdminRequest(String clientHost) {
        logger.info("Remote admin login request from " + clientHost);
    }

    public void remoteAdminLogout() {
        logger.info("Remote Admin Logout");


    }

    public void remoteAdminClose() {
        logger.info("Remote Admin Close");
    }

    public void remoteAdminTimeout() {
        logger.info("Remote admin timeout");
    }

}
