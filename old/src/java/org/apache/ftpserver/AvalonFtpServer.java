package org.apache.ftpserver;

import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.cornerstone.services.sockets.SocketManager;
import org.apache.avalon.cornerstone.services.sockets.ServerSocketFactory;
import org.apache.avalon.cornerstone.services.connection.ConnectionManager;

import java.net.InetAddress;
import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonFtpServer extends FtpServerImpl implements
        Contextualizable,
        Serviceable,
        Configurable,
        Disposable,
        LogEnabled {

    private Logger logger;

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set application context - first spep.
     */
    public void contextualize(Context context) throws ContextException {
        try {
            mConfig = new AvalonFtpConfig();
        }
        catch(IOException ex) {
            logger.error("FtpServerImpl.contextualize()", ex);
            throw new ContextException("FtpServerImpl.contextualize()", ex);
        }
    }

    /**
     * Get all managers - second step.
     * @phoenix:dependency name="org.apache.avalon.cornerstone.services.sockets.SocketManager"
     * @phoenix:dependency name="org.apache.avalon.cornerstone.services.connection.ConnectionManager"
     * @phoenix:dependency name="org.apache.ftpserver.usermanager.UserManagerInterface"
     * @phoenix:dependency name="org.apache.ftpserver.ip.IpRestrictorInterface"
     *
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        mSocManager = (SocketManager)serviceManager.lookup(SocketManager.ROLE);
        mConManager = (ConnectionManager)serviceManager.lookup(ConnectionManager.ROLE);
    }

    /**
     * Configure the server - third step.
     *
     * @param conf the XML configuration block
     */
    public void configure(Configuration conf) throws ConfigurationException {
       try {

            // open server socket
            ServerSocketFactory factory = mSocManager.getServerSocketFactory("plain");
            InetAddress serverAddress = mConfig.getSelfAddress();
            if(serverAddress == null) {
                mServerSocket = factory.createServerSocket(mConfig.getServerPort(), 5);
            }
            else {
                mServerSocket = factory.createServerSocket(mConfig.getServerPort(), 5, serverAddress);
            }
            mConManager.connect(DISPLAY_NAME, mServerSocket, this);

            System.out.println("FTP server ready!");
            if(mConfig.isRemoteAdminAllowed()) {
                System.out.println("You can start the remote admin by executing \"java -jar ftp-admin.jar\".");
            }
       }
       catch(Exception ex) {
           logger.error("FtpServerImpl.configure()", ex);
           throw new ConfigurationException(ex.getMessage(), ex);
       }
    }

    /**
     * Release all resources.
     */
    public void dispose() {
        logger.info("Closing Ftp server...");
        if (mConfig != null) {
            try {
                mConfig.dispose();
            }
            catch(Exception ex) {
                logger.warn("FtpServerImpl.dispose()", ex);
            }
        }
    }

}
