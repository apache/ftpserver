package org.apache.ftpserver;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface RemoteHandlerMonitor {

    void remoteAdminLoginRequestError(Exception ex);

    void remoteLoginAdminRequest(String clientHost);

    void remoteAdminLogout();

    void remoteAdminClose();

    void remoteAdminTimeout();

}
