package org.apache.ftpserver;

import org.apache.ftpserver.ip.IpRestrictorInterface;
import org.apache.ftpserver.remote.RemoteHandler;
import org.apache.ftpserver.usermanager.UserManagerInterface;
import org.apache.ftpserver.util.AsyncMessageQueue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public abstract class AbstractFtpConfig {

    protected FtpStatus  mStatus                  = null;
    protected ConnectionService mConService       = null;
    protected IpRestrictorInterface mIpRestrictor = null;
    protected UserManagerInterface mUserManager   = null;

    protected InetAddress mServerAddress          = null;
    protected InetAddress mSelfAddress            = null;

    protected FtpStatistics mStatistics           = null;

    protected RemoteHandler mRemoteHandler        = null;

    protected int miServerPort;
    protected int miDataPort[][];
    protected int miRmiPort;
    protected int miMaxLogin;
    protected int miAnonLogin;
    protected int miPollInterval;
    protected int miDefaultIdle;
    protected boolean mbAnonAllowed;
    protected boolean mbCreateHome;
    protected boolean mbRemoteAdminAllowed;
    protected File mDefaultRoot;
    protected AsyncMessageQueue mQueue;
    protected File baseDir;

    /**
      * Default constructor - first step.
      */
     public AbstractFtpConfig() throws IOException {
         mStatus = new FtpStatus();
         mQueue  = new AsyncMessageQueue();
         mQueue.setMaxSize(4096);
     }

    /**
     * Get server port.
     */
    public final int getServerPort()  {
        return miServerPort;
    }

    /**
     * Get server bind address.
     */
    public final InetAddress getServerAddress() {
        return mServerAddress;
    }

    /**
     * Get self address
     */
    public final InetAddress getSelfAddress() {
        return mSelfAddress;
    }

    /**
     * Check annonymous login support.
     */
    public final boolean isAnonymousLoginAllowed() {
        return mbAnonAllowed;
    }

    /**
     * Get ftp status resource.
     */
    public final FtpStatus getStatus() {
        return mStatus;
    }

    /**
     * Get connection service.
     */
    public final ConnectionService getConnectionService() {
        return mConService;
    }

    /**
     * Get user manager.
     */
    public final UserManagerInterface getUserManager() {
        return mUserManager;
    }

    /**
     * Get maximum number of connections.
     */
    public final int getMaxConnections() {
        return miMaxLogin;
    }

    /**
     * Get maximum number of anonymous connections.
     */
    public final int getMaxAnonymousLogins() {
        if(!isAnonymousLoginAllowed()) {
            return 0;
        }
        return miAnonLogin;
    }

    /**
     * Get poll interval in seconds.
     */
    public final int getSchedulerInterval() {
        return miPollInterval;
    }

    /**
     * Get default idle time in seconds.
     */
    public final int getDefaultIdleTime() {
        return miDefaultIdle;
    }

    /**
     * Get default root directory
     */
    public final File getDefaultRoot() {
        return mDefaultRoot;
    }

    /**
     * Create user home directory if not exist during login
     */
    public final boolean isCreateHome() {
        return mbCreateHome;
    }

    /**
     * Get rmi port
     */
    public final int getRemoteAdminPort() {
        return miRmiPort;
    }

    /**
     * Is remote admin allowed
     */
    public final boolean isRemoteAdminAllowed() {
        return mbRemoteAdminAllowed;
    }

    /**
     * Get base directory
     */
    public final File getBaseDirectory() {
        return baseDir;
    }

    /**
     * Get IP restrictor object.
     */
    public final IpRestrictorInterface getIpRestrictor() {
        return mIpRestrictor;
    }

    /**
     * Get global statistics object.
     */
    public final FtpStatistics getStatistics() {
        return mStatistics;
    }

    /**
     * Get message queue
     */
    public final AsyncMessageQueue getMessageQueue() {
        return mQueue;
    }


    /**
     * Get the system name.
     */
    public final String getSystemName() {
        String systemName = System.getProperty("os.name");
        if(systemName == null) {
            systemName = "UNKNOWN";
        }
        else {
            systemName = systemName.toUpperCase();
            systemName = systemName.replace(' ', '-');
        }
        return systemName;
    }

    public abstract void releaseDataPort(int miPort);

    public abstract int getDataPort();

    public abstract void dispose();

}
