package org.apache.ftpserver.core;

import org.apache.avalon.framework.logger.Logger;
import org.apache.ftpserver.RemoteHandlerMonitor;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonRemoteHandlerMonitor implements RemoteHandlerMonitor {

    Logger logger;

    public AvalonRemoteHandlerMonitor(Logger logger) {
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
