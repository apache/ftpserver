/*
 * Created on Aug 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.ftpserver.remote;

import java.rmi.RemoteException;

import org.apache.ftpserver.AbstractFtpConfig;
import org.apache.ftpserver.interfaces.FtpRemoteHandlerMonitor;

/**
 * @author Vladimirov
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RemoteHandlerFactoryImpl implements RemoteHandlerFactory {
    public RemoteHandler createRemoteHandler(AbstractFtpConfig ftpConfig,
            FtpRemoteHandlerMonitor remoteHandlerMonitor)
            throws RemoteException {
        return new RemoteHandlerImpl(ftpConfig, remoteHandlerMonitor);
    }
}