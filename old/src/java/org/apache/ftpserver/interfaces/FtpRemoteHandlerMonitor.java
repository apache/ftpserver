package org.apache.ftpserver.interfaces;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FtpRemoteHandlerMonitor {

    void remoteAdminLoginRequestError(Exception ex);
    void remoteLoginAdminRequest(String clientHost);
    void remoteAdminLogout();
    void remoteAdminClose();
    void remoteAdminTimeout();

}
