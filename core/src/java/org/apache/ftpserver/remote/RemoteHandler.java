/*
 * Created on Aug 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.ftpserver.remote;

import org.apache.ftpserver.remote.interfaces.FtpConfigInterface;

/**
 * @author Vladimirov
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public interface RemoteHandler {
    /**
     * Close the remote handler
     */
    public void dispose();

    /**
     * Remote admin login
     */
    public abstract String login(String id, String password) throws Exception;

    /**
     * Remote admin logout
     */
    public abstract boolean logout(String sessId);

    /**
     * Get configuration interface
     */
    public abstract FtpConfigInterface getConfigInterface(String sessId);

    /**
     * Unreferenced - admin user idle timeout
     */
    public abstract void unreferenced();
}